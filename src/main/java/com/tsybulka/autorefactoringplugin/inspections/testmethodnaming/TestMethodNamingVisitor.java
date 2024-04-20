package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmellType;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;

import java.util.List;
import java.util.regex.Pattern;

public class TestMethodNamingVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.test.method.name.display.name");

	private List<TestSmell> smellsList;

	public TestMethodNamingVisitor(List<TestSmell> smellsList) {
		this.smellsList = smellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return true;
	}

	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			String namingRegExp = PluginSettings.getInstance().getTestMethodNamingRegExp();
			if (isTestMethod(method)) {
				Pattern pattern = Pattern.compile(namingRegExp);
				String methodName = method.getName();
				if (!pattern.matcher(methodName).matches()) {
					registerSmell(method);
				}
			}
		}
	}

	private boolean isTestMethod(PsiMethod method) {
		PsiAnnotation[] annotations = method.getAnnotations();
		for (PsiAnnotation annotation : annotations) {
			String annotationQualifiedName = annotation.getQualifiedName();
			if ("org.junit.Test".equals(annotationQualifiedName) ||
					"org.junit.jupiter.api.Test".equals(annotationQualifiedName) ||
					"org.junit.jupiter.api.ParameterizedTest".equals(annotationQualifiedName) ||
					"org.junit.jupiter.api.RepeatedTest".equals(annotationQualifiedName) ||
					"org.testng.annotations.Test".equals(annotationQualifiedName)) {
				return true;
			}
		}
		return false;
	}

	void registerSmell(PsiMethod method) {
		// Find the containing class of the expression
		String namingRegExp = PluginSettings.getInstance().getTestMethodNamingRegExp();
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String className = "";
		String packageName = "";
		String methodName = method.getName();

		if (containingClass != null) {
			className = containingClass.getName();
			// Find the package name of the class
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				packageName = ((PsiJavaFile) containingFile).getPackageName();
			}
		}

		smellsList.add(new TestSmell(NAME, packageName, InspectionsBundle.message("inspection.test.method.name.problem.descriptor", namingRegExp), TestSmellType.TEST_METHOD_NAMING, method.getNameIdentifier(), className, methodName));
	}

}
