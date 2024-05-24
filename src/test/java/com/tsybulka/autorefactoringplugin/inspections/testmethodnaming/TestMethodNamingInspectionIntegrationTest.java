package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestMethodNamingInspectionIntegrationTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME = InspectionsBundle.message("inspection.test.method.name.use.quickfix");

	@Override
	protected String getTestDataPath() {
		return "src/test/testData/testNamingInspection";
	}

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp(); // Ensures that LightJavaCodeInsightFixtureTestCase setup is called
	}

	@AfterEach
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testNamingInspectionFix() {
		myFixture.configureByFile("testNaming.java");

		// Apply the inspection
		InspectionProfileEntry inspection = new TestMethodNamingInspection();
		myFixture.enableInspections(inspection);

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
		assertFalse(highlightInfos.isEmpty());
		// Get all available quick fixes
		AtomicBoolean isDetected= new AtomicBoolean(false);
		List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
		for (IntentionAction quickFix : quickFixes) {
			WriteCommandAction.runWriteCommandAction(getProject(), () -> {
				ApplicationManager.getApplication().invokeLater(() -> {
			if (quickFix.getText().equals(QUICK_FIX_NAME)) {
				isDetected.set(true);
			}
				});
			});
			if (isDetected.get()) break;
		}
		assertTrue(isDetected.get());
	}
}
