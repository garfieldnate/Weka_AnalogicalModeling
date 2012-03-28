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

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author Nate Glenn
 *
 */
public class Options {
	private static final String USAGE = "Options: " +
			"<-C path to config file holding run parameters>\n" +
			"<-e exemplar separator; newline by default, but that's not possible on the " +
			"command line>\n" +
			"<-E path to file holding exemplars>\n" +
			"<-T path to file holding exemplars to predict the outcomes for, or 'leave_one_out'," +
			" or a list of exemplars " +
			"separated by the exemplar separator>\n" +
			"<-f regex which separates features in input exemplar strings>\n" +
			"<-o path to output file>\n" +
			"<-I index of outcome in input exemplar vectors>\n" +
			"<-l calculate pointers linearly instead of quadratically>\n" +
			"<-M method of dealing with missing data (variable, match or mismatch)>\n" +
			"<-m feature string to indicate missing data>\n" +
			"<-c comment string>\n" +
			"<-v verbose>\n" +
			"<-r remove items which are present in testing data from training data>";
	
	//Define names of all options here;
	public static final String VERBOSE = "verbose";
	public static final String INPUT_COMMENT = "input.comment";
	public static final String INPUT_MISSING_STRING = "input.missing.string";
	public static final String ALGORITHM_MISSING_STRATEGY = "algorithm.missing.strategy";
	public static final String INPUT_EXEMPLAR_SEP = "input.exemplar.sep";
	public static final String INPUT_FEATURE_SEP = "input.feature.sep";
	public static final String INPUT_PATH = "input.path";
	public static final String TEST_PATH = "test.path";
	public static final String OUTPUT_PATH = "output.path";
	public static final String POINTERS_COUNT = "algorithm.pointers.count";
	public static final String REMOVE_DUPLICATES = "algorithm.removeDuplicates";
	public static final String EXEMPLAR_OUTCOME_INDEX = "exemplar.outcome.index";
	public static final String LEAVE_ONE_OUT = "leave_one_out";

	public static Properties properties;

	static{
		properties = new Properties();
		//load the properties here;
		try {
			properties.load(Options.class.getResourceAsStream("default.prop"));
		} catch (IOException e) {
			System.err.println("Failed to load default properties file!");
			e.printStackTrace();
		}
	}
	
	public static String getOption(String key){
		return properties.getProperty(key);
	}
	
	public static boolean getBooleanOption(String key){
		return Boolean.parseBoolean(properties.getProperty(key));
	}

	public static void setOption(String key, String val){
		properties.setProperty(key, val);
	}
	
	/**
	 * 
	 * @param fileName Name of the file containing the properties
	 * @throws FileNotFoundException If properties file is not found
	 * @throws IOException If an error occurs while trying to load the properties file
	 */
	public static void loadOptions(String fileName) throws FileNotFoundException, IOException{
		properties.load(new FileInputStream(fileName));
	}
	
	public static void writeOptions(FileWriter output) throws IOException{
		properties.store(output,"Analogical Modeling options");
	}


	/**
	 * Parses all of the command line arguments and sets the relevant properties in 
	 * {@link Options#properties}
	 * @param args Command line arguments
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	static void parseArgs(String[] args) throws FileNotFoundException, IOException {
		CmdLineParser clp = new CmdLineParser();

		//config file path
		Option configFile = clp.addStringOption('C', "config");

		//exemplar file path, or "leave_one_out", or exemplar list
		Option testFile = clp.addStringOption('T',"exemplars");
		
		//exemplar file path, or "leave_one_out", or exemplar list
		Option exFile = clp.addStringOption('E',"exemplars");
		
		//output file path; not specified means System.out
		Option outFile = clp.addStringOption('o',"output");
		
		//Regex separator between features in an exemplar
		Option featSep = clp.addStringOption('f',"featureSep");
		
		//index of outcome in exemplar; default is last feature.
		Option outcomeIndex = clp.addStringOption('I',"outcomeIndex");
		
		//true means linear pointer calculation; false means quadratic
		Option linear = clp.addBooleanOption('l',"linear");
		
		//how to deal with missing data: "variable", "match", or "mismatch"
		Option missingStrategy = clp.addStringOption('M', "missing");
		
		//string used to indicate missing data
		Option missingStr = clp.addStringOption('m', "missingString");
		
		Option comment = clp.addBooleanOption('c',"comment");
		
		Option verbose = clp.addBooleanOption('v',"verbose");
		
		Option removeDup = clp.addBooleanOption('r',"removeDuplicates");
		
		//TODO: parse arguments for:
		//	subcontext info
		//		lists of exemplars fitting in each subcontext
		//		nondeterministic subcontexts
		//	gangs
		
		try {
			clp.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			throw new IllegalArgumentException(e.getMessage() + "\n" + USAGE);
		}
				
		String configPath = (String) clp.getOptionValue(configFile);
		if(configPath != null)
			try {
				loadOptions(configPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		
		String exInPath = (String) clp.getOptionValue(exFile);
		if(exInPath != null)
			setOption(INPUT_PATH, exInPath);
//		if(!new File(exInPath).exists())
//			throw new FileNotFoundException("Could not find " + exInPath);
		
		String testPath = (String) clp.getOptionValue(testFile);
		if(testPath != null)
//			throw new IllegalArgumentException("No test data specified!");
			setOption(TEST_PATH, testPath);

		String exOutPath = (String) clp.getOptionValue(outFile);
		if(exOutPath != null)
			setOption(OUTPUT_PATH, exOutPath);

		String featSepString = (String) clp.getOptionValue(featSep);
		if(featSepString != null)
			setOption(INPUT_FEATURE_SEP, featSepString);

		Integer outcomeIndexInt = (Integer) clp.getOptionValue(outcomeIndex);
		if(outcomeIndexInt != null)
			setOption(EXEMPLAR_OUTCOME_INDEX, outcomeIndexInt.toString());
		
		Boolean linearBool = (Boolean) clp.getOptionValue(linear);
		if(linearBool != null)
			setOption(POINTERS_COUNT,"linear");
		
		//TODO: check input; must be one of three strings
		String missingStrategyString = (String) clp.getOptionValue(missingStrategy);
		if(missingStrategyString != null)
			setOption(ALGORITHM_MISSING_STRATEGY,missingStrategyString);
		
		String missingString = (String) clp.getOptionValue(missingStr);
		if(missingString != null)
			setOption(INPUT_MISSING_STRING,missingString);
		
		String commentString = (String) clp.getOptionValue(comment);
		if(commentString != null)
			setOption(INPUT_COMMENT,commentString);
		
		Boolean verboseBool = (Boolean) clp.getOptionValue(verbose);
		if(verboseBool != null)
			setOption(VERBOSE,verboseBool.toString());
		
		Boolean removeDuplicates = (Boolean) clp.getOptionValue(removeDup);
		if(removeDuplicates != null)
			setOption(REMOVE_DUPLICATES,removeDuplicates.toString());
	}

}
