package com.tsybulka.autorefactoringplugin.model.smell;

public enum SmellType {
	ARCHITECTURE("Architecture"),
	IMPLEMENTATION("Implementation"),
	TEST("Test");

	private final String value;

	SmellType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
