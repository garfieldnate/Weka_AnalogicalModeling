package weka.classifiers.lazy.AM.lattice;

public class Utils {

	/**
	 * @param card
	 *            Number of features in the subcontext
	 * @param label
	 *            Integer label for the subcontext
	 * @return Binary string representation of the provided label, with zeros
	 *         padded in the front
	 */
	public static String labelToString(int card, int label) {
		StringBuilder sb = new StringBuilder();
		String binary = Integer.toBinaryString(label);

		int diff = card - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');

		sb.append(binary);
		return sb.toString();
	}
}
