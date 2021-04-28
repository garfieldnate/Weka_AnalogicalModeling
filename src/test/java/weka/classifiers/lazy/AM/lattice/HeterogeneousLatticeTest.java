package weka.classifiers.lazy.AM.lattice;

import org.junit.Test;

import org.mockito.Mockito;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.*;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test the HeterogeneousLattice, which does not remove heterogeneous
 * supracontexts and therefore cannot be tested with the other {@link Lattice}
 * implementations in {@link LatticeTest}.
 *
 * @author Nathan Glenn
 */
public class HeterogeneousLatticeTest {
    @Test
    public void testChapter3Data() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        final Instance test = train.get(0);
        train.remove(0);

        // Define a labeler which doesn't partition labels so that we can just
        // test with the chapter 3 data without it being reduced to a
        // cardinality of one
		Labeler noPartitionLabeler = Mockito.spy(new LabelerFactory.IntLabelerFactory().createLabeler(test, false, MissingDataCompare.MATCH));
		when(noPartitionLabeler.numPartitions()).thenReturn(1);
		SubcontextList subList = new SubcontextList(noPartitionLabeler, train, false);
		HeterogeneousLattice heteroLattice = new HeterogeneousLattice(0);
		heteroLattice.fill(subList);

		Set<Supracontext> actualSupras = heteroLattice.getSupracontexts();

        String[] expectedSupras = new String[]{
            "[2x(001|&nondeterministic&|3,1,0,e/3,1,1,r)]", "[1x(100|r|2,1,2,r)]",
            "[1x(001|&nondeterministic&|3,1,0,e/3,1,1,r),(100|r|2,1,2,r),(101|r|2,1,0,r)]",
            "[1x(110|r|0,3,2,r),(100|r|2,1,2,r)]",
            "[1x(001|&nondeterministic&|3,1,0,e/3,1,1,r),(110|r|0,3,2,r),(100|r|2,1,2,r),(101|r|2,1,0,r)]"
        };

        assertEquals(expectedSupras.length, actualSupras.size());
        for (String expected : expectedSupras) {
            ClassifiedSupra supra = TestUtils.getSupraFromString(expected, train);
            TestUtils.assertContainsSupra(actualSupras, supra);
        }
    }

}
