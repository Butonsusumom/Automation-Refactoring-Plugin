package com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation;

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

public class RepeatedObjectCreationInspectionIntegrationTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME = InspectionsBundle.message("inspection.repeated.object.creation.use.quickfix");

	@Override
	protected String getTestDataPath() {
		return "src/test/testData/repeatedObjectCreation";
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
	public void testRepeatedObjectCreationFix() {
		myFixture.configureByFile("repeatedObjectCreationBefore.java");

		// Apply the inspection
		InspectionProfileEntry inspection = new RepeatedObjectCreationInspection();
		myFixture.enableInspections(inspection);

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
		assertFalse(highlightInfos.isEmpty());
		// Get all available quick fixes
		List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
		AtomicBoolean invoked = new AtomicBoolean(false);
		for (IntentionAction quickFix : quickFixes) {
			WriteCommandAction.runWriteCommandAction(getProject(), () -> {
				ApplicationManager.getApplication().invokeLater(() -> {
					if (quickFix.getText().equals(QUICK_FIX_NAME)) {
						myFixture.launchAction(quickFix);
						invoked.set(true);
					}
				});
			});
			if (invoked.get()) break;
		}
		// Print the modified file content for debugging
		String modifiedContent = myFixture.getFile().getText();
		System.out.println("Modified content: \n" + modifiedContent);
		// Verify the results
		myFixture.checkResultByFile("repeatedObjectCreationAfter.java");
	}
}
