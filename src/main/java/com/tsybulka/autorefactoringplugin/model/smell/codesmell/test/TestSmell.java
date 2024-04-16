package com.tsybulka.autorefactoringplugin.model.smell.codesmell.test;

import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.CodeSmell;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TestSmell extends CodeSmell {

	private String className;
	private String methodName;

}
