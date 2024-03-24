package com.tsybulka.autorefactoringplugin.model.smell.codesmell;

import com.intellij.psi.PsiElement;
import com.tsybulka.autorefactoringplugin.model.smell.SpecificSmellType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public abstract class CodeSmell {

	private String name;
	private String classPackage;
	private String description;
	private SpecificSmellType smellType;
	private PsiElement psiElement;

}
