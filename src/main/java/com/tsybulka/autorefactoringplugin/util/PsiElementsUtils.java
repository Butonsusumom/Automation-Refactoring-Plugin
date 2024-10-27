package com.tsybulka.autorefactoringplugin.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

/**
 * Utility class for working with IntelliJ PSI elements.
 */
public class PsiElementsUtils {

	private static final String GET_METHOD_PREFIX = "get";
	private static final String IS_METHOD_PREFIX = "is";

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

	/**
	 * Collects all {@link PsiClass} instances from the source root directories of the given project.
	 *
	 * <p>This method retrieves the source root directories and recursively searches through them
	 * to find all PsiClasses. The collected classes are stored in a set to avoid duplicates.</p>
	 *
	 * @param project the project from which to collect PsiClasses
	 * @return a set of PsiClass instances found in the source directories
	 */
	public static Set<PsiClass> collectPsiClassesFromSrc(Project project) {
		Set<PsiClass> psiClasses = new HashSet<>();
		VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();
		for (VirtualFile sourceRoot : sourceRoots) {
			PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(sourceRoot);
			if (psiDirectory != null) {
				collectPsiClassesRecursively(psiDirectory, psiClasses);
			}
		}
		return psiClasses;
	}

	/**
	 * Checks if the method is a getter method.
	 *
	 * @param method The method to check.
	 * @return True if the method is a getter, false otherwise.
	 */
	public static boolean isGetterMethod(PsiMethod method) {
		String name = method.getName();
		return method.getParameterList().isEmpty() &&
				(name.startsWith(GET_METHOD_PREFIX) || name.startsWith(IS_METHOD_PREFIX))
				&& method.getReturnType() != null;
	}

	/**
	 * Extracts the property name from a getter method name.
	 *
	 * @param method The getter method.
	 * @return The property name or null if not a valid getter.
	 */
	public static String getPropertyNameFromGetter(PsiMethod method) {
		String name = method.getName();
		if (name.startsWith(GET_METHOD_PREFIX)) {
			return StringUtil.decapitalize(name.substring(3));
		} else if (name.startsWith(IS_METHOD_PREFIX)) {
			return StringUtil.decapitalize(name.substring(2));
		}
		return null;
	}

	/**
	 * Checks if the resolved element is a field of the parameter's type.
	 *
	 * @param parameter       the parameter to check
	 * @param resolvedElement the resolved element to check against
	 * @return true if the resolved element is a field of the parameter's type, false otherwise
	 */
	public static boolean isParameterOfObject(PsiParameter parameter, PsiElement resolvedElement) {
		PsiType parameterType = parameter.getType();
		if (parameterType instanceof PsiClassType) {
			PsiClass parameterClass = ((PsiClassType) parameterType).resolve();
			if (parameterClass != null) {
				return Arrays.stream(parameterClass.getAllFields())
						.anyMatch(field -> Objects.equals(field, resolvedElement));
			}
		}
		return false;
	}

	/**
	 * Collects PsiClass instances from the specified PsiDirectory and adds them to the provided set.
	 *
	 * @param directory  the PsiDirectory to search for PsiClasses
	 * @param psiClasses the set to which found PsiClasses will be added
	 */
	private static void collectPsiClassesRecursively(PsiDirectory directory, Set<PsiClass> psiClasses) {
		for (PsiFile psiFile : directory.getFiles()) {
			// Check if the file is a Java file
			if (psiFile instanceof PsiJavaFile) {
				PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
				Collections.addAll(psiClasses, psiJavaFile.getClasses());
			}
		}
		for (PsiDirectory subdirectory : directory.getSubdirectories()) {
			collectPsiClassesRecursively(subdirectory, psiClasses);
		}
	}

}
