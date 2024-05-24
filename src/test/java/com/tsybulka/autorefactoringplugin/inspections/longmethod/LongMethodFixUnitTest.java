package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Query;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LongMethodFixUnitTest {

	@Mock
	private Project mockProject;
	@Mock
	private ProblemDescriptor mockProblemDescriptor;
	@Mock
	private PsiMethod mockPsiMethod;
	@Mock
	private PsiElementFactory mockFactory;
	@Mock
	private Query<PsiReference> mockQuery;
	@Mock
	private Application mockApplication;
	@Mock
	private JavaPsiFacade mockJavaPsiFacade;

	@Captor
	private ArgumentCaptor<Runnable> runnableCaptor;

	private MockedStatic<ApplicationManager> mockedApplicationManager;
	private MockedStatic<JavaPsiFacade> mockedJavaPsiFacade;
	private MockedStatic<LongMethodVisitor> mockedLongMethodVisitor;
	private MockedStatic<LongMethodDialogsProvider> mockedDialogsProvider;
	private MockedStatic<PsiElementFactory> mockedElementFactory;

	@BeforeEach
	void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);

		mockedJavaPsiFacade = mockStatic(JavaPsiFacade.class);
		mockedJavaPsiFacade.when(() -> JavaPsiFacade.getInstance(any())).thenReturn(mockJavaPsiFacade);
		mockedJavaPsiFacade.when(() -> JavaPsiFacade.getElementFactory(any())).thenReturn(mockFactory);

		mockedLongMethodVisitor = mockStatic(LongMethodVisitor.class);
		mockedDialogsProvider = mockStatic(LongMethodDialogsProvider.class);
		mockedElementFactory = mockStatic(PsiElementFactory.class);
	}

	@AfterEach
	void tearDown() {
		mockedApplicationManager.close();
		mockedJavaPsiFacade.close();
		mockedLongMethodVisitor.close();
		mockedDialogsProvider.close();
		mockedElementFactory.close();
	}

	@Test
	void testGetFamilyName() {
		LongMethodFix fix = new LongMethodFix();
		assertEquals(fix.getFamilyName(), InspectionsBundle.message("inspection.long.method.use.quickfix"));
	}

	@Test
	void testApplyFix() {
		when(mockProblemDescriptor.getPsiElement()).thenReturn(mockPsiMethod);
		when(mockPsiMethod.getProject()).thenReturn(mockProject);

		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return null;
		}).when(mockApplication).invokeLater(any(Runnable.class));

		LongMethodFix fix = new LongMethodFix();
		fix.applyFix(mockProject, mockProblemDescriptor);

		verify(mockApplication).invokeLater(runnableCaptor.capture());
		Runnable capturedRunnable = runnableCaptor.getValue();
		capturedRunnable.run();

		// Verify the metrics calculation
		verify(mockPsiMethod, times(2)).getProject();
	}

	@Test
	void testRefactorMethod() {
		mockedDialogsProvider.when(() -> LongMethodDialogsProvider.showStartExtractParametersDialog(mockProject)).thenReturn(true);
		mockedElementFactory.when(() -> PsiElementFactory.getInstance(mockProject)).thenReturn(mockFactory);

		LongMethodFix fix = new LongMethodFix();

		ApplicationManager.getApplication().invokeAndWait(() -> {
			boolean result = fix.refactorMethod(mockPsiMethod);
			assertTrue(result);
		});
	}

	@Test
	void testUpdateMethodBody() {
		PsiMethod mockMethod = mock(PsiMethod.class);
		PsiCodeBlock mockBody = mock(PsiCodeBlock.class);
		PsiParameter[] mockParameters = {mock(PsiParameter.class), mock(PsiParameter.class)};
		when(mockParameters[0].getName()).thenReturn("param1");
		when(mockParameters[1].getName()).thenReturn("param2");
		when(mockMethod.getBody()).thenReturn(mockBody);
		when(mockBody.getStatements()).thenReturn(new PsiStatement[0]);

		LongMethodFix fix = new LongMethodFix();
		fix.updateMethodBody(mockMethod, mockParameters, "params");

		verify(mockBody).getStatements();
	}

	@Test
	void testReplaceVariableUsages() {
		PsiReferenceExpression mockReferenceExpression = mock(PsiReferenceExpression.class);
		when(mockPsiMethod.getChildren()).thenReturn(new PsiElement[]{mockReferenceExpression});
		when(mockReferenceExpression.getChildren()).thenReturn(new PsiElement[]{});
		when(mockReferenceExpression.getReferenceName()).thenReturn("param1");

		when(JavaPsiFacade.getElementFactory(mockProject)).thenReturn(mockFactory);
		when(mockFactory.createExpressionFromText(anyString(), any())).thenReturn(mock(PsiExpression.class));

		LongMethodFix fix = new LongMethodFix();
		fix.replaceVariableUsages(mockPsiMethod, Map.of("param1", "params.getParam1()"));

		verify(mockReferenceExpression).replace(any(PsiExpression.class));
	}

	@Test
	void testUpdateMethodCalls() {
		try (MockedStatic<MethodReferencesSearch> mockedMethodReferencesSearch = mockStatic(MethodReferencesSearch.class);
			 MockedStatic<WriteCommandAction> mockedWriteCommandAction = mockStatic(WriteCommandAction.class)) {

			mockedMethodReferencesSearch.when(() -> MethodReferencesSearch.search(any(PsiMethod.class), any(GlobalSearchScope.class), anyBoolean())).thenReturn(mockQuery);

			LongMethodFix fix = new LongMethodFix();
			fix.updateMethodCalls(mockPsiMethod, "TestClass", mockFactory, mockProject);
		}
	}

	@Test
	void testRecursiveRefactor() {
		PsiElement mockElement = mock(PsiElement.class);
		when(mockElement.getChildren()).thenReturn(new PsiElement[0]);

		mockedLongMethodVisitor.when(() -> LongMethodVisitor.getLinesOfCode(mockElement)).thenReturn(0);

		LongMethodFix fix = new LongMethodFix();
		boolean result = fix.recursiveRefactor(mockElement, "loc");

		assertFalse(result);
	}

	@Test
	void testRefactorMethodEDT() {
		PsiParameterList mockParameterList = mock(PsiParameterList.class);

		mockedDialogsProvider.when(() -> LongMethodDialogsProvider.showStartExtractParametersDialog(mockProject)).thenReturn(true);
		mockedElementFactory.when(() -> PsiElementFactory.getInstance(mockProject)).thenReturn(mockFactory);

		LongMethodFix fix = new LongMethodFix();

		// Ensure EDT
		ApplicationManager.getApplication().invokeAndWait(() -> {
			boolean result = fix.refactorMethod(mockPsiMethod);
			assertTrue(result);
		});
	}
}
