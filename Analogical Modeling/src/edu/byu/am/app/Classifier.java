/*
 * 	Analogical Modeling Java module
 *  Copyright (C) 2011  Nathan Glenn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.byu.am.app;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.byu.am.data.AnalogicalSet;
import edu.byu.am.data.DataLoader;
import edu.byu.am.data.Exemplar;
import edu.byu.am.lattice.Lattice;
import edu.byu.am.lattice.SubcontextList;

/**
 * This controls all of the other AM classes in predicting item outcomes.
 * 
 * TODO: add documentation on options
 * @author Nate Glenn
 * 
 */
public class Classifier {
	
	/**
	 * Exemplars
	 */
	List<Exemplar> data;

	/**
	 * Supracontextual lattice
	 */
	Lattice lattice;// supracontextual lattice

	SubcontextList subList;

	/**
	 * Preconfigured dataloader used when loading files.
	 */
	DataLoader dl;

	/**
	 * cardinality of the vectors
	 */
	int card;

	/**
	 * 
	 * @param fileName
	 *            containing exemplar vectors
	 * @throws IOException
	 *             If an error occurs while reading fileName
	 */
	public Classifier(String fileName) throws IOException {
		makeDataLoader();
		load(fileName);
		card = data.get(0).size();
	}

	/**
	 * 
	 * @param dloader
	 *            preconfigured {@link DataLoader}
	 * @param fileName
	 *            containing exemplar vectors
	 * @throws IOException
	 *             If an error occurs while reading fileName
	 */
	public Classifier(DataLoader dloader, String fileName) throws IOException {
		dl = dloader;
		load(fileName);
		card = data.get(0).size();
	}

	/**
	 * 
	 * @param fileName
	 *            Name of file to load exemplars from
	 * @throws IOException
	 *             If an error occurs while reading fileName
	 */
	public void load(String fileName) throws IOException {
		data = dl.exemplars(fileName);
	}

	//create the data loader with settings based on the contents of properties
	private void makeDataLoader() {
		dl = new DataLoader();
		dl.setCommentor(Options.properties.getProperty("input.comment"));
		dl.setFeatureSeparator(Options.properties.getProperty("input.feature.sep"));
	}

	public void setDataLoader(DataLoader dloader) {
		dl = dloader;
	}

	/**
	 * 
	 * @return List of analogical sets representing a classification of one of
	 *         the exemplars; each of the exemplars will be classified by all of
	 *         the other exemplars.
	 */
	public List<AnalogicalSet> leaveOneOut() {
		List<AnalogicalSet> sets = new LinkedList<AnalogicalSet>();
		Exemplar temp;
		for (int i = 0; i < data.size(); i++) {
			temp = data.remove(i);
			sets.add(classify(temp));
			data.add(temp);
		}
		return sets;
	}

	/**
	 * 
	 * @param fileName
	 *            containing test items
	 * @return
	 * @throws IOException
	 *             If an error occurs while reading fileName
	 */
	public List<AnalogicalSet> classify(String fileName) {
		if (dl == null)
			makeDataLoader();
		System.out.println("here: " + fileName);

		List<AnalogicalSet> sets = new LinkedList<AnalogicalSet>();
		try {
			for (Exemplar ex : dl.exemplars(fileName))
				sets.add(classify(ex));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		System.out.println(sets.size());
		return sets;
	}

	/**
	 * 
	 * @param testItem
	 *            Item to make context base on
	 * @return Analogical set which holds results of the classification for the
	 *         given item
	 */
	private AnalogicalSet classify(Exemplar testItem) {
		System.out.println("Classifying: " + testItem);
		// 1. Place each data item in a subcontext
		subList = new SubcontextList(testItem, data);
		// 2. Place subcontexts into the supracontextual lattice
		lattice = new Lattice(card, subList);
		// 3. pointers in homogeneous supracontexts are used to give the
		// analogical set and predicted outcome.
		return new AnalogicalSet(lattice.getSupracontextList(), testItem,
				Boolean.parseBoolean(Options.properties.getProperty("algorithm.pointers.linear")));
	}

	public static void main(String[] args) throws IOException {
		Classifier cl = new Classifier("ch3example.txt");
		// System.out.println(cl.classify("ch3examplePredict.txt"));
		System.out.println(cl.leaveOneOut());

		// cl = new Classifier("A-An corpus.txt");

	}
}
