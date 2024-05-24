package testData.objectMethodParameterInspection;

class ObjectMethodParameterTest {

	public String method1(TestObject s1) {
		return s1.getField1();
	}
}

class TestObject {
	private String field1;
	public String getField1() {
		return field1;
	}
}