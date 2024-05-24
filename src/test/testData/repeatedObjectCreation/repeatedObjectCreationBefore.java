package testData.repeatedObjectCreation;

class ObjectMethodParameterTest {
	public void method1() {
		var1 = new TestObject("a");
		String a = "a";
		var2 = new TestObject(a);
	}
}

class TestObject {
	private String field1;

	public TestObject(String field1) {
		this.field1 = field1;
	}

	public String getField1() {
		return field1;
	}
}