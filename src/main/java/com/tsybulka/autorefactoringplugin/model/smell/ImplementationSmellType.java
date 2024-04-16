package com.tsybulka.autorefactoringplugin.model.smell;

import lombok.Getter;

@Getter
public enum ImplementationSmellType implements SpecificSmellType{
	CONTENT_COMPARISON_INSTEAD_OF_REFERENCE("Content comparison instead of reference"),
	REFERENCE_COMPARISON_INSTEAD_OF_CONTENT("Reference comparison instead of content"),
	OBJECT_METHOD_PARAMETER("Unnecessary object parameter");

	private final String value;

	ImplementationSmellType(String value) {
		this.value = value;
	}

}
