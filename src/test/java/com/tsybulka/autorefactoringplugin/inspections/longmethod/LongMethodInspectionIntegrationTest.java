package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LongMethodInspectionIntegrationTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME = InspectionsBundle.message("inspection.long.method.use.quickfix");

	@Override
	protected String getTestDataPath() {
		return "src/test/testData/longMethodInspection";
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
	public void testLongMethodFix() {
		myFixture.configureByFile("longMethod.java");

		// Apply the inspection
		InspectionProfileEntry inspection = new LongMethodInspection();
		myFixture.enableInspections(inspection);

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
		assertFalse(highlightInfos.isEmpty());
		// Get all available quick fixes
		boolean isDetected=false;
		List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
		for (IntentionAction quickFix : quickFixes) {
			if (quickFix.getText().equals(QUICK_FIX_NAME)) {
				isDetected=true;
				break;
			}
		}
		assertTrue(isDetected);
	}
}
