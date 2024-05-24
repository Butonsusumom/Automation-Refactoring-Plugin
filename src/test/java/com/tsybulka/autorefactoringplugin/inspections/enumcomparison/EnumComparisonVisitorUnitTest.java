package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnumComparisonVisitorUnitTest {

	@Mock private PsiMethodCallExpression mockMethodCallExpression;
	@Mock private PsiReferenceExpression mockReferenceExpression;
	@Mock private PsiMethod mockMethod;
	@Mock private PsiClass mockContainingClass;
	@Mock private PsiExpression mockArgument;
	@Mock private PsiType mockType;
	@Mock private PsiFile mockFile;
	@Mock private PsiJavaFile mockJavaFile;
	@Mock private PsiMethod mockContainingMethod;
	@Mock private PsiExpressionList mockExpressionList;

	private final List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
	private final EnumComparisonVisitor visitor = spy(new EnumComparisonVisitor(implementationSmellsList));

	private static MockedStatic<ApplicationManager> mockedApplicationManager;

	@BeforeAll
	static void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		Application mockApplication = mock(Application.class);
		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);

		PluginSettings mockSettings = mock(PluginSettings.class);
		when(mockApplication.getService(PluginSettings.class)).thenReturn(mockSettings);
		when(mockSettings.isEnumComparisonCheck()).thenReturn(true);
	}

	@BeforeEach
	void resetMocks() {
		reset(mockMethodCallExpression, mockReferenceExpression, mockMethod, mockContainingClass, mockArgument, mockType, mockFile, mockJavaFile, mockContainingMethod, mockExpressionList);
		implementationSmellsList.clear();
	}

	@AfterAll
	static void tearDownClass() {
		mockedApplicationManager.close();
	}

	@Test
	void testVisitMethodCallExpression_EqualsMethod() {
		when(mockMethodCallExpression.getMethodExpression()).thenReturn(mockReferenceExpression);
		when(mockReferenceExpression.getReferenceName()).thenReturn("equals");
		when(mockMethodCallExpression.resolveMethod()).thenReturn(mockMethod);
		when(mockMethod.getContainingClass()).thenReturn(mockContainingClass);
		when(mockContainingClass.getQualifiedName()).thenReturn("java.util.Objects");
		when(mockMethodCallExpression.getArgumentList()).thenReturn(mockExpressionList);

		PsiExpression[] mockArguments = {mockArgument};
		when(mockExpressionList.getExpressions()).thenReturn(mockArguments);
		when(mockArgument.getType()).thenReturn(mockType);
		when(visitor.isEnum(mockType)).thenReturn(true);

		visitor.visitMethodCallExpression(mockMethodCallExpression);

		verify(visitor).registerSmell(mockMethodCallExpression);
		assertEquals(1, implementationSmellsList.size());
	}

	@Test
	void testVisitMethodCallExpression_EqualsMethod_NonEnum() {
		when(mockMethodCallExpression.getMethodExpression()).thenReturn(mockReferenceExpression);
		when(mockReferenceExpression.getReferenceName()).thenReturn("equals");
		when(mockMethodCallExpression.resolveMethod()).thenReturn(mockMethod);
		when(mockMethod.getContainingClass()).thenReturn(mockContainingClass);
		when(mockContainingClass.getQualifiedName()).thenReturn("java.util.Objects");
		when(mockMethodCallExpression.getArgumentList()).thenReturn(mockExpressionList);

		PsiExpression[] mockArguments = {mockArgument};
		when(mockExpressionList.getExpressions()).thenReturn(mockArguments);
		when(mockArgument.getType()).thenReturn(mockType);
		when(visitor.isEnum(mockType)).thenReturn(false);

		visitor.visitMethodCallExpression(mockMethodCallExpression);

		verify(visitor, never()).registerSmell(mockMethodCallExpression);
		assertTrue(implementationSmellsList.isEmpty());
	}

	@Test
	void testRegisterSmell() {
		when(mockContainingClass.getName()).thenReturn("MockClass");
		when(mockContainingClass.getContainingFile()).thenReturn(mockJavaFile);
		when((mockJavaFile).getPackageName()).thenReturn("com.mock.package");
		when(PsiTreeUtil.getParentOfType(mockMethodCallExpression, PsiClass.class)).thenReturn(mockContainingClass);
		when(PsiTreeUtil.getParentOfType(mockMethodCallExpression, PsiMethod.class)).thenReturn(mockContainingMethod);
		when(mockContainingMethod.getName()).thenReturn("mockMethod");

		visitor.registerSmell(mockMethodCallExpression);

		assertEquals(1, implementationSmellsList.size());
		ImplementationSmell smell = implementationSmellsList.get(0);
		assertEquals("MockClass", smell.getClassName());
		assertEquals("com.mock.package", smell.getClassPackage());
		assertEquals("mockMethod", smell.getMethodName());
		assertEquals(ImplementationSmellType.CONTENT_COMPARISON_INSTEAD_OF_REFERENCE, smell.getSmellType());
	}
}
