package weka.classifiers.lazy.AM.lattice.distributed;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.Label;
import weka.classifiers.lazy.AM.lattice.Labeler;
import weka.classifiers.lazy.AM.lattice.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.SubcontextList;
import weka.classifiers.lazy.AM.lattice.Supracontext;
import weka.core.Instance;
import weka.core.Instances;

public class HeterogeneousLatticeTest {

	@SuppressWarnings("serial")
	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.chapter3Train();
		Instance test = TestUtils.chapter3Test();

		Labeler labeler = new Labeler(MissingDataCompare.MATCH, test, false);
		SubcontextList subList = new SubcontextList(labeler, train);
		// each mask will contain one feature
		LabelMask[] masks = LabelMask.getMasks(1, labeler.getCardinality());
		HeterogeneousLattice heteroLattice = new HeterogeneousLattice(subList,
				masks[0]);
		List<Supracontext> supras = heteroLattice.getSupracontextList();

		final Subcontext sub1 = new Subcontext(new Label(0b001, 3));
		sub1.add(train.get(0));// 310e
		sub1.add(train.get(4));// 311r
		final Subcontext sub2 = new Subcontext(new Label(0b100, 3));
		sub2.add(train.get(3));// 212r
		final Subcontext sub3 = new Subcontext(new Label(0b101, 3));
		sub3.add(train.get(1));// 210r
		final Subcontext sub4 = new Subcontext(new Label(0b110, 3));
		sub4.add(train.get(2));// 032r

		// TODO: the heterolattice does not set outcomes in a meaningful way, so
		// these should not be tested.
		Supracontext expected = new Supracontext();
		expected.setCount(2);
		expected.setOutcome(AMUtils.NONDETERMINISTIC);
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		});
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext();
		expected.setCount(1);
		expected.setOutcome(0);// r
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub2);
			}
		});
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext();
		expected.setCount(1);
		expected.setOutcome(0);// r
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		});
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext();
		expected.setCount(1);
		expected.setOutcome(0);// r
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub2);
				add(sub4);
			}
		});
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext();
		expected.setCount(1);
		expected.setOutcome(0);// r
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
				add(sub4);
			}
		});
		TestUtils.assertContainsSupra(supras, expected);

	}

}
