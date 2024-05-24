package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity;

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
class PsiElementCyclomaticExtractVisitorUnitTest {

	@Mock private PsiIfStatement mockIfStatement;
	@Mock private PsiWhileStatement mockWhileStatement;
	@Mock private PsiDoWhileStatement mockDoWhileStatement;
	@Mock private PsiForStatement mockForStatement;
	@Mock private PsiForeachStatement mockForeachStatement;
	@Mock private PsiSwitchStatement mockSwitchStatement;
	@Mock private PsiMethodCallExpression mockMethodCallExpression;
	@Mock private PsiConditionalExpression mockConditionalExpression;
	@Mock private PsiStatement mockStatement;
	@Mock private PsiCodeBlock mockCodeBlock;
	@Mock private static ExtractMethodProcessor mockProcessor;
	@Mock private PsiFile mockFile;
	@Mock private Project mockProject;

	private static PsiElementCyclomaticExtractVisitor visitor;
	private static MockedStatic<CyclomaticComplexityDialogsProvider> mockedDialogsProvider;
	private static MockedStatic<ExtractMethodHandler> mockedExtractMethodHandler;

	@BeforeAll
	static void setUp() {
		visitor = spy(new PsiElementCyclomaticExtractVisitor());

		mockedDialogsProvider = mockStatic(CyclomaticComplexityDialogsProvider.class);
		mockedDialogsProvider.when(() -> CyclomaticComplexityDialogsProvider.showIdentifyComplexElementDialog(any(), any())).thenReturn(true);
		mockedDialogsProvider.when(() -> CyclomaticComplexityDialogsProvider.showIdentifyComplexElementsDialog(any(), any())).thenReturn(true);

		mockedExtractMethodHandler = mockStatic(ExtractMethodHandler.class);
		mockedExtractMethodHandler.when(() -> ExtractMethodHandler.getProcessor(any(), any(), any(), eq(false))).thenAnswer(invocation -> {
			System.out.println("ExtractMethodHandler.getProcessor called");
			return mockProcessor;
		});
		mockedExtractMethodHandler.when(() -> ExtractMethodHandler.invokeOnElements(any(), any(), any(), eq(true))).thenReturn(true);
	}

	@BeforeEach
	void setUpEach() {
		// Reset the visitor's state or any other setup needed before each test
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

	@Test
	void testVisitCodeBlock() {
		PsiStatement mockStatement1 = mock(PsiStatement.class);
		PsiStatement mockStatement2 = mock(PsiStatement.class);
		PsiStatement[] mockStatements = {mockStatement1, mockStatement2};

		when(mockCodeBlock.getStatements()).thenReturn(mockStatements);
		when(mockStatement1.getProject()).thenReturn(mockProject);
		when(mockStatement1.getContainingFile()).thenReturn(mockFile);

		visitor.visitCodeBlock(mockCodeBlock);

		verify(visitor).extractElementArray(mockStatements);
		assertTrue(visitor.isRefactored());
	}
}
