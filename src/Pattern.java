import java.util.List;

public class Pattern {
    public String head;
    public String headTag;
    public String modifier;
    public String modifierTag;
    public Relation relation;

    public Pattern(String head, String headTag, String modifier, String modifierTag, Relation relation) {
        this.head = head;
        this.headTag = headTag;
        this.modifier = modifier;
        this.modifierTag = modifierTag;
        this.relation = relation;
    }

    public enum Relation {
        amod,
        acomp,
        nsubj,
        cop,
        dobj,
        conj_and,
        neg,
        nn,
        aspect
    }

    public static Relation asRelation(String str) {
        for (Relation relation : Relation.values()) {
            if (relation.name().equalsIgnoreCase(str))
                return relation;
        }
        return null;
    }

    @Override
    public String toString() {
        return head + " " + headTag + " " + modifier + " " + modifierTag + " " + relation;
    }

    public boolean isValid() {
        switch (relation) {
            case amod:
                return headTag.startsWith("NN") && modifierTag.startsWith("JJ");
            case acomp:
                return headTag.startsWith("VB") && modifierTag.startsWith("JJ");
            case nsubj:
                return headTag.startsWith("VB") && modifierTag.startsWith("NN")
                        ||
                        headTag.startsWith("JJ") && modifierTag.startsWith("NN");
            case cop:
                return headTag.startsWith("JJ") && modifierTag.startsWith("VB");
            case dobj:
                return headTag.startsWith("VB") && modifierTag.startsWith("NN");
        }

        return false;
    }

    public Pattern Combine(List<Pattern> patterns) {
        switch(relation) {
            case acomp:
                for (Pattern pattern : patterns) {
                    if (pattern.relation == Pattern.Relation.nsubj
                            && pattern.headTag.startsWith("VB")
                            && pattern.modifierTag.startsWith("NN")
                            && pattern.head.equals(head)) {

                        return new Pattern(pattern.modifier, pattern.modifierTag, modifier, modifierTag, Relation.aspect);
                    }
                }
            case cop:
                for (Pattern pattern : patterns) {
                    if (pattern.relation == Pattern.Relation.nsubj
                            && pattern.headTag.startsWith("JJ")
                            && pattern.modifierTag.startsWith("NN")
                            && pattern.head.equals(head)) {

                        return new Pattern(pattern.modifier, pattern.modifierTag, head, headTag, Relation.aspect);
                    }
                }
            case dobj:
                for (Pattern pattern : patterns) {
                    if (pattern.relation == Pattern.Relation.nsubj
                            && pattern.headTag.startsWith("VB")
                            && pattern.modifierTag.startsWith("NN")
                            && !pattern.modifier.equals(modifier)) {

                        return new Pattern(modifier, modifierTag, head, headTag, Relation.aspect);
                    }
                }
            case aspect:
                for (Pattern pattern : patterns) {
                    switch(pattern.relation) {
                        case conj_and:
                            if (pattern.headTag.startsWith("NN")
                                && pattern.modifierTag.startsWith("NN")
                                && !pattern.modifier.equals(modifier)) {

                                return new Pattern(modifier, modifierTag, head, headTag, Relation.aspect);
                            }
                    }
                }
        }

        return null;
    }
}