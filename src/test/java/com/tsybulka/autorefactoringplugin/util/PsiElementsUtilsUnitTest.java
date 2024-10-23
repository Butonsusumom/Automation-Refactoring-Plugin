package com.tsybulka.autorefactoringplugin.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PsiElementsUtilsUnitTest extends LightPlatformCodeInsightFixture4TestCase {

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
	}

	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void shouldReturnPackageName_whenGetPackageName_givenJavaClass() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java",
				"package com.example;\n" +
						"public class TestClass {}");

		String expected = "com.example";

		PsiClass psiClass = ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> psiJavaFile.getClasses()[0]);

		// when
		String actual = ApplicationManager.getApplication().runReadAction((Computable<String>) ()
				-> PsiElementsUtils.getPackageName(psiClass));

		// then
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void shouldReturnEmptyString_whenGetPackageName_givenPsiFile() {
		// given
		PsiClass psiClass = ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> {
			PsiElementFactory factory = PsiElementFactory.getInstance(getProject());
			return factory.createClass("TestClass");
		});

		// when
		String actual = ApplicationManager.getApplication().runReadAction((Computable<String>) () ->
				PsiElementsUtils.getPackageName(psiClass) // Should return empty string
		);
		// then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldReturnEmptyString_whenGetPackageName_givenNullClass() {
		// given

		// when
		String actual = PsiElementsUtils.getPackageName(null);

		// then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldReturnContainingMethodName_whenGetContainingMethodName_givenExpressionInClass() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java",
				"public class TestClass {\n" +
						"    public void testMethod() {\n" +
						"        int a = 1;\n" +
						"    }\n" +
						"}");

		PsiExpression expression  = ApplicationManager.getApplication().runReadAction((Computable<PsiExpression>) () -> {
			PsiClass psiClass = psiJavaFile.getClasses()[0];
			PsiMethod psiMethod = psiClass.getMethods()[0];
			PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) psiMethod.getBody().getStatements()[0];
			PsiLocalVariable localVariable = (PsiLocalVariable) declarationStatement.getDeclaredElements()[0];
			return localVariable.getInitializer();
		});

		String expected = "testMethod";

		// when
		String actual = ApplicationManager.getApplication().runReadAction((Computable<String>) () ->
				PsiElementsUtils.getContainingMethodName(expression)
		);

		// then
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void shouldReturnEmptyString_whenGetContainingMethodName_givenExpressionNotInAMethod() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java",
				"public class TestClass {\n" +
						"        int a = 1;\n" +
						"}");

		PsiExpression expression = ApplicationManager.getApplication().runReadAction((Computable<PsiExpression>) () -> {
			PsiClass psiClass = psiJavaFile.getClasses()[0];
			PsiField psiField = psiClass.getFields()[0]; // Get the field
			return psiField.getInitializer(); // Return the initializer of the field
		});

		// when
		String actual = ApplicationManager.getApplication().runReadAction((Computable<String>) () ->
				PsiElementsUtils.getContainingMethodName(expression)
		);

		// then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldReturnTrue_whenIsObjectType_givenNotEnumClass() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java",
				"public class TestClass {\n" +
						"    private String value;\n" +
						"}");

		PsiType objectType = ApplicationManager.getApplication().runReadAction((Computable<PsiType>) () -> {
			PsiClass psiClass = psiJavaFile.getClasses()[0]; // Get the class
			PsiField psiField = psiClass.getFields()[0]; // Get the field (String value)
			return psiField.getType(); // Get the field type, which is String
		});

		// when
		boolean result = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () ->
				PsiElementsUtils.isObjectType(objectType)
		);

		// then
		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalse_whenIsObjectType_givenEnumClass() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestEnum.java",
				"public enum TestEnum {\n" +
						"    VALUE1, VALUE2;\n" +
						"}");

		PsiType enumType = ApplicationManager.getApplication().runReadAction((Computable<PsiType>) () -> {
			PsiClass psiEnumClass = psiJavaFile.getClasses()[0]; // Get the enum class
			return JavaPsiFacade.getElementFactory(getProject()).createType(psiEnumClass); // Create a type from the enum
		});

		// when
		boolean result = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () ->
				PsiElementsUtils.isObjectType(enumType)
		);

		// then
		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrue_whenIsEnumType_givenEnumClass() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestEnum.java",
				"public enum TestEnum {\n" +
						"    VALUE1, VALUE2;\n" +
						"}");

		PsiType enumType = ApplicationManager.getApplication().runReadAction((Computable<PsiType>) () -> {
			PsiClass psiEnumClass = psiJavaFile.getClasses()[0]; // Get the enum class
			return JavaPsiFacade.getElementFactory(getProject()).createType(psiEnumClass);
		});

		// when
		boolean result = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () ->
				PsiElementsUtils.isEnumType(enumType)
		);

		// then
		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalse_whenIsEnumType_givenNotEnumType() {
		// given
		PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java",
				"public class TestClass {\n" +
						"    private int value;\n" +
						"}");

		PsiType nonEnumType = ApplicationManager.getApplication().runReadAction((Computable<PsiType>) () -> {
			PsiClass psiClass = psiJavaFile.getClasses()[0]; // Get a regular class (not an enum)
			return JavaPsiFacade.getElementFactory(getProject()).createType(psiClass);
		});

		// when
		boolean result = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () ->
				PsiElementsUtils.isEnumType(nonEnumType)
		);

		// then
		assertThat(result).isFalse();
	}
}