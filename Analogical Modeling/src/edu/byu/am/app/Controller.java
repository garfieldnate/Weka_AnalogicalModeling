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

import java.io.FileWriter;
import java.io.IOException;
import edu.byu.am.data.DataLoader;

/**
 * This class will be the entry point for using Analogical Modeling to predict
 * behavior.
 * 
 * @author Nate Glenn
 * 
 */
public class Controller {
	
	public static void main(String[] args) throws IOException {
		Options.parseArgs(args);
		FileWriter output = new FileWriter(Options.getOption(Options.OUTPUT_PATH));
		if(Options.getBooleanOption(Options.VERBOSE))
			Options.writeOptions(output);
//		Classifier cl = new Classifier(Options.getOption(Options.INPUT_PATH));
		DistTest cl = new DistTest(Options.getOption(Options.INPUT_PATH));
		if(Options.getOption(Options.TEST_PATH).equals(Options.LEAVE_ONE_OUT))
			output.write(cl.leaveOneOut().toString());
		else
			output.write(cl.classify(Options.getOption(Options.TEST_PATH)).toString());
	}

	public static void demo() throws IOException{
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("");//"[ ,\t]+");

		dl.setOutcomeIndex(0);
		Classifier cl = new Classifier(dl, "finnverb.txt");//"xPlural.txt");
//		cl.
//		new FileWriter("demoOutput.txt").write(cl.leaveOneOut().toString());
//		System.out.println(cl.leaveOneOut());
	}
}
