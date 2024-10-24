package com.tsybulka.autorefactoringplugin.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;

/**
 * Utility class for working with IntelliJ PSI elements.
 */
public class PsiElementsUtils {

	/**
	 * Retrieves the package name of the class containing the provided {@link PsiClass}.
	 *
	 * @param containingClass the {@link PsiClass} whose package name is to be retrieved
	 * @return the package name if available, otherwise an empty string
	 */
	public static String getPackageName(PsiClass containingClass) {
		if (containingClass != null) {
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				return ((PsiJavaFile) containingFile).getPackageName();
			}
		}
		return "";
	}

	/**
	 * Retrieves the name of the method that contains the given {@link PsiExpression}.
	 *
	 * @param expression the {@link PsiExpression} whose containing method's name is to be retrieved
	 * @return the method name if found, otherwise an empty string
	 */
	public static String getContainingMethodName(PsiExpression expression) {
		PsiMethod containingMethod = PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
		return containingMethod != null ? containingMethod.getName() : "";
	}

	/**
	 * Checks if the given {@link PsiType} represents a class type that is not an enum.
	 *
	 * @param type the {@link PsiType} to check
	 * @return true if the type is a class type and not an enum, false otherwise
	 */
	public static boolean isObjectType(PsiType type) {
		return type instanceof PsiClassType && !isEnumType(type);
	}

	/**
	 * Checks if the given {@link PsiType} represents an enum type.
	 *
	 * @param type the {@link PsiType} to check
	 * @return true if the type is an enum, false otherwise
	 */
	public static boolean isEnumType(PsiType type) {
		PsiClass psiClass = PsiUtil.resolveClassInType(type);
		return psiClass != null && psiClass.isEnum();
	}

}
