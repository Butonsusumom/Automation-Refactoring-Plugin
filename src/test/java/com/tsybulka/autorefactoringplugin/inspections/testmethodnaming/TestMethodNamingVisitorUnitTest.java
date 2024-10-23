package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmellType;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestMethodNamingVisitorUnitTest extends LightPlatformCodeInsightFixture4TestCase {

	private TestMethodNamingVisitor classUnderTest;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();

		MockedStatic<PluginSettings> mockedSettings = Mockito.mockStatic(PluginSettings.class);
		PluginSettings pluginSettings = mock(PluginSettings.class);

		when(PluginSettings.getInstance()).thenReturn(pluginSettings);
		when(pluginSettings.getTestMethodNamingRegExp()).thenReturn("test[A-Za-z]+");

		classUnderTest = spy(new TestMethodNamingVisitor());

		mockedSettings.close();
	}

	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void shouldReturnTrue_whenIsInspectionEnabled_givenNothing() {
		//given

		//when
		boolean actual = classUnderTest.isInspectionEnabled();

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldRegisterSmell_whenVisitMethod_givenMethodNameDoesNotMatchPattern() {
		// given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		PsiMethod expectedMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		TestSmell expectedSmell = new TestSmell(
				"Test method naming convention not followed",
				"",
				"Test names should follow the \"test[A-Za-z]+\" format to describe the tested method.",
				TestSmellType.TEST_METHOD_NAMING,
				ApplicationManager.getApplication().runReadAction((Computable<PsiIdentifier>) expectedMethod::getNameIdentifier),
				"DummyClass",
				"badMethodName");

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.visitMethod(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).hasSize(1).first().usingRecursiveComparison().isEqualTo(expectedSmell);
		verify(classUnderTest).registerSmell(givenMethod);
	}

	@Test
	public void shouldNotRegisterSmell_whenVisitMethod_givenMethodNameMatchesPattern() {
		// given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void testValidMethodName() {}");

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.visitMethod(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).isEmpty();
		verify(classUnderTest, never()).registerSmell(givenMethod);
	}

	@Test
	public void shouldNotRegisterSmell_whenVisitMethod_givenInspectionIsDisabled() {
		// given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void testValidMethodName() {}");

		when(classUnderTest.isInspectionEnabled()).thenReturn(false);

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.visitMethod(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).isEmpty();
		verify(classUnderTest, never()).registerSmell(givenMethod);
	}

	@Test
	public void shouldNotRegisterSmell_whenVisitMethod_givenMethodIsNotTestMethod() {
		// given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("public void notATestMethod() {}");

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.visitMethod(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).isEmpty();
		verify(classUnderTest, never()).registerSmell(givenMethod);
	}

	@Test
	public void shouldRegisterSmellWithClassName_whenRegisterSmell_givenMethodHasContainingClass() {
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		PsiMethod expectedMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		TestSmell expectedSmell = new TestSmell(
				"Test method naming convention not followed",
				"",
				"Test names should follow the \"test[A-Za-z]+\" format to describe the tested method.",
				TestSmellType.TEST_METHOD_NAMING,
				ApplicationManager.getApplication().runReadAction((Computable<PsiIdentifier>) expectedMethod::getNameIdentifier),
				"DummyClass",
				"badMethodName");

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.registerSmell(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).hasSize(1).first().usingRecursiveComparison().isEqualTo(expectedSmell);

		assertThat(smellsList).isEmpty();
	}

	@Test
	public void shouldRegisterSmellWithoutClassName_whenRegisterSmell_givenMethodHasNoContainingClass() {
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		PsiMethod expectedMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void badMethodName() {}");

		TestSmell expectedSmell = new TestSmell(
				"Test method naming convention not followed",
				"",
				"Test names should follow the \"test[A-Za-z]+\" format to describe the tested method.",
				TestSmellType.TEST_METHOD_NAMING,
				ApplicationManager.getApplication().runReadAction((Computable<PsiIdentifier>) expectedMethod::getNameIdentifier),
				"",
				"badMethodName");

		// when
		ApplicationManager.getApplication().runReadAction(()->classUnderTest.registerSmell(givenMethod));

		// then
		List<TestSmell> smellsList = classUnderTest.getSmellsList();

		assertThat(smellsList).hasSize(1).first().usingRecursiveComparison().isEqualTo(expectedSmell);

		assertThat(smellsList).isEmpty();
	}

	@Test
	public void shouldReturnTrue_whenIsTestMethod_givenJUnit4TestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.Test public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnTrue_whenIsTestMethod_givenJUnit5TestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.Test public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnTrue_whenIsTestMethod_givenParameterizedTestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.ParameterizedTest public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnTrue_whenIsTestMethod_givenRepeatedTestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.junit.jupiter.api.RepeatedTest public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnTrue_whenIsTestMethod_givenTestNGTestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("@org.testng.annotations.Test public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnFalse_whenIsTestMethod_givenNoTestAnnotation() {
		//given
		PsiMethod givenMethod = getPsiMethodFromTextWithClass("public void myTest() {}");

		//when
		boolean actual = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> classUnderTest.isTestMethod(givenMethod));

		//then
		assertThat(actual).isFalse();
	}

	private PsiMethod getPsiMethodFromTextWithClass(String methodText) {
		String classText = "class DummyClass { " + methodText + " }";
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("DummyClass.java", classText);
		PsiClass psiClass
				= ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> psiJavaFile.getClasses()[0]);
		return ApplicationManager.getApplication().runReadAction((Computable<PsiMethod>) () -> psiClass.getMethods()[0]);
	}

	private PsiMethod getPsiMethodFromTextWithoutClass(String methodText) {
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("DummyClass.java", methodText);
		PsiClass psiClass
				= ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> psiJavaFile.getClasses()[0]);
		return ApplicationManager.getApplication().runReadAction((Computable<PsiMethod>) () -> psiClass.getMethods()[0]);
	}

}