package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonInspection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@TestDataPath("$CONTENT_ROOT/testData")
public class ObjectComparisonInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	private static final String QUICK_FIX_NAME =
			InspectionsBundle.message("inspection.comparing.objects.references.use.quickfix");


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		myFixture.enableInspections(new ObjectComparisonInspection());
		// optimization: add a fake java.lang.String class to avoid loading all JDK classes for test
		myFixture.addClass("package java.lang; public final class String {}");
	}

	/**
	 * Defines the path to files used for running tests.
	 *
	 * @return The path from this module's root directory ($MODULE_WORKING_DIR$) to the
	 * directory containing files for these tests.
	 */
	@Override
	protected String getTestDataPath() {
		return "src/test/testData";
	}

	/**
	 * Test the '==' case.
	 */
	public void testRelationalEq() {
		doTest("Eq");
	}

	/**
	 * Given the name of a test file, runs comparing references inspection quick fix and tests
	 * the results against a reference outcome file.
	 * File name pattern 'foo.java' and 'foo.after.java' are matching before and after files
	 * in the testData directory.
	 *
	 * @param testName test file name base
	 */
	protected void doTest(@NotNull String testName) {
		// Initialize the test based on the testData file
		myFixture.configureByFile(testName + ".java");
		// Initialize the inspection and get a list of highlighted
		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
		assertFalse(highlightInfos.isEmpty());
		// Get the quick fix action for comparing references inspection and apply it to the file
		final IntentionAction action = myFixture.findSingleIntention(QUICK_FIX_NAME);
		assertNotNull(action);
		myFixture.launchAction(action);
		// Verify the results
		myFixture.checkResultByFile(testName + ".after.java");
	}

}