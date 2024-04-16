package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterInspection;

/**
 * Collects all implemented inspections
 */
public class CodeInspectionProvider implements InspectionToolProvider {

	@Override
	public Class[] getInspectionClasses() {
		return new Class[] {
				// Architecture

				// Implementation
				ObjectComparisonInspection.class,
				EnumComparisonInspection.class,
				ObjectMethodParameterInspection.class

				// Test
		};
	}
}