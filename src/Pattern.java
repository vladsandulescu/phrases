import java.util.List;

public class Pattern {
    public String head;
    public String headTag;
    public String modifier;
    public String modifierTag;
    public Relation relation;
    public Pattern mother;
    public Pattern father;

    public Pattern(String head, String modifier, Relation relation) {

        this(head, null, modifier, null, relation);
    }

    public Pattern(String head, String headTag, String modifier, String modifierTag, Relation relation) {

        this(head, headTag, modifier, modifierTag, relation, null, null);
    }

    public Pattern(String head, String headTag, String modifier, String modifierTag, Relation relation, Pattern mother, Pattern father) {
        this.head = head;
        this.headTag = headTag;
        this.modifier = modifier;
        this.modifierTag = modifierTag;
        this.relation = relation;
        this.mother = mother;
        this.father = father;
    }

    public static Relation asRelation(String str) {
        for (Relation relation : Relation.values()) {
            if (relation.name().equalsIgnoreCase(str))
                return relation;
        }
        return null;
    }

    public boolean equals(Object pattern) {
        if (!(pattern instanceof Pattern)) {
            return false;
        }
        return ((Pattern) pattern).head.equals(head)
                && ((Pattern) pattern).modifier.equals(modifier)
                && ((Pattern) pattern).relation.equals(relation);
    }

    public int hashCode() {
        return (head + modifier + relation).hashCode();
    }

    @Override
    public String toString() {
        return head + " " + headTag + " " + modifier + " " + modifierTag + " " + relation;
    }

    public String toAspect() {
        return head + " " + modifier;
    }

    public String toSentences() {
        return head + " . " + modifier;
    }

    public boolean isPrimaryPattern() {
        switch (relation) {
            case amod:
                return headTag.equals("NN") && modifierTag.startsWith("JJ");
            case acomp:
                return headTag.startsWith("VB") && modifierTag.startsWith("JJ");
            case nsubj:
                return headTag.startsWith("VB") && modifierTag.equals("NN")
                        || headTag.startsWith("JJ") && modifierTag.equals("NN")
                        || headTag.startsWith("VB") && modifierTag.startsWith("PR");
            case cop:
                return headTag.startsWith("JJ") && modifierTag.startsWith("VB");
            case dobj:
                return headTag.startsWith("VB") && modifierTag.equals("NN");
            case conj_and:
                return headTag.equals("NN") && modifierTag.equals("NN")
                        || headTag.startsWith("JJ") && modifierTag.startsWith("JJ")
                        || headTag.startsWith("VB") && modifierTag.startsWith("VB");
            case neg:
            case conj_but:
                return (headTag.startsWith("JJ") || headTag.startsWith("VB"));
            case nn:
                return headTag.equals("NN") && modifierTag.equals("NN");
        }

        return false;
    }

    public Pattern TryCombine(List<Pattern> patterns) {
        switch (relation) {
            case amod:
                return TryCombineAmod();
            case acomp:
                return TryCombineAcomp(patterns);
            case cop:
                return TryCombineCop(patterns);
            case dobj:
                return TryCombineDobj(patterns);
            case aspect:
                for (Pattern pattern : patterns) {
                    Pattern newAspect = null;
                    switch (pattern.relation) {
                        case conj_and:
                            newAspect = TryCombineAspectWithConjAnd(pattern);
                            break;
                        case conj_but:
                            newAspect = TryCombineAspectWithConjBut(pattern);
                            break;
                        case neg:
                            newAspect = TryCombineAspectWithNeg(pattern);
                            break;
                        case nn:
                            newAspect = TryCombineAspectWithNn(pattern);
                            break;
                    }
                    if (newAspect != null) {
                        return newAspect;
                    }
                }
                return null;
        }

        return null;
    }

    private Pattern TryCombineAspectWithNn(Pattern pattern) {
        if (pattern.head == head && pattern.modifierTag.equals("NN")) {

            return new Pattern(pattern.modifier + " " + head, headTag, modifier, modifierTag, Relation.aspect, this, pattern);
        }
        if (pattern.modifier == head && pattern.headTag.equals("NN")) {

            return new Pattern(head + " " + pattern.head, headTag, modifier, modifierTag, Relation.aspect, this, pattern);
        }

        return null;
    }

    private Pattern TryCombineAspectWithNeg(Pattern pattern) {
        if (pattern.head == modifier
                && (pattern.modifier.equals("not") || pattern.modifier.equals("n't"))) {

            return new Pattern(head, headTag, pattern.modifier + " " + modifier, pattern.modifierTag, Relation.aspect, this, pattern);
        }

        return null;
    }

    private Pattern TryCombineAspectWithConjAnd(Pattern pattern) {
        if (pattern.head == head && pattern.modifierTag.equals("NN")) {

            return new Pattern(pattern.modifier, pattern.modifierTag, modifier, modifierTag, Relation.aspect, this, pattern);
        }

        if (pattern.head == modifier
                && (pattern.modifierTag.startsWith("JJ")
                || pattern.modifierTag.startsWith("VB"))) {

            return new Pattern(head, headTag, pattern.modifier, pattern.modifierTag, Relation.aspect, this, pattern);
        }

        return null;
    }

    private Pattern TryCombineAspectWithConjBut(Pattern pattern) {
        if (pattern.head == modifier
                && (pattern.modifierTag.startsWith("RB"))) {

            return new Pattern(head, headTag, pattern.modifier + " " + pattern.head, pattern.headTag, Relation.aspect, this, pattern);
        }

        return null;
    }

    private Pattern TryCombineDobj(List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.relation == Pattern.Relation.nsubj
                    && pattern.headTag.startsWith("VB")
                    && pattern.modifierTag.startsWith("PR")
                    && !pattern.modifier.equals(modifier)) {

                return new Pattern(modifier, modifierTag, head, headTag, Relation.aspect, this, pattern);
            }
        }

        return null;
    }

    private Pattern TryCombineCop(List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.relation == Pattern.Relation.nsubj
                    && pattern.headTag.startsWith("JJ")
                    && pattern.modifierTag.equals("NN")
                    && pattern.head.equals(head)) {

                return new Pattern(pattern.modifier, pattern.modifierTag, head, headTag, Relation.aspect, this, pattern);
            }
        }

        return null;
    }

    private Pattern TryCombineAcomp(List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.relation == Pattern.Relation.nsubj
                    && pattern.headTag.startsWith("VB")
                    && pattern.modifierTag.equals("NN")
                    && pattern.head.equals(head)) {

                return new Pattern(pattern.modifier, pattern.modifierTag, modifier, modifierTag, Relation.aspect, this, pattern);
            }
        }

        return null;
    }

    private Pattern TryCombineAmod() {
        return new Pattern(head, headTag, modifier, modifierTag, Relation.aspect);
    }

    public enum Relation {
        amod,
        acomp,
        nsubj,
        cop,
        dobj,
        conj_and,
        neg,
        conj_but,
        nn,
        aspect
    }
}