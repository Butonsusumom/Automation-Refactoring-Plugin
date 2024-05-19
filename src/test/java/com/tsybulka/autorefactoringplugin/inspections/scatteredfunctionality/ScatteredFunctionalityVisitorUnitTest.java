package com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ScatteredFunctionalityVisitorUnitTest {

	@Mock private PsiCodeBlock mockCodeBlock;
	@Mock private PsiReferenceExpression mockReferenceExpression;
	@Mock private PsiMethodCallExpression mockMethodCallExpression;
	@Mock private PsiExpression qualifier;
	@Mock private PluginSettings mockSettings;

	private Map<Integer, Set<PsiElement>> seenCodeBlocks;
	private ScatteredFunctionalityVisitor visitor;

	private static MockedStatic<ApplicationManager> mockedApplicationManager;
	private static MockedStatic<PluginSettings> mockedPluginSettings;

	@BeforeEach
	void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		Application mockApplication = mock(Application.class);
		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);

		mockedPluginSettings = mockStatic(PluginSettings.class);
		mockedPluginSettings.when(PluginSettings::getInstance).thenReturn(mockSettings);

		seenCodeBlocks = new HashMap<>();
		visitor = spy(new ScatteredFunctionalityVisitor(seenCodeBlocks));
	}

	@AfterEach
	void tearDown() {
		mockedApplicationManager.close();
		mockedPluginSettings.close();
	}

	@Test
	void testPsiReferenceExpressionHandling() {
		when(mockReferenceExpression.getQualifierExpression()).thenReturn(qualifier);
		when(qualifier.getText()).thenReturn("var.method()");
		when(qualifier.getChildren()).thenReturn(new PsiCodeBlock[]{});

		StringBuilder out = new StringBuilder();
		Set<String> declaredVariables = new HashSet<>(List.of("var"));
		visitor.recursiveNormalize(mockReferenceExpression, out, declaredVariables);
	}

	@Test
	void testPsiMethodCallExpressionHandling() {
		when(mockMethodCallExpression.getMethodExpression()).thenReturn(mockReferenceExpression);
		when(mockReferenceExpression.getQualifierExpression()).thenReturn(qualifier);
		when(qualifier.getText()).thenReturn("var");
		when(mockReferenceExpression.getReferenceName()).thenReturn("method");

		PsiExpression[] args = new PsiExpression[0]; // No arguments for simplicity
		when(mockMethodCallExpression.getArgumentList()).thenReturn(mock(PsiExpressionList.class));
		when(mockMethodCallExpression.getArgumentList().getExpressions()).thenReturn(args);

		StringBuilder out = new StringBuilder();
		Set<String> declaredVariables = new HashSet<>(List.of("var"));
		visitor.recursiveNormalize(mockMethodCallExpression, out, declaredVariables);

		assertEquals("var.method()", out.toString(), "Output should replace the method call with normalized format.");
	}

	@Test
	void testVisitCodeBlock_WithSufficientLineCount_ShouldProcessElement() {
		when(mockCodeBlock.getText()).thenReturn("int a = 0;\na++;\nif (a > 0) {\na--;\n}");
		when(mockCodeBlock.getChildren()).thenReturn(new PsiElement[]{});
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(true);
		//when(visitor.countLines(mockCodeBlock)).thenReturn(5);

		visitor.visitCodeBlock(mockCodeBlock);

		verify(mockCodeBlock, atLeastOnce()).getText();
		assertFalse(seenCodeBlocks.isEmpty(), "Code blocks should have been registered in the map.");
	}

	@Test
	void testVisitCodeBlock_WithInsufficientLineCount_ShouldNotProcessElement() {
		when(mockCodeBlock.getText()).thenReturn("a++;");
		//when(visitor.countLines(mockCodeBlock)).thenReturn(1);
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(true);

		visitor.visitCodeBlock(mockCodeBlock);

		verify(mockCodeBlock, atLeastOnce()).getText();
		assertTrue(seenCodeBlocks.isEmpty(), "Code blocks should not be registered in the map.");
	}

	@Test
	void testNormalizationAndProcessing() {
		String code = "\n \n \n \n int x = 0;\nx++;";
		when(mockCodeBlock.getChildren()).thenReturn(new PsiCodeBlock[]{});
		when(mockCodeBlock.getText()).thenReturn(code);
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(true);

		visitor.visitCodeBlock(mockCodeBlock);

		// Simulate normalization (to be further detailed)
		String normalizedCode = "";
		assertEquals(normalizedCode, visitor.normalizePsiCodeBlock(mockCodeBlock), "Normalized code should match expected result.");
	}

	@Test
	void testInspectionDisabled() {
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(false);

		visitor.visitCodeBlock(mockCodeBlock);

		assertTrue(seenCodeBlocks.isEmpty(), "No code blocks should be processed when inspection is disabled.");
	}
}
