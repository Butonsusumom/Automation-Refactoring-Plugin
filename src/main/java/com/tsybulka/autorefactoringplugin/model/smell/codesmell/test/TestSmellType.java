package com.tsybulka.autorefactoringplugin.model.smell.codesmell.test;

import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.Getter;

@Getter
public enum TestSmellType implements SpecificSmellType {

	TEST_METHOD_NAMING("Test method naming convention not followed");

	private final String value;

	TestSmellType(String value) {
		this.value = value;
	}

}
