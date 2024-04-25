package com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture;

import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.Getter;

@Getter
public enum ArchitectureSmellType implements SpecificSmellType {

	SCATTERED_FUNCTIONALITY("Duplicated code across project");

	private final String value;

	ArchitectureSmellType(String value) {
		this.value = value;
	}

}
