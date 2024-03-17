package com.tsybulka.autorefactoringplugin.model.smell.codesmell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImplementationSmell extends CodeSmell {

	private String className;
	private String methodName;

}