package testData.objectMethodParameterInspection;

class ObjectMethodParameterTest {

	public String method1(String field1) {
		return field1;
	}
}

class TestObject {
	private String field1;
	public String getField1() {
		return field1;
	}
}