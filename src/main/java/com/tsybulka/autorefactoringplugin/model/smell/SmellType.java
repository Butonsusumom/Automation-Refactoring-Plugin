package com.tsybulka.autorefactoringplugin.model.smell;

import lombok.Getter;

@Getter
public enum SmellType {
	ARCHITECTURE("Architecture"),
	IMPLEMENTATION("Implementation"),
	TEST("Test");

	private final String value;

	SmellType(String value) {
		this.value = value;
	}

}
