package com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture;

import com.intellij.psi.PsiElement;
import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.CodeSmell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ArchitectureSmell extends CodeSmell {

	private Set<PsiElement> psiElements;
	private String className;
	private String methodName;

	public ArchitectureSmell(String name, String classPackage, String description, SpecificSmellType smellType, Set<PsiElement> psiElements, String className, String methodName) {
		super(name, classPackage, description, smellType, psiElements.iterator().next());
		this.psiElements = psiElements;
		this.className = className;
		this.methodName = methodName;
	}
}
