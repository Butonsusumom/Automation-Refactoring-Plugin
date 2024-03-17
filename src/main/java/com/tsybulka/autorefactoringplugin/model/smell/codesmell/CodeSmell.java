package com.tsybulka.autorefactoringplugin.model.smell.codesmell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class CodeSmell {

	private String name;
	private String classPackage;
	private String description;
	private String project;
	private String smellType;

}
