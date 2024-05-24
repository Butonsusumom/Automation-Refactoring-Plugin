package testData.testNamingInspection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

class EnumComparisonTest {

	@org.junit.Test
	public void shouldA() {
		// given
		TestObject given = new TestObject();
		given.setField3("initialValue");

		String expected = "initialValue";

		// when
		String actual = classUnderTest.method3(given);

		//then
		assertEquals(actual, expected);
	}
}