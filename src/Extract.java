import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Extract {

    final String stopwordsFile = "stopwords.txt";
    List<String> stopwords;

    public Extract() {

        try {
            stopwords = LoadStopwords();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private HashSet<Pattern> ExtractSentencePatterns(CoreMap sentence) {
        SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        List<Pattern> primary = ExtractPrimaryPatterns(semanticGraph.typedDependencies());

        List<Pattern> combined;
        combined = ExtractCombinedPatterns(primary, primary);
        combined.addAll(ExtractCombinedPatterns(combined, primary));
        combined.addAll(ExtractCombinedPatterns(combined, primary));

        return PruneCombinedPatterns(combined);
    }

    private HashSet<Pattern> PruneCombinedPatterns(List<Pattern> combined) {
        List<Pattern> remove = new ArrayList<Pattern>();

        HashSet<Pattern> patterns = new HashSet<Pattern>(combined);
        for (Pattern pattern : patterns) {
            if (patterns.contains(pattern.mother) && pattern.mother.relation != Pattern.Relation.conj_and
                    && pattern.father.relation != Pattern.Relation.conj_and) {
                remove.add(pattern.mother);
                remove.add(pattern.father);
            }

            //remove patterns containing stopwords
            if (stopwords.contains(pattern.head) || stopwords.contains(pattern.modifier)) {
                remove.add(pattern);
            }
        }
        patterns.removeAll(remove);

        return patterns;
    }

    private List<String> LoadStopwords() throws IOException {
        List<String> words = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new FileReader(stopwordsFile));
        String line;
        while ((line = br.readLine()) != null) {
            words.add(line);
        }
        br.close();

        return words;
    }

    public List<Pattern> run(String text) {
        List<Pattern> patterns = new ArrayList<Pattern>();

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            patterns.addAll(ExtractSentencePatterns(sentence));
        }

        return patterns;
    }
}
