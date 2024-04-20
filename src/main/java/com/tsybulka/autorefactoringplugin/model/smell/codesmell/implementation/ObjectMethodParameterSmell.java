package com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class ObjectMethodParameterSmell extends ImplementationSmell {

	private PsiField usedField;
	private PsiMethod method;

	public ObjectMethodParameterSmell(String name, String classPackage, String description, SpecificSmellType smellType, PsiElement psiElement, String className, String methodName, PsiField usedField, PsiMethod method) {
		super(name, classPackage, description, smellType, psiElement, className, methodName);
		this.usedField = usedField;
		this.method = method;
	}
}
