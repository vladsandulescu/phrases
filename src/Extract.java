import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Extract {

    public static void main(String[] args) {
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        Extract extract = new Extract();
        extract.run(lp, "d:/review.txt");
    }

    public void run(LexicalizedParser lp, String filename) {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            Tree parse = lp.apply(sentence);
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

            List<Pattern> patterns = new ArrayList<Pattern>();

            for (TypedDependency td : tdl) {
                String reln = td.reln().toString();
                String gov = td.gov().value();
                String govTag = td.gov().label().tag();
                int govIdx = td.gov().index();
                String dep = td.dep().value();
                String depTag = td.dep().label().tag();
                int depIdx = td.dep().index();
                boolean extra = td.extra();

                Pattern.Relation relation = Pattern.asRelation(reln);
                if (relation != null) {
                    Pattern pattern = new Pattern(gov, govTag, dep, depTag, relation);
                    if (pattern.isValid()) {
                        patterns.add(pattern);

                        System.out.println(pattern.toString());
                    }
                }
            }

            List<Pattern> extra_patterns = new ArrayList<Pattern>();
            for (Pattern pattern : patterns) {
                Pattern extra_pattern = pattern.Combine(patterns);
                if (extra_pattern != null) {
                    extra_patterns.add(extra_pattern);
                }
            }
        }
    }
}
