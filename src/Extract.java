import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Extract {

    final static String ParserModelPath = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

    public static void main(String[] args) {
        try {
            Extract.run(new FileReader("d:/review.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<Pattern> run(Reader text) {

        LexicalizedParser parser = LexicalizedParser.loadModel(ParserModelPath);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

        List<Pattern> patterns = new ArrayList<Pattern>();
        for (List<HasWord> sentence : new DocumentPreprocessor(text)) {
            patterns.addAll(ExtractSentencePatterns(parser, gsf, sentence));
        }

        return patterns;
    }

    private static HashSet<Pattern> ExtractSentencePatterns(LexicalizedParser parser, GrammaticalStructureFactory gsf, List<HasWord> sentence) {
        Tree parse = parser.apply(sentence);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

        List<Pattern> primary = ExtractPrimaryPatterns(tdl);
        List<Pattern> combined = new ArrayList<Pattern>();
        combined = ExtractCombinedPatterns(primary, primary);
        combined.addAll(ExtractCombinedPatterns(combined, primary));
        combined.addAll(ExtractCombinedPatterns(combined, primary));

        return PruneCombinedPatterns(combined);
    }

    private static HashSet<Pattern> PruneCombinedPatterns(List<Pattern> combined) {
        List<Pattern> remove = new ArrayList<Pattern>();

        HashSet<Pattern> patterns = new HashSet<Pattern>(combined);
        for (Pattern pattern : patterns) {
            if (patterns.contains(pattern.mother) && pattern.mother.relation != Pattern.Relation.conj_and
                && pattern.father.relation != Pattern.Relation.conj_and) {
                remove.add(pattern.mother);
                remove.add(pattern.father);
            }
        }
        patterns.removeAll(remove);

        return patterns;
    }

    private static List<Pattern> ExtractPrimaryPatterns(Collection<TypedDependency> tdl) {
        List<Pattern> primary = new ArrayList<Pattern>();

        for (TypedDependency td : tdl) {
            Pattern pattern = TryExtractPattern(td);
            if (pattern != null) {
                primary.add(pattern);
            }
        }

        return primary;
    }

    private static Pattern TryExtractPattern(TypedDependency dependency) {
        String rel = dependency.reln().toString();
        String gov = dependency.gov().value();
        String govTag = dependency.gov().label().tag();
        String dep = dependency.dep().value();
        String depTag = dependency.dep().label().tag();

        Pattern.Relation relation = Pattern.asRelation(rel);
        if (relation != null) {
            Pattern pattern = new Pattern(gov, govTag, dep, depTag, relation);
            if (pattern.isPrimaryPattern()) {

                return pattern;
            }
        }

        return null;
    }

    private static List<Pattern> ExtractCombinedPatterns(List<Pattern> combined, List<Pattern> primary) {
        List<Pattern> results = new ArrayList<Pattern>();

        for (Pattern pattern : combined) {
            Pattern aspect = pattern.TryCombine(primary);
            if (aspect != null) {
                results.add(aspect);
            }
        }

        return results;
    }
}
