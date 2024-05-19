package com.tsybulka.autorefactoringplugin.inspections;


import com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.MethodCyclomaticComplexityInspection;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.LongMethodInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonInspection;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterInspection;
import com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation.RepeatedObjectCreationInspection;
import com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality.ScatteredFunctionalityInspection;
import com.tsybulka.autorefactoringplugin.inspections.testmethodnaming.TestMethodNamingInspection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.intellij.codeInspection.InspectionToolProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeInspectionProviderUnitTest {

	@Test
	void testGetInspectionClasses() {
		InspectionToolProvider provider = new CodeInspectionProvider();
		Class<?>[] classes = provider.getInspectionClasses();

		// Assert that all expected inspection classes are present
		assertNotNull(classes, "The array of inspection classes should not be null.");
		assertTrue(classes.length > 0, "The array of inspection classes should not be empty.");

		// List all expected classes
		Class<?>[] expectedClasses = {
				ScatteredFunctionalityInspection.class,
				RepeatedObjectCreationInspection.class,
				ObjectComparisonInspection.class,
				EnumComparisonInspection.class,
				ObjectMethodParameterInspection.class,
				MethodCyclomaticComplexityInspection.class,
				LongMethodInspection.class,
				TestMethodNamingInspection.class
		};

		// Check if all expected classes are in the returned array
		for (Class<?> expectedClass : expectedClasses) {
			assertTrue(containsClass(classes, expectedClass),
					"The array should contain " + expectedClass.getSimpleName());
		}
	}

	private boolean containsClass(Class<?>[] classes, Class<?> expectedClass) {
		for (Class<?> cls : classes) {
			if (cls.equals(expectedClass)) {
				return true;
			}
		}
		return false;
	}
}
