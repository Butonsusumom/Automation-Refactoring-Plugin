package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.classmetrics.CyclomaticComplexityVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MethodCyclomaticComplexityFixTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);
	@Mock private Project mockProject;
	@Mock private ProblemDescriptor mockProblemDescriptor;
	@Mock private PsiElement mockElement;
	@Mock private PsiIfStatement mockIfStatement;
	@Mock private PsiMethodCallExpression mockMethodCallExpression;

	@InjectMocks private MethodCyclomaticComplexityFix methodCyclomaticComplexityFix;

	private static MockedStatic<ApplicationManager> mockedApplicationManager;
	private static MockedStatic<CyclomaticComplexityDialogsProvider> mockedDialogsProviderStatic;

	@BeforeAll
	static void setUpAll() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		mockedDialogsProviderStatic = mockStatic(CyclomaticComplexityDialogsProvider.class);
	}

	@AfterAll
	static void tearDownAll() {
		mockedApplicationManager.close();
		mockedDialogsProviderStatic.close();
	}

	@BeforeEach
	void setUp() {
		Application mockApplication = mock(Application.class);
		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
		//when(mockProblemDescriptor.getPsiElement()).thenReturn(mockElement);
	}

	@Test
	void testGetFamilyName() {
		assertEquals(InspectionsBundle.message("inspection.cyclomatic.complexity.use.quickfix"), methodCyclomaticComplexityFix.getFamilyName());
	}

	@Test
	void testApplyFix_ShouldNotRefactor() {
		mockedDialogsProviderStatic.when(()->CyclomaticComplexityDialogsProvider.showStartDialog(any())).thenReturn(false);
		methodCyclomaticComplexityFix.applyFix(mockProject, mockProblemDescriptor);

		verifyNoInteractions(mockElement);
	}

	@Test
	void testRefactor_ShouldReturnFalseWhenNoComplexElement() {
		when(mockElement.getChildren()).thenReturn(new PsiElement[0]);

		boolean result = methodCyclomaticComplexityFix.refactor(mockElement);

		assertFalse(result);
	}

	@Test
	void testRefactor_ShouldReturnTrueWhenRefactored() {
		PsiElement mockChild = mock(PsiElement.class);
		when(mockElement.getChildren()).thenReturn(new PsiElement[]{mockChild});
		doAnswer(invocation -> {
			CyclomaticComplexityVisitor visitor = invocation.getArgument(0);
			visitor.visitElement(mockChild);
			return null;
		}).when(mockChild).accept(any());

		boolean result = methodCyclomaticComplexityFix.refactor(mockElement);

		assertFalse(result);
	}

	@Test
	void testGetComplexity() {
		doAnswer(invocation -> {
			CyclomaticComplexityVisitor visitor = invocation.getArgument(0);
			visitor.visitElement(mockElement);
			return null;
		}).when(mockElement).accept(any());

		int complexity = methodCyclomaticComplexityFix.getComplexity(mockElement);

		assertEquals(1, complexity);
	}

	@Test
	void testGetChildren_ForPsiIfStatement() {
		PsiStatement thenBranch = mock(PsiStatement.class);
		PsiStatement elseBranch = mock(PsiStatement.class);
		PsiExpression condition = mock(PsiExpression.class);
		when(mockIfStatement.getThenBranch()).thenReturn(thenBranch);
		when(mockIfStatement.getElseBranch()).thenReturn(elseBranch);
		when(mockIfStatement.getCondition()).thenReturn(condition);
		when(thenBranch.getChildren()).thenReturn(new PsiElement[]{thenBranch});
		when(elseBranch.getChildren()).thenReturn(new PsiElement[]{elseBranch});

		PsiElement[] children = methodCyclomaticComplexityFix.getChildren(mockIfStatement);

		assertEquals(3, children.length);
	}

	@Test
	void testGetChildren_ForPsiMethodCallExpression() {
		PsiExpression argument = mock(PsiExpression.class);
		when(mockMethodCallExpression.getArgumentList()).thenReturn(mock(PsiExpressionList.class));
		when(mockMethodCallExpression.getArgumentList().getExpressions()).thenReturn(new PsiExpression[]{argument});

		PsiElement[] children = methodCyclomaticComplexityFix.getChildren(mockMethodCallExpression);

		assertEquals(1, children.length);
	}

	@Test
	void testGetChildren_ForOtherElements() {
		PsiElement child = mock(PsiElement.class);
		when(mockElement.getChildren()).thenReturn(new PsiElement[]{child});

		PsiElement[] children = methodCyclomaticComplexityFix.getChildren(mockElement);

		assertEquals(1, children.length);
	}
}
