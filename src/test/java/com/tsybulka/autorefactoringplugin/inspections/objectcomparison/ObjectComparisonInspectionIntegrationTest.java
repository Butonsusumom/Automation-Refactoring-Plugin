package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ObjectComparisonInspectionIntegrationTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME = InspectionsBundle.message("inspection.comparing.objects.references.use.quickfix");

	@Override
	protected String getTestDataPath() {
		return "src/test/testData/objectComparisonInspection";
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
	public void testObjectComparisonFix() {
		myFixture.configureByFile("before.java");

		// Apply the inspection
		InspectionProfileEntry inspection = new ObjectComparisonInspection();
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
		myFixture.checkResultByFile("after.java");
	}
}
