import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

public class TestPatterns {

    /**
     * 1. amod(N,A)→< N,A >
     */
    @Test
    public void Pattern1() {
        Pattern goal1 = new Pattern("zoom", "great", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("resolution", "great", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("This camera has great zoom and resolution.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 2. acomp(V,A)+nsubj(V,N)→< N,A >
     */
    @Test
    public void Pattern2() {
        Pattern goal = new Pattern("camera case", "nice", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The camera case looks nice.");

        Assert.assertTrue(patterns.contains(goal));
    }

    /**
     * 3. cop(A,V )+nsubj(A,N)→< N,A >
     */
    @Test
    public void Pattern3() {
        Pattern goal1 = new Pattern("screen", "wide", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("screen", "clear", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The screen is wide and clear.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 4. dobj(V,N)+nsubj(V,N')→< N,V >
     */
    @Test
    public void Pattern4() {
        Pattern goal = new Pattern("picture quality", "love", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("I love the picture quality.");

        Assert.assertTrue(patterns.contains(goal));
    }

    /**
     * 5. < h1,m > +conj_and(h1,h2)→< h2,m >
     */
    @Test
    public void Pattern5() {
        Pattern goal1 = new Pattern("zoom", "great", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("resolution", "great", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("This camera has great zoom and resolution.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 6. < h,m1 > +conj_and(m1,m2)→< h,m2 >
     */
    @Test
    public void Pattern6() {
        Pattern goal1 = new Pattern("screen", "wide", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("screen", "clear", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The screen is wide and clear.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 6. < h,m1 > +conj_and(m1,m2)→< h,m2 >
     */
    @Test
    public void Pattern61() {
        Pattern goal1 = new Pattern("screen", "love", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("screen", "hate", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("I love and hate the screen.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 6. < h,m1 > +conj_and(m1,m2)→< h,m2 >
     *     This is a shortcoming of the dependency parser as it does not detect acomp
     */
    @Test
    public void Pattern62() {
        Pattern goal1 = new Pattern("screen", "love", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("camera case", "love", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("I love the screen and the camera case.");

        Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }


    /**
     * 7. < h,m > +neg(m,not)→< h, not+m >
     */
    @Test
    public void Pattern7() {
        Pattern goal = new Pattern("battery life", "not long", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The battery life is not long");

        Assert.assertTrue(patterns.contains(goal));
    }

    /**
     * 7. < h,m > +neg(m,not)→< h, not+m >
     */
    @Test
    public void Pattern71() {
        //Pattern goal1 = new Pattern("battery life", "good", Pattern.Relation.aspect);
        Pattern goal2 = new Pattern("battery life", "not great", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The battery life was good but not great");

        //Assert.assertTrue(patterns.contains(goal1));
        Assert.assertTrue(patterns.contains(goal2));
    }

    /**
     * 8. < h,m > +nn(h,N)→< N +h,m >
     */
    @Test
    public void Pattern8() {
        Pattern goal = new Pattern("camera case", "nice", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("The camera case looks nice.");

        Assert.assertTrue(patterns.contains(goal));
    }

    /**
     * 9. < h,m > +nn(N,h)→< h+N,m >
     */
    @Test
    public void Pattern9() {
        Pattern goal = new Pattern("picture quality", "love", Pattern.Relation.aspect);

        List<Pattern> patterns = new Extract().run("I love the picture quality.");

        Assert.assertTrue(patterns.contains(goal));
    }
}
