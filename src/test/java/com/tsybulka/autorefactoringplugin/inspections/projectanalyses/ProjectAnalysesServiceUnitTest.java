package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;

import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ArchitectureSmell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectAnalysesServiceUnitTest {

	private final ProjectAnalysesService classUnderTest = new ProjectAnalysesService();

	@Test
	public void testGetFilePath_EmptyList() {
		List<ArchitectureSmell> actual = classUnderTest.collectArchitectureSmells(null);

		assertEquals(new ArrayList<>(), actual);
	}
}