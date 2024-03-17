package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Collects all implemented inspections
 */
public class CodeInspectionProvider implements InspectionToolProvider {

	@NotNull
	@Override
	public Class[] getInspectionClasses() {
		return new Class[]{
				ObjectComparisonInspection.class,
				EnumComparisonInspection.class
		};
	}
}