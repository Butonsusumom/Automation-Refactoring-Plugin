package com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation;

import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.Getter;

@Getter
public enum ImplementationSmellType implements SpecificSmellType {

	CONTENT_COMPARISON_INSTEAD_OF_REFERENCE("Content comparison instead of reference"),
	REFERENCE_COMPARISON_INSTEAD_OF_CONTENT("Reference comparison instead of content"),
	OBJECT_METHOD_PARAMETER("Unnecessary object parameter"),
	CYCLOMATIC_COMPLEXITY("the permitted cyclomatic complexity"),
	REPEATED_OBJECT_CREATION("Repeated object creation"),
	LONG_METHOD("Long method");

	private final String value;

	ImplementationSmellType(String value) {
		this.value = value;
	}

}
