package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestMethodNamingVisitorUnitTest {

	private MockedStatic<PluginSettings> mockedPluginSettings;
	private TestMethodNamingVisitor visitor;
	private List<TestSmell> testSmells;
	@Mock
	private PsiMethod mockMethod;
	@Mock
	private PsiAnnotation testAnnotation;

	@BeforeEach
	void setUp() {
		mockedPluginSettings = mockStatic(PluginSettings.class);
		PluginSettings mockSettings = mock(PluginSettings.class);
		when(mockSettings.getTestMethodNamingRegExp()).thenReturn("should[A-Z].*");
		mockedPluginSettings.when(PluginSettings::getInstance).thenReturn(mockSettings);

		testSmells = new ArrayList<>();
		visitor = new TestMethodNamingVisitor(testSmells);

		when(mockMethod.getAnnotations()).thenReturn(new PsiAnnotation[]{testAnnotation});
	}

	@AfterEach
	void tearDown() {
		mockedPluginSettings.close();  // Important: Close the static mock
	}

	@Test
	void testMethodNaming_NotTestMethod_ShouldNotRegisterSmell() {
		visitor.visitMethod(mockMethod);
		assertTrue(testSmells.isEmpty(), "No smells should be registered for conforming names.");
	}

	@Test
	void testMethodNaming_ConformsToPattern_ShouldNotRegisterSmell() {
		when(testAnnotation.getQualifiedName()).thenReturn("org.junit.Test");
		when(mockMethod.getName()).thenReturn("shouldTestSomething");
		visitor.visitMethod(mockMethod);
		assertTrue(testSmells.isEmpty(), "No smells should be registered for conforming names.");
	}

	@Test
	void testMethodNaming_ConformsToPatternTestNg_ShouldNotRegisterSmell() {
		when(testAnnotation.getQualifiedName()).thenReturn("org.testng.annotations.Test");
		when(mockMethod.getName()).thenReturn("shouldCallMethod");
		visitor.visitMethod(mockMethod);
		assertTrue(testSmells.isEmpty(), "No smells should be registered for conforming names.");
	}

	@Test
	void testMethodNaming_ConformsToPatternParameterizedTest_ShouldNotRegisterSmell() {
		when(testAnnotation.getQualifiedName()).thenReturn("org.junit.jupiter.api.ParameterizedTest");
		when(mockMethod.getName()).thenReturn("shouldCallMethod");
		visitor.visitMethod(mockMethod);
		assertTrue(testSmells.isEmpty(), "No smells should be registered for conforming names.");
	}

	@Test
	void testMethodNaming_ConformsToPatternRepeatedTest_ShouldNotRegisterSmell() {
		when(testAnnotation.getQualifiedName()).thenReturn("org.junit.jupiter.api.RepeatedTest");
		when(mockMethod.getName()).thenReturn("shouldCallMethod");
		visitor.visitMethod(mockMethod);
		assertTrue(testSmells.isEmpty(), "No smells should be registered for conforming names.");
	}

	@Test
	void testMethodNaming_DoesNotConformToPattern_ShouldRegisterSmell() {
		when(testAnnotation.getQualifiedName()).thenReturn("org.junit.jupiter.api.Test");
		when(mockMethod.getName()).thenReturn("testSomething");
		visitor.visitMethod(mockMethod);
		assertFalse(testSmells.isEmpty(), "Smells should be registered for non-conforming names.");
	}
}
