package weka.classifiers.lazy.AM;

import org.junit.Assume;
import org.junit.Test;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instances;

import static junit.framework.TestCase.assertEquals;

public class IntegrationTests {
    // This dataset has published results with AM, so we ensure our accuracy matches the publication
    // It's a long test, though, so only run it during integration tests
    @Test
    public void testSpanishStress() throws Exception {
        Assume.assumeTrue(TestUtils.RUN_INTEGRATION_TESTS);

        Instances train = TestUtils.getDataSet(TestUtils.SPANISH_STRESS);
        AnalogicalModeling am = new AnalogicalModeling();
        // Ensure Johnsen-Johansson lattice runs deterministically
        am.setRandomProvider(TestUtils.getDeterministicRandomProvider());

        int numCorrect = TestUtils.leaveOneOut(am, train);
        assertEquals("Leave-one-out accuracy of Spanish stress dataset", 4727, numCorrect);
    }
}
