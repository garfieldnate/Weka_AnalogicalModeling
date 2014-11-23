/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy;

import junit.framework.TestSuite;

import org.junit.Test;

import weka.classifiers.AbstractClassifierTest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Tests AnalogicalModeling.
 * 
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 */
public class AnalogicalModelingTest extends AbstractClassifierTest {
	public AnalogicalModelingTest(String name) {
		super(name);
//		DEBUG = true;
	}

	/** Creates a default AnalogicalModeling */
	@Override
	public AnalogicalModeling getClassifier() {
		return new AnalogicalModeling();
	}
	
	private static final double DELTA = 1e-10;
	@Test
	public void testChapter3dataSerial() throws Exception {
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		
		source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
		
		AnalogicalModeling am = getClassifier();
		
		am.buildClassifier(train);
		double[] prediction = am.distributionForInstance(test.firstInstance());
		assertEquals("distribution given for two classes", prediction.length, 2);
		assertEquals(0.6923076923076923, prediction[0], DELTA);
		assertEquals(0.3076923076923077, prediction[1], DELTA);
	}
	
	@Test
	public void testChapter3dataParallel() throws Exception {
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		
		source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
		
		AnalogicalModeling am = getClassifier();
		am.setParallel(true);
		
		am.buildClassifier(train);
		double[] prediction = am.distributionForInstance(test.firstInstance());
		assertEquals("distribution given for two classes", prediction.length, 2);
		assertEquals(0.6923076923076923, prediction[0], DELTA);
		assertEquals(0.3076923076923077, prediction[1], DELTA);
	}

	public static junit.framework.Test suite() {
		return new TestSuite(AnalogicalModelingTest.class);
	}

}
