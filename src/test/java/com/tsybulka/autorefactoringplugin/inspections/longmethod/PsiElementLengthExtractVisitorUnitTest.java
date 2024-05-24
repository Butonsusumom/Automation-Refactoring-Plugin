package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsiElementLengthExtractVisitorUnitTest {

	@Mock private PsiIfStatement mockIfStatement;
	@Mock private PsiWhileStatement mockWhileStatement;
	@Mock private PsiDoWhileStatement mockDoWhileStatement;
	@Mock private PsiForStatement mockForStatement;
	@Mock private PsiForeachStatement mockForeachStatement;
	@Mock private PsiSwitchStatement mockSwitchStatement;
	@Mock private PsiMethodCallExpression mockMethodCallExpression;
	@Mock private PsiConditionalExpression mockConditionalExpression;
	@Mock private PsiStatement mockStatement;
	@Mock private static ExtractMethodProcessor mockProcessor;

	private final PsiElementLengthExtractVisitor visitor =  spy(new PsiElementLengthExtractVisitor());
	private static MockedStatic<LongMethodDialogsProvider> mockedDialogsProvider;
	private static MockedStatic<ExtractMethodHandler> mockedExtractMethodHandler;

	@BeforeAll
	static void setUp() {
		mockedDialogsProvider = mockStatic(LongMethodDialogsProvider.class);
		mockedDialogsProvider.when(() -> LongMethodDialogsProvider.showIdentifyComplexElementDialog(any(), any())).thenReturn(true);
		mockedDialogsProvider.when(() -> LongMethodDialogsProvider.showIdentifyComplexElementsDialog(any(), any())).thenReturn(true);

		mockedExtractMethodHandler = mockStatic(ExtractMethodHandler.class);
		mockedExtractMethodHandler.when(() -> ExtractMethodHandler.getProcessor(any(), any(), any(), eq(false))).thenAnswer(invocation -> {
			System.out.println("ExtractMethodHandler.getProcessor called");
			return mockProcessor;
		});
		mockedExtractMethodHandler.when(() -> ExtractMethodHandler.invokeOnElements(any(), any(), any(), eq(true))).thenReturn(true);
	}

	@BeforeEach
	void setUpEach() {
		clearInvocations(mockProcessor);
	}

	@AfterAll
	static void tearDown() {
		mockedDialogsProvider.close();
		mockedExtractMethodHandler.close();
	}

	@Test
	void testVisitIfStatement() {
		visitor.visitIfStatement(mockIfStatement);

		verify(visitor).extract(mockIfStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitWhileStatement() {
		visitor.visitWhileStatement(mockWhileStatement);

		verify(visitor).extract(mockWhileStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitDoWhileStatement() {
		visitor.visitDoWhileStatement(mockDoWhileStatement);

		verify(visitor).extract(mockDoWhileStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitForStatement() {
		visitor.visitForStatement(mockForStatement);

		verify(visitor).extract(mockForStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitForeachStatement() {
		visitor.visitForeachStatement(mockForeachStatement);

		verify(visitor).extract(mockForeachStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitSwitchStatement() {
		visitor.visitSwitchStatement(mockSwitchStatement);

		verify(visitor).extract(mockSwitchStatement);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitMethodCallExpression() {
		visitor.visitMethodCallExpression(mockMethodCallExpression);

		verify(visitor).extract(mockMethodCallExpression);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitConditionalExpression() {
		visitor.visitConditionalExpression(mockConditionalExpression);

		verify(visitor).extract(mockConditionalExpression);
		assertTrue(visitor.isRefactored());
	}

	@Test
	void testVisitStatement() {
		visitor.visitStatement(mockStatement);

		verify(visitor).extract(mockStatement);
		assertTrue(visitor.isRefactored());
	}

}
