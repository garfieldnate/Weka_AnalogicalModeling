package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.HeterogeneousLattice;
import weka.core.Instance;
import weka.core.Instances;

public class HeterogeneousLatticeTest {

	@SuppressWarnings("serial")
	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		final Instance test = train.get(0);
		train.remove(0);

		// Define a labeler which doesn't partition labels so that we can just
		// test with the chapter 3 data without it being reduced to a
		// cardinality of one
		Labeler noPartitionLabeler = new Labeler(MissingDataCompare.MATCH,
				test, false) {
			Labeler l = new IntLabeler(MissingDataCompare.MATCH, test, false);

			@Override
			public Label label(Instance data) {
				return l.label(data);
			}

			@Override
			public int numPartitions() {
				return 1;
			}

			@Override
			public Label partition(Label label, int partitionIndex) {
				return label;
			}
		};
		SubcontextList subList = new SubcontextList(noPartitionLabeler, train);
		HeterogeneousLattice heteroLattice = new HeterogeneousLattice(subList,
				0);
		List<Supracontext> supras = heteroLattice.getSupracontextList();

		final Subcontext sub1 = new Subcontext(new IntLabel(0b001, 3));
		sub1.add(train.get(0));// 310e
		sub1.add(train.get(4));// 311r
		final Subcontext sub2 = new Subcontext(new IntLabel(0b100, 3));
		sub2.add(train.get(3));// 212r
		final Subcontext sub3 = new Subcontext(new IntLabel(0b101, 3));
		sub3.add(train.get(1));// 210r
		final Subcontext sub4 = new Subcontext(new IntLabel(0b110, 3));
		sub4.add(train.get(2));// 032r

		// TODO: the heterolattice does not set outcomes in a meaningful way, so
		// these should not be tested.
		Supracontext expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		}, BigInteger.valueOf(2), AMUtils.NONDETERMINISTIC);
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub2);
			}
		}, BigInteger.ONE, 0);// r
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		}, BigInteger.ONE, 0);// r
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub2);
				add(sub4);
			}
		}, BigInteger.ONE, 0); // r
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
				add(sub4);
			}
		}, BigInteger.ONE, 0); // r
		TestUtils.assertContainsSupra(supras, expected);

	}

}
