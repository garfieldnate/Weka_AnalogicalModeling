package weka.classifiers.lazy.AM.lattice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test the lattices that can be used for item classification. These are
 * implementations of the {@link Lattice} interface.
 *
 * @author Nathan Glenn
 */
@RunWith(Parameterized.class)
public class LatticeTest {
    @Parameter(0)
    public String testName;
    @Parameter(1)
    public Constructor<Lattice> latticeConstructor;

    /**
     * @return A collection of argument arrays for running tests. In each array: <ol> <li>arg[0] is the test name.</li>
     * <li>arg[1] is the {@link Constructor} for the {@link Lattice} to be tested.</li> </ol>
     * @throws SecurityException     if a lattice constructor can't be run due to security requirements
     * @throws NoSuchMethodException if a lattice constructor can't be run
     */
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> instancesToTest() throws NoSuchMethodException, SecurityException {
        Collection<Object[]> parameters = new ArrayList<>();
        // distributed lattice
        parameters.add(new Object[]{
            DistributedLattice.class.getSimpleName(), DistributedLattice.class.getConstructor(SubcontextList.class)
        });
        return parameters;
    }

    /**
     * Test that supracontexts are properly marked heterogeneous.
     *
     * @throws Exception if the finnverb set can't be loaded
     */
    @Test
    public void testHeterogeneousMarking() throws Exception {
        Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN, "6-10");
		System.out.println(train);
                String[] expectedSupras = new String[]{
            "[1x(01010|&nondeterministic&|H,A,V,I,0,A/H,A,V,A,0,B)]", "[2x(10000|A|K,U,V,U,0,A)]",
            "[2x(00011|C|H,U,V,O,S,C)]", "[1x(10010|A|U,U,V,I,0,A),(10000|A|K,U,V,U,0,A)]",
            "[1x(10010|A|U,U,V,I,0,A),(10110|A|P,U,0,?,0,A),(10000|A|K,U,V,U,0,A)]"
        };
        testSupras(train, 0, expectedSupras);
//
//        train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
//        expectedSupras = new String[]{
//            "[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]", "[2x(00110|B|A,A,V,U,0,B)]",
//            "[2x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B),(00110|B|A,A,V,U,0,B)]",
//            "[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]"
//        };
//        testSupras(train, 0, expectedSupras);
    }

    /**
     * Test that the given test/train combination yields the given list of
     * supracontexts.
     *
     * @param train          Dataset to train with
     * @param testIndex      Index of item in dataset to remove and use as a test item
     * @param expectedSupras String representations of the supracontexts that should be created from the train/test
     *                       combo
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void testSupras(Instances train, int testIndex, String[] expectedSupras) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final Instance test = train.get(testIndex);
        train.remove(testIndex);
        SubcontextList subList = new SubcontextList(getFullSplitLabeler(test), train);
        Lattice testLattice = latticeConstructor.newInstance(subList);
        Set<Supracontext> actualSupras = testLattice.getSupracontexts();

//        assertEquals(expectedSupras.length, actualSupras.size());
//        for (String expected : expectedSupras) {
//            ClassifiedSupra supra = TestUtils.getSupraFromString(expected, train);
//            TestUtils.assertContainsSupra(actualSupras, supra);
//        }
    }

    // create a labeler which splits labels into labels of cardinality 1
    private Labeler getFullSplitLabeler(final Instance test) {
        return new Labeler(MissingDataCompare.VARIABLE, test, false) {
            final Labeler internal = new IntLabeler(MissingDataCompare.VARIABLE, test, false);

            @Override
            public Label label(Instance data) {
                return internal.label(data);
            }

            @Override
            public Label partition(Label label, int partitionIndex) {
                int labelBit = label.matches(partitionIndex) ? 0 : 1;
                return new IntLabel(labelBit, 1);
            }

            @Override
            public int numPartitions() {
                return getCardinality();
            }

            @Override
            public Label getAllMatchLabel() {
                return internal.getAllMatchLabel();
            }
        };
    }
}
