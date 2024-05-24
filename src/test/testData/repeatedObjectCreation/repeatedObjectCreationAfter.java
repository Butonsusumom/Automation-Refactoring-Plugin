package testData.repeatedObjectCreation;

class ObjectMethodParameterTest {
    private static final TestObject TESTOBJECT_CONSTANT = new TestObject("a");

    public void method1() {
		var1 = TESTOBJECT_CONSTANT;
		String a = "a";
		var2 = TESTOBJECT_CONSTANT;
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