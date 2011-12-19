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

package weka.classifiers.lazy;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;

/**
 * A Weka classifier implementing AM.
 * @author Nathan Glenn
 *
 */
public class AM extends weka.classifiers.Classifier{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1212462913157286103L;

	@Override
	public void buildClassifier(Instances arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Capabilities getCapabilities(){
		Capabilities result = new Capabilities(null);//super.getCapabilities();
		result.disableAllClasses();               // disable all class types
	    result.disableAllClassDependencies();     // no dependencies!
	    result.enable(Capability.NOMINAL_CLASS);  // only nominal classes allowed
		return result;
	}

}
