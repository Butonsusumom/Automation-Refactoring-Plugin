package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ObjectMethodParameterInspectionIntegrationTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME = InspectionsBundle.message("inspection.object.parameter.use.quickfix");

	@Override
	protected String getTestDataPath() {
		return "src/test/testData/objectMethodParameterInspection";
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
	public void testObjectParameterFix() {
		myFixture.configureByFile("objectParameterBefore.java");

		// Apply the inspection
		InspectionProfileEntry inspection = new ObjectMethodParameterInspection();
		myFixture.enableInspections(inspection);

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
		assertFalse(highlightInfos.isEmpty());
		// Get all available quick fixes
		List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
		for (IntentionAction quickFix : quickFixes) {
			if (quickFix.getText().equals(QUICK_FIX_NAME)) {
				myFixture.launchAction(quickFix);
				break;
			}
		}
		// Print the modified file content for debugging
		String modifiedContent = myFixture.getFile().getText();
		System.out.println("Modified content: \n" + modifiedContent);
		// Verify the results
		myFixture.checkResultByFile("objectParameterAfter.java");
	}
}
