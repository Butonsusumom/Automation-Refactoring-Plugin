package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.util.PsiElementsUtils;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import lombok.Getter;

import java.util.*;

/**
 * Visitor class for inspecting methods for implementation smells related to
 * object parameters, specifically when only a single field of the object
 * is accessed.
 */
@Getter
public class ObjectMethodParameterVisitor extends BaseCodeInspectionVisitor {

	private static final String GET_METHOD_PREFIX = "get";
	private static final String IS_METHOD_PREFIX = "is";

	private static final String NAME = InspectionsBundle.message("inspection.object.parameter.display.name");

	private List<ImplementationSmell> smellsList = new ArrayList<>();

	public ObjectMethodParameterVisitor(List<ImplementationSmell> smellsList) {
		this.smellsList = smellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isObjectMethodParameterCheck();
	}

	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			super.visitMethod(method);
			analyzeMethodParameters(method);
		}
	}

	/**
	 * Analyzes the parameters of the specified method to identify if they are
	 * object parameters with only a single accessed field.
	 *
	 * @param method The method being inspected.
	 */
	private void analyzeMethodParameters(PsiMethod method) {
		PsiParameterList parameterList = method.getParameterList();
		for (PsiParameter parameter : parameterList.getParameters()) {
			if (!(parameter.getType() instanceof PsiPrimitiveType)) {
				Set<PsiField> accessedProperties = retrieveAccessedProperties(method, parameter);
				checkForSmell(method, accessedProperties, parameter);
			}
		}
	}

	/**
	 * Analyzes the method body to determine which properties are accessed
	 * from the given parameter.
	 *
	 * @param method   The method being inspected.
	 * @param parameter The parameter to analyze.
	 * @return A set of accessed properties as PsiField objects.
	 */
	private Set<PsiField> retrieveAccessedProperties(PsiMethod method, PsiParameter parameter) {
		Set<PsiField> accessedProperties = new HashSet<>();
		Set<String> methodCalls = new HashSet<>();

		method.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitReferenceExpression(PsiReferenceExpression expression) {
				super.visitReferenceExpression(expression);
				handleReferenceExpression(expression, parameter, accessedProperties, methodCalls);
			}
		});

		return accessedProperties;
	}

	/**
	 * Handles the reference expressions found in the method body, updating
	 * the accessed properties and method calls accordingly.
	 *
	 * @param expression      The reference expression being visited.
	 * @param parameter       The parameter being analyzed.
	 * @param accessedProperties A set to collect accessed properties.
	 * @param methodCalls     A set to collect method calls.
	 */
	private void handleReferenceExpression(PsiReferenceExpression expression, PsiParameter parameter,
										   Set<PsiField> accessedProperties, Set<String> methodCalls) {
		PsiElement resolved = expression.resolve();
		if (resolved instanceof PsiField && PsiElementsUtils.isParameterOfObject(parameter, resolved)) {
			accessedProperties.add((PsiField) resolved);
		} else if (resolved instanceof PsiMethod) {
			handleMethodReference((PsiMethod) resolved, parameter, accessedProperties, methodCalls);
		}
	}

	/**
	 * Handles method references found in the method body, checking if they
	 * are getter methods and updating accessed properties accordingly.
	 *
	 * @param resolvedMethod  The resolved method reference.
	 * @param parameter       The parameter being analyzed.
	 * @param accessedProperties A set to collect accessed properties.
	 * @param methodCalls     A set to collect method calls.
	 */
	private void handleMethodReference(PsiMethod resolvedMethod, PsiParameter parameter,
									   Set<PsiField> accessedProperties, Set<String> methodCalls) {
		if (PsiElementsUtils.isGetterMethod(resolvedMethod)) {
			PsiField objectField = resolveGetterField(resolvedMethod);
			if (PsiElementsUtils.isParameterOfObject(parameter, objectField)) {
				accessedProperties.add(objectField);
			}
		} else {
			methodCalls.add(resolvedMethod.getName());
		}
	}

	/**
	 * Checks if the method can be refactored based on the accessed properties
	 * and registers a smell if appropriate.
	 *
	 * @param method           The method being inspected.
	 * @param accessedProperties A set of accessed properties.
	 * @param parameter        The parameter being analyzed.
	 */
	private void checkForSmell(PsiMethod method, Set<PsiField> accessedProperties, PsiParameter parameter) {
		if (accessedProperties.size() == 1) {
			PsiField property = accessedProperties.iterator().next();
			registerSmell(method, property, parameter);
		}
	}

	/**
	 * Resolves the field corresponding to a getter method.
	 *
	 * @param getterMethod The getter method to resolve.
	 * @return The corresponding field, or null if not found.
	 */
	private PsiField resolveGetterField(PsiMethod getterMethod) {
		if (PsiElementsUtils.isGetterMethod(getterMethod)) {
			String propertyName = PsiElementsUtils.getPropertyNameFromGetter(getterMethod);
			return findFieldByName(getterMethod.getContainingClass(), propertyName);
		}
		return null;
	}

	/**
	 * Finds a field by its name within the specified class.
	 *
	 * @param containingClass The class containing the field.
	 * @param fieldName      The name of the field to find.
	 * @return The corresponding field, or null if not found.
	 */
	private PsiField findFieldByName(PsiClass containingClass, String fieldName) {
		if (containingClass != null) {
			for (PsiField field : containingClass.getAllFields()) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * Registers an implementation smell for the given method, property, and parameter.
	 *
	 * @param method   The method associated with the smell.
	 * @param property The property that is accessed.
	 * @param parameter The parameter that triggers the smell.
	 */
	private void registerSmell(PsiMethod method, PsiField property, PsiParameter parameter) {
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String className = containingClass != null ? containingClass.getName() : "";
		String packageName = PsiElementsUtils.getPackageName(containingClass);
		String methodName = method.getName();

		smellsList.add(new ImplementationSmell(NAME, packageName,
				InspectionsBundle.message("inspection.object.parameter.problem.descriptor", property.getName()),
				ImplementationSmellType.OBJECT_METHOD_PARAMETER, parameter, className, methodName));
	}
}