package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.util.PsiElementsUtils;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmellType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Visitor class that inspects methods in the codebase to ensure they conform to the naming
 * conventions for test methods. This class checks if a method is a test method based on its annotations
 * and verifies that the method name matches the regular expression defined for test methods in the plugin settings.
 * If a method does not follow the specified naming convention, it is registered as a "code smell" for further analysis.
 *
 * @see BaseCodeInspectionVisitor
 */
@Getter
public class TestMethodNamingVisitor extends BaseCodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.test.method.name.display.name");

	private List<TestSmell> smellsList = new ArrayList<>();

	/**
	 * Always enabled
	 */
	@Override
	public boolean isInspectionEnabled() {
		return true;
	}

	/**
	 * Visits a {@link PsiMethod} and checks if it conforms to the specified naming convention
	 * for test methods.
	 * <p>
	 * This method is called during the inspection process to evaluate the naming of test methods
	 * in the codebase. It retrieves the regular expression for valid test method names from the
	 * plugin settings and compares it against the name of the given method. If the method is a
	 * recognized test method but does not match the expected naming convention, it registers a
	 * code smell for further analysis.
	 * </p>
	 *
	 * @param method the method to be inspected; must not be null.
	 */
	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			String namingRegExp = settings.getTestMethodNamingRegExp();
			if (isTestMethod(method)) {
				Pattern pattern = Pattern.compile(namingRegExp);
				String methodName = method.getName();
				if (!pattern.matcher(methodName).matches()) {
					registerSmell(method);
				}
			}
		}
	}

	/**
	 * Checks if the given method is a test method based on its annotations.
	 * <p>
	 * This method inspects the annotations of the provided {@link PsiMethod} to determine
	 * if it is a recognized test method. It checks for annotations from various testing
	 * frameworks, including JUnit 4, JUnit 5 (both standard and parameterized tests),
	 * and TestNG.
	 * </p>
	 *
	 * @param method the method to check for test annotations; must not be null.
	 * @return {@code true} if the method is annotated as a test method; {@code false} otherwise.
	 */
	@VisibleForTesting
	void registerSmell(PsiMethod method) {
		String testNamingRegExp = settings.getTestMethodNamingRegExp();
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String packageName = PsiElementsUtils.getPackageName(containingClass);
		String methodName = method.getName();
		String className = "";

		if (containingClass != null) {
			className = containingClass.getName();
		}

		smellsList.add(new TestSmell(NAME, packageName, InspectionsBundle.message("inspection.test.method.name.problem.descriptor", testNamingRegExp), TestSmellType.TEST_METHOD_NAMING, method.getNameIdentifier(), className, methodName));
	}

	/**
	 * Checks if the given method is a test method based on its annotations.
	 * <p>
	 * This method inspects the annotations of the provided {@link PsiMethod} to determine
	 * if it is a recognized test method. It checks for annotations from various testing
	 * frameworks, including JUnit 4, JUnit 5 (both standard and parameterized tests),
	 * and TestNG.
	 * </p>
	 *
	 * @param method the method to check for test annotations; must not be null.
	 * @return {@code true} if the method is annotated as a test method; {@code false} otherwise.
	 */
	@VisibleForTesting
	boolean isTestMethod(PsiMethod method) {
		PsiAnnotation[] annotations = method.getAnnotations();
		for (PsiAnnotation annotation : annotations) {
			String annotationQualifiedName = annotation.getQualifiedName();
			if ("org.junit.Test".equals(annotationQualifiedName) || "org.junit.jupiter.api.Test".equals(annotationQualifiedName) || "org.junit.jupiter.api.ParameterizedTest".equals(annotationQualifiedName) || "org.junit.jupiter.api.RepeatedTest".equals(annotationQualifiedName) || "org.testng.annotations.Test".equals(annotationQualifiedName)) {
				return true;
			}
		}
		return false;
	}

}
