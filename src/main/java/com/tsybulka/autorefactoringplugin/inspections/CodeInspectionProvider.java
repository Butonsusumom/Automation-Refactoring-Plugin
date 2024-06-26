package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.MethodCyclomaticComplexityInspection;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.LongMethodInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterInspection;
import com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation.RepeatedObjectCreationInspection;
import com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality.ScatteredFunctionalityInspection;
import com.tsybulka.autorefactoringplugin.inspections.testmethodnaming.TestMethodNamingInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Collects all implemented inspections
 */
public class CodeInspectionProvider implements InspectionToolProvider {

	@NotNull
	@Override
	public Class[] getInspectionClasses() {
		return new Class[] {
				// Architecture
				ScatteredFunctionalityInspection.class,

				// Implementation
				RepeatedObjectCreationInspection.class,
				ObjectComparisonInspection.class,
				EnumComparisonInspection.class,
				ObjectMethodParameterInspection.class,
				MethodCyclomaticComplexityInspection.class,
				LongMethodInspection.class,

				// Test
				TestMethodNamingInspection.class
		};
	}
}