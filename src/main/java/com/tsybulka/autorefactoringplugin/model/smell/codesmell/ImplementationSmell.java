package com.tsybulka.autorefactoringplugin.model.smell.codesmell;

import com.intellij.psi.PsiElement;
import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class ImplementationSmell extends CodeSmell {

	private String className;
	private String methodName;

	public ImplementationSmell(String name, String classPackage, String description, SpecificSmellType smellType, PsiElement psiElement, String className, String methodName) {
		super(name, classPackage, description, smellType, psiElement);
		this.className = className;
		this.methodName = methodName;
	}
}