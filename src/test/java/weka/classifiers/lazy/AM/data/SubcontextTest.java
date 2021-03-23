package weka.classifiers.lazy.AM.data;

import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.Label;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubcontextTest {
    private static Instances dataset;

    @BeforeClass
    public static void setUpBeforeClass() {
        ArrayList<Attribute> atts = new ArrayList<>();
        ArrayList<String> classes = new ArrayList<>();
        classes.add("e");
        classes.add("r");
        atts.add(new Attribute("a"));
        atts.add(new Attribute("class", classes));
        dataset = new Instances("TestInstances", atts, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        double[][] data = new double[][]{
            new double[]{1, 1}, new double[]{1, 0}
        };
        for (double[] datum : data) {
            Instance instance = new DenseInstance(2, datum);
            dataset.add(instance);
        }
    }

    private static final double DELTA = 1e-10;

    @Test
    public void test() {
        Label label = new IntLabel(0, 1);
        Subcontext s = new Subcontext(label);
        s.add(dataset.get(0));
        assertEquals(s.getExemplars(), new HashSet<Instance>() {
            {
                add(dataset.get(0));
            }
        });
        assertEquals(s.getLabel(), new IntLabel(0, 1));
        assertEquals(s.getOutcome(), 1.0, DELTA);
        assertEquals("(0|r|1,r,{2})", s.toString());

        s.add(dataset.get(1));
        assertEquals(s.getExemplars(), new HashSet<Instance>() {
            {
                add(dataset.get(0));
                add(dataset.get(1));
            }
        });
        assertEquals("Multiple outcomes lead to non-determinism", s.getOutcome(), AMUtils.NONDETERMINISTIC, DELTA);

        // TODO: can't test toString() like this because data is an unordered
        // set
        // assertEquals("(0|&nondeterministic&|1,r,{2}/1,e,{2})", s.toString());
    }
    
    @Test
	public void testToStringWithEmptyData() {
    	Subcontext testSub = new Subcontext(new IntLabel(0b10, 2));
    	String actual = testSub.toString();
    	assertNotNull(actual);
	}

}
