package testData.scatteredFunctionalityInspection;

import org.junit.Test;

class ScatteredFunctionalityTest {

	public static boolean validateTestObject(TestObject obj) {
		if (obj == null) {
			return false;
		}
		increment(obj.getFieldInt1()) ;
		return obj.getFieldInt2() != null;
	}

	public static boolean validateTestObjectObject(TestObject obj) {
		if (obj == null) {
			return false;
		}
		////
		/**
		 *
		 */





		increment(obj.getFieldInt1());


		//
		return obj.getFieldInt2() != null;
	}
}