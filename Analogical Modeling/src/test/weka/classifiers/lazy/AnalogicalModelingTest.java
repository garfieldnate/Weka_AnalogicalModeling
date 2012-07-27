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

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests AnalogicalModeling. Run from the command line with:<p>
 * java weka.classifiers.lazy.AnalogicalModelingTest
 *
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 * @version $Revision: 8034 $
 */
public class AnalogicalModelingTest extends AbstractClassifierTest {

  public AnalogicalModelingTest(String name) { super(name);  }

  /** Creates a default AnalogicalModeling */
  @Override
public Classifier getClassifier() {
    return new AnalogicalModeling();
  }

  public static Test suite() {
    return new TestSuite(AnalogicalModelingTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }

}
