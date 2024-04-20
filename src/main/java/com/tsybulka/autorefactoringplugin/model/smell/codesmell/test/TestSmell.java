package com.tsybulka.autorefactoringplugin.model.smell.codesmell.test;

import com.intellij.psi.PsiElement;
import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
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

	public TestSmell(String name, String classPackage, String description, SpecificSmellType smellType, PsiElement psiElement, String className, String methodName) {
		super(name, classPackage, description, smellType, psiElement);
		this.className = className;
		this.methodName = methodName;
	}

}
