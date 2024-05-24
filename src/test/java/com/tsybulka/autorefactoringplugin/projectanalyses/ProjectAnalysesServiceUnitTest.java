package com.tsybulka.autorefactoringplugin.projectanalyses;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectAnalysesServiceUnitTest {

	@Mock private Project mockProject;
	@Mock private PsiClass mockPsiClass;
	@Mock private PsiElement mockPsiElement;
	@Mock private PsiJavaFile mockJavaFile;
	@Mock private PsiMethod mockMethod;
	@Mock private PsiPackageStatement mockPackageStatement;
	@Mock private MetricsCalculationService mockMetricsCalculationService;
	@Mock Application mockApplication;

	@Spy private List<ClassMetrics> mockClassMetrics = new ArrayList<>();
	@Spy private Set<PsiClass> mockClasses = new HashSet<>();
	@Spy private List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
	@Spy private Set<ArchitectureSmell> architectureSmellsList = new HashSet<>();
	@Spy private List<TestSmell> testSmellsList = new ArrayList<>();
	@Spy private Map<Integer, Set<PsiElement>> seenCodeBlocks = new HashMap<>();

	private ProjectAnalysesService projectAnalysesService;
	private MockedStatic<ApplicationManager> mockedApplicationManager;
	private MockedStatic<Application> mockedApplication;
	private MockedStatic<PluginSettings> mockedPluginSettings;

	@BeforeEach
	void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		mockedApplication = mockStatic(Application.class);
		mockedPluginSettings = mockStatic(PluginSettings.class);

		when(ApplicationManager.getApplication()).thenReturn(mockApplication);

		projectAnalysesService = new ProjectAnalysesService(mockMetricsCalculationService);
		mockClasses.add(mockPsiClass);
	}

	@AfterEach
	void tearDown() {
		mockedApplicationManager.close();
		mockedApplication.close();
		mockedPluginSettings.close();
	}

	@Test
	void testAnalyseProject() {
		when(mockMetricsCalculationService.calculateProjectMetrics(mockProject)).thenReturn(mockClassMetrics);
		when(mockMetricsCalculationService.collectPsiClassesFromSrc(mockProject)).thenReturn(mockClasses);

		ProjectSmellsInfo result = projectAnalysesService.analyseProject(mockProject);

		assertNotNull(result);
		assertIterableEquals(implementationSmellsList, result.getImplementationSmellsList());
		assertIterableEquals(architectureSmellsList, result.getArchitectureSmellList());
		assertIterableEquals(testSmellsList, result.getTestSmellsList());
		assertIterableEquals(mockClassMetrics, result.getClassMetricsList());
	}

	@Test
	void testCollectImplementationSmells() {
		List<ImplementationSmell> result = projectAnalysesService.collectImplementationSmells(mockClasses);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void testCollectArchitectureSmells() {

		Set<ArchitectureSmell> result = projectAnalysesService.collectArchitectureSmells(mockClasses);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void testCollectTestSmells() {
		List<TestSmell> result = projectAnalysesService.collectTestSmells(mockClasses);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void testRegisterScatteredSmell() {
		PsiElement mockElement1 = mock(PsiElement.class);
		PsiElement mockElement2 = mock(PsiElement.class);
		Set<PsiElement> elements = new HashSet<>(Arrays.asList(mockElement1, mockElement2));

		seenCodeBlocks.put(1, elements);
		when(mockElement1.getContainingFile()).thenReturn(mockJavaFile);
		when(mockElement2.getContainingFile()).thenReturn(mockJavaFile);

		projectAnalysesService.registerScatteredSmell(architectureSmellsList, seenCodeBlocks);

		assertEquals(1, architectureSmellsList.size());
	}

	@Test
	void testGetPackageName() {
		when(mockPsiElement.getContainingFile()).thenReturn(mockJavaFile);
		when(mockJavaFile.getPackageStatement()).thenReturn(mockPackageStatement);
		when(mockPackageStatement.getPackageName()).thenReturn("com.example");

		String result = projectAnalysesService.getPackageName(mockPsiElement);

		assertEquals("com.example", result);
	}

	@Test
	void testGetScatteredClasses() {
		when(PsiTreeUtil.getParentOfType(mockPsiElement, PsiClass.class)).thenReturn(mockPsiClass);
		when(mockPsiClass.getName()).thenReturn("TestClass");

		Set<PsiElement> elements = new HashSet<>(Collections.singletonList(mockPsiElement));
		String result = projectAnalysesService.getScatteredClasses(elements, ", ");

		assertEquals("TestClass", result);
	}

	@Test
	void testGetScatteredPackages() {
		when(mockPsiElement.getContainingFile()).thenReturn(mockJavaFile);
		when(mockJavaFile.getPackageStatement()).thenReturn(mockPackageStatement);
		when(mockPackageStatement.getPackageName()).thenReturn("com.example");

		Set<PsiElement> elements = new HashSet<>(Collections.singletonList(mockPsiElement));
		String result = projectAnalysesService.getScatteredPackages(elements);

		assertEquals("com.example", result);
	}

	@Test
	void testGetScatteredMethods() {
		when(PsiTreeUtil.getParentOfType(mockPsiElement, PsiMethod.class)).thenReturn(mockMethod);
		when(mockMethod.getName()).thenReturn("testMethod");

		Set<PsiElement> elements = new HashSet<>(Collections.singletonList(mockPsiElement));
		String result = projectAnalysesService.getScatteredMethods(elements);

		assertEquals("testMethod", result);
	}
}
