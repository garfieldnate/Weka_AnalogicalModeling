package weka.classifiers.lazy.AM.lattice.distributed;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.IntLabel;
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
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Instance test = train.get(0);
		train.remove(0);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH, test, false);
		SubcontextList subList = new SubcontextList(labeler, train);
		// normally a single mask which gives the whole label would not be
		// created, but for testing purposes we use it here.
		LabelMask mask = new LabelMask(0, 3);
		HeterogeneousLattice heteroLattice = new HeterogeneousLattice(subList,
				mask);
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
		}, BigInteger.ONE, 0);//r
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
		}, BigInteger.ONE, 0); //r
		TestUtils.assertContainsSupra(supras, expected);

		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
				add(sub4);
			}
		}, BigInteger.ONE, 0); //r
		TestUtils.assertContainsSupra(supras, expected);

	}

}
