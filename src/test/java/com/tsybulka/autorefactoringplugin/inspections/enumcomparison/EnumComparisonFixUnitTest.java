package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnumComparisonFixUnitTest {

	@Mock
	private Project mockProject;

	@Mock
	private ProblemDescriptor mockProblemDescriptor;

	@Mock
	private PsiMethodCallExpression mockMethodCallExpression;

	@Mock
	private PsiReferenceExpression mockReferenceExpression;

	@Mock
	private PsiMethod mockMethod;

	@Mock
	private PsiClass mockContainingClass;

	@Mock
	private PsiExpression mockLeftOperand;

	@Mock
	private PsiExpression mockRightOperand;

	@Mock
	private PsiElementFactory mockElementFactory;

	@Mock
	private PsiBinaryExpression mockBinaryExpression;

	@Mock
	private PsiJavaToken mockOperationToken;

	@Mock
	private PsiExpressionList mockArgumentList;

	@InjectMocks
	private EnumComparisonFix fix;

	@BeforeEach
	void setUp() {
		when(mockProblemDescriptor.getPsiElement()).thenReturn(mockMethodCallExpression);
		when(mockMethodCallExpression.resolveMethod()).thenReturn(mockMethod);
		when(mockMethod.getContainingClass()).thenReturn(mockContainingClass);
		when(mockContainingClass.getQualifiedName()).thenReturn("java.util.Objects");
		when(mockArgumentList.getExpressions()).thenReturn(new PsiExpression[]{mockLeftOperand, mockRightOperand});
		when(mockMethodCallExpression.getArgumentList()).thenReturn(mockArgumentList);

		JavaPsiFacade mockFacade = mock(JavaPsiFacade.class);
		when(JavaPsiFacade.getInstance(mockProject)).thenReturn(mockFacade);
		when(mockFacade.getElementFactory()).thenReturn(mockElementFactory);
		when(mockElementFactory.createExpressionFromText(anyString(), isNull())).thenReturn(mockBinaryExpression);
	}

	@Test
	void testApplyFix_equals() {
		// Setup for non-Objects.equals scenario
		when(mockMethodCallExpression.getMethodExpression()).thenReturn(mockReferenceExpression);
		when(mockReferenceExpression.getQualifierExpression()).thenReturn(mockLeftOperand);
		when(mockContainingClass.getQualifiedName()).thenReturn("java.lang.Object");
		when(mockMethodCallExpression.getMethodExpression().getQualifierExpression()).thenReturn(mockLeftOperand);
		PsiPrefixExpression mockParent = mock(PsiPrefixExpression.class);
		when(mockMethodCallExpression.getParent()).thenReturn(mockParent);
		when(mockParent.getOperationSign()).thenReturn(mockOperationToken);
		when(mockBinaryExpression.getLOperand()).thenReturn(mockLeftOperand);
		when(mockBinaryExpression.getROperand()).thenReturn(mockRightOperand);

		fix.applyFix(mockProject, mockProblemDescriptor);

		// Verify replacement operations
		verify(mockBinaryExpression).getLOperand();
		verify(mockBinaryExpression).getROperand();
		verify(mockBinaryExpression.getLOperand()).replace(mockLeftOperand);
		verify(mockBinaryExpression.getROperand()).replace(mockLeftOperand);
	}

	@Test
	void testApplyFix_objectsEquals() {
		// Setup for Objects.equals scenario
		when(mockContainingClass.getQualifiedName()).thenReturn("java.util.Objects");
		PsiPrefixExpression mockParent = mock(PsiPrefixExpression.class);
		when(mockMethodCallExpression.getParent()).thenReturn(mockParent);
		when(mockParent.getOperationSign()).thenReturn(mockOperationToken);
		when(mockBinaryExpression.getLOperand()).thenReturn(mockLeftOperand);
		when(mockBinaryExpression.getROperand()).thenReturn(mockRightOperand);

		fix.applyFix(mockProject, mockProblemDescriptor);

		// Verify replacement operations
		verify(mockBinaryExpression).getLOperand();
		verify(mockBinaryExpression).getROperand();
		verify(mockBinaryExpression.getLOperand()).replace(mockLeftOperand);
		verify(mockBinaryExpression.getROperand()).replace(mockRightOperand);
	}

	@Test
	void testApplyFix_negatedEquals() {
		// Setup for negated equals scenario
		PsiPrefixExpression mockParent = mock(PsiPrefixExpression.class);
		when(mockMethodCallExpression.getParent()).thenReturn(mockParent);
		when(mockParent.getOperationSign()).thenReturn(mockOperationToken);
		when(mockOperationToken.getTokenType()).thenReturn(JavaTokenType.EXCL);
		when(mockBinaryExpression.getLOperand()).thenReturn(mockLeftOperand);
		when(mockBinaryExpression.getROperand()).thenReturn(mockRightOperand);

		fix.applyFix(mockProject, mockProblemDescriptor);

		// Verify replacement operations
		verify(mockBinaryExpression).getLOperand();
		verify(mockBinaryExpression).getROperand();
		verify(mockBinaryExpression.getLOperand()).replace(mockLeftOperand);
		verify(mockBinaryExpression.getROperand()).replace(mockRightOperand);
		verify(mockParent).replace(mockBinaryExpression);
	}
}
