package com.tsybulka.autorefactoringplugin.projectanalyses;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.*;
import com.intellij.psi.search.ProjectScopeBuilder;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.tsybulka.autorefactoringplugin.model.metric.ClassMetricType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.ClassMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Query;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetricsCalculationServiceUnitTest {

	@Mock
	private Project mockProject;
	@Mock private PsiMethod superClassMethod;
	@Mock
	private PsiMethod psiMethod;
	@Mock
	private PsiField psiField;
	@Mock
	private PsiClass objectClass;
	@Mock
	private PsiClass superClass;
	@Mock
	private PsiClass mockPsiClass;
	@Mock
	private PsiFile psiFile;
	@Mock
	private PsiJavaFile psiJavaFile;
	@Mock
	private VirtualFile virtualFile;
	@Mock
	private PsiMethod psiMethod1, psiMethod2;
	@Mock
	private static GlobalSearchScope globalSearchScope;

	private static ProjectRootManager projectRootManager;
	private static PsiManager psiManager;

	@Spy
	private MetricsCalculationService classUnderTest = new MetricsCalculationService();

	@BeforeAll
	public static void setUp() {
		MockedStatic<ProjectRootManager> mockedProjectRootManager = mockStatic(ProjectRootManager.class);
		MockedStatic<PsiManager> mockedPsiManager = mockStatic(PsiManager.class);
		MockedStatic<ProjectScopeBuilder> mockedProjectScopedBuilder = mockStatic(ProjectScopeBuilder.class);

		projectRootManager = mock(ProjectRootManager.class);
		psiManager = mock(PsiManager.class);
		ProjectScopeBuilder projectScopedBuilder = mock(ProjectScopeBuilder.class);

		mockedProjectRootManager.when(() -> ProjectRootManager.getInstance(any(Project.class)))
				.thenReturn(projectRootManager);
		mockedPsiManager.when(() -> PsiManager.getInstance(any(Project.class)))
				.thenReturn(psiManager);
		mockedProjectScopedBuilder.when(() -> ProjectScopeBuilder.getInstance(any(Project.class)))
				.thenReturn(projectScopedBuilder);

		when(projectScopedBuilder.buildAllScope()).thenReturn(globalSearchScope);

	}

	@Test
	void testCalculateOopMetrics() {
		doReturn(100).when(classUnderTest).calculateLinesOfCode(mockPsiClass);
		doReturn(10).when(classUnderTest).calculateNumberOfFields(mockPsiClass);
		doReturn(5).when(classUnderTest).calculateNumberOfPublicFields(mockPsiClass);
		doReturn(15).when(classUnderTest).calculateNumberOfMethods(mockPsiClass);
		doReturn(8).when(classUnderTest).calculateNumberOfPublicMethods(mockPsiClass);
		doReturn(20).when(classUnderTest).calculateWmc(mockPsiClass);
		doReturn(3).when(classUnderTest).calculateNumberOfChildren(mockPsiClass, mockProject);
		doReturn(2).when(classUnderTest).calculateDepthOfInheritanceTree(mockPsiClass);
		doReturn(7).when(classUnderTest).calculateLackOfCohesionInMethods(mockPsiClass);
		doReturn(12).when(classUnderTest).calculateFanIn(mockPsiClass, mockProject);
		doReturn(9).when(classUnderTest).calculateFanOut(mockPsiClass, mockProject);

		HashMap<ClassMetricType, Integer> metrics = classUnderTest.calculateOopMetrics(mockPsiClass, mockProject);
		assertEquals(11, metrics.size()); // Check if all metrics are added
		assertEquals(100, metrics.get(ClassMetricType.LINES_OF_CODE));
		assertEquals(10, metrics.get(ClassMetricType.NUMBER_OF_FIELDS));
		assertEquals(20, metrics.get(ClassMetricType.WEIGHT_METHODS));
	}

	@Test
	void testCalculateFanIn_withValidReferences_returnsCorrectCount() {
		// Prepare the mocks
		PsiMethod mockMethod = mock(PsiMethod.class);
		PsiReference mockReference = mock(PsiReference.class);
		PsiMethodCallExpression mockExpression = mock(PsiMethodCallExpression.class);
		PsiMethod mockCallingMethod = mock(PsiMethod.class);
		PsiClass mockReferencingClass = mock(PsiClass.class);
		Project mockProject = mock(Project.class);

		// Mocking the responses needed for the method to function properly
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[] {mockMethod});
		when(mockReference.getElement()).thenReturn(mockExpression);
		when(mockExpression.getParent()).thenReturn(mockCallingMethod);

		// Mocking the static call to GlobalSearchScope.projectScope with a simple answer
		GlobalSearchScope scope = new GlobalSearchScope() {  // This is a simple implementation, adjust as necessary.
			@Override
			public boolean contains(@NotNull VirtualFile file) {
				return true; // Simplistic scope containment check
			}

			@Override
			public boolean isSearchInModuleContent(@NotNull Module aModule) {
				return false;
			}

			@Override
			public boolean isSearchInLibraries() {
				return true;
			}
		};
		MockedStatic<GlobalSearchScope> mockedScope = mockStatic(GlobalSearchScope.class);
		mockedScope.when(() -> GlobalSearchScope.projectScope(mockProject)).thenReturn(scope);

		// Create a fake query that responds to the search
		Query<PsiReference> fakeQuery = new FakeQuery<>(Collections.singletonList(mockReference));
		MockedStatic<ReferencesSearch> mockedSearch = mockStatic(ReferencesSearch.class);
		mockedSearch.when(() -> ReferencesSearch.search(mockMethod, scope)).thenReturn(fakeQuery);

		// Execute the method under test
		int fanIn = classUnderTest.calculateFanIn(mockPsiClass, mockProject);

		// Assertions and clean-up
		assertEquals(0, fanIn);
		mockedScope.close();
		mockedSearch.close();
	}


	// Helper class to fake Query results
	static class FakeQuery<T> implements Query<T> {
		private final Collection<T> results;

		FakeQuery(Collection<T> results) {
			this.results = results;
		}

		@Override
		public Collection<T> findAll() {
			return results;
		}

		@Override
		public @Nullable T findFirst() {
			return null;
		}

		@Override
		public boolean forEach(@NotNull Processor<? super T> consumer) {
			return false;
		}

		@Override
		public Iterator<T> iterator() {
			return results.iterator();
		}
	}

	@Test
	void testCalculateFanOut_withValidReferences_returnsCorrectCount() {
		// Prepare the mocks
		PsiMethod mockMethod = mock(PsiMethod.class);
		PsiReference mockReference = mock(PsiReference.class);
		PsiMethodCallExpression mockExpression = mock(PsiMethodCallExpression.class);
		PsiMethod mockCallingMethod = mock(PsiMethod.class);
		Project mockProject = mock(Project.class);

		// Mocking the responses needed for the method to function properly
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[] {mockMethod});
		when(mockReference.getElement()).thenReturn(mockExpression);
		when(mockExpression.getParent()).thenReturn(mockCallingMethod);

		// Mocking the static call to GlobalSearchScope.projectScope with a simple answer
		GlobalSearchScope scope = new GlobalSearchScope() {  // This is a simple implementation, adjust as necessary.
			@Override
			public boolean contains(@NotNull VirtualFile file) {
				return true; // Simplistic scope containment check
			}

			@Override
			public boolean isSearchInModuleContent(@NotNull Module aModule) {
				return false;
			}

			@Override
			public boolean isSearchInLibraries() {
				return true;
			}
		};
		MockedStatic<GlobalSearchScope> mockedScope = mockStatic(GlobalSearchScope.class);
		mockedScope.when(() -> GlobalSearchScope.projectScope(mockProject)).thenReturn(scope);

		// Create a fake query that responds to the search
		Query<PsiReference> fakeQuery = new FakeQuery<>(Collections.singletonList(mockReference));
		MockedStatic<ReferencesSearch> mockedSearch = mockStatic(ReferencesSearch.class);
		mockedSearch.when(() -> ReferencesSearch.search(mockMethod, scope)).thenReturn(fakeQuery);

		// Execute the method under test
		int fanIn = classUnderTest.calculateFanOut(mockPsiClass, mockProject);

		// Assertions and clean-up
		assertEquals(0, fanIn);
		mockedScope.close();
		mockedSearch.close();
	}

	@Test
	void testExtractFieldNames_withFields_returnsFieldNames() {
		// Arrange
		PsiMethod mockPsiMethod = mock(PsiMethod.class);
		PsiReferenceExpression mockReferenceExpression = mock(PsiReferenceExpression.class);
		PsiField mockField = mock(PsiField.class);
		when(mockField.getName()).thenReturn("fieldName");
		when(mockReferenceExpression.resolve()).thenReturn(mockField);

		// Use a custom visitor to simulate visiting reference expressions
		doAnswer(invocation -> {
			JavaRecursiveElementVisitor visitor = invocation.getArgument(0);
			visitor.visitReferenceExpression(mockReferenceExpression);
			return null;
		}).when(mockPsiMethod).accept(any(JavaRecursiveElementVisitor.class));

		// Act
		Set<String> fieldNames = classUnderTest.extractFieldNames(mockPsiMethod);

		// Assert
		assertNotNull(fieldNames);
		assertTrue(fieldNames.contains("fieldName"));
	}

	@Test
	void testExtractFieldNames_noFields_returnsEmptySet() {
		// Arrange
		PsiMethod mockPsiMethod = mock(PsiMethod.class);

		// Simulate no reference expressions
		doNothing().when(mockPsiMethod).accept(any(JavaRecursiveElementVisitor.class));

		// Act
		Set<String> fieldNames = classUnderTest.extractFieldNames(mockPsiMethod);

		// Assert
		assertTrue(fieldNames.isEmpty());
	}

	@Test
	void whenProjectIsEmpty_thenCalculateProjectMetricsReturnsEmptyList() {
		Set<PsiClass> emptySet = Collections.emptySet();
		doReturn(emptySet).when(classUnderTest).collectPsiClassesFromSrc(any(Project.class));
		List<ClassMetrics> result = classUnderTest.calculateProjectMetrics(mockProject);
		assertTrue(result.isEmpty());
	}

	@Test
	void whenProjectHasClasses_thenCalculateProjectMetricsReturnsMetricsList() {
		Set<PsiClass> classes = new HashSet<>(List.of(mockPsiClass));
		doReturn(classes).when(classUnderTest).collectPsiClassesFromSrc(any(Project.class));
		doReturn(new HashMap<>()).when(classUnderTest).calculateOopMetrics(any(PsiClass.class), any(Project.class));
		List<ClassMetrics> result = classUnderTest.calculateProjectMetrics(mockProject);
		assertEquals(1, result.size());
	}

	@Test
	void calculateLackOfCohesionInMethods_withNoSharedFields_shouldReturnTotalPairs() {
		PsiMethod[] methods = {psiMethod1, psiMethod2};
		doReturn(methods).when(mockPsiClass).getMethods();
		doReturn(false).when(classUnderTest).methodsShareField(psiMethod1, psiMethod2);

		int lcom = classUnderTest.calculateLackOfCohesionInMethods(mockPsiClass);

		assertEquals(1, lcom); // 1 pair, no shared fields
	}

	@Test
	void calculateLackOfCohesionInMethods_withSharedFields_shouldReducePairs() {
		PsiMethod[] methods = {psiMethod1, psiMethod2};
		doReturn(methods).when(mockPsiClass).getMethods();
		doReturn(true).when(classUnderTest).methodsShareField(psiMethod1, psiMethod2);

		int lcom = classUnderTest.calculateLackOfCohesionInMethods(mockPsiClass);

		assertEquals(0, lcom); // 1 pair, 1 shared field, should return 0
	}

	@Test
	void methodsShareField_withIntersection_shouldReturnTrue() {
		Set<String> fields1 = new HashSet<>(Arrays.asList("field1", "field2"));
		Set<String> fields2 = new HashSet<>(Arrays.asList("field2", "field3"));
		doReturn(fields1).when(classUnderTest).extractFieldNames(psiMethod1);
		doReturn(fields2).when(classUnderTest).extractFieldNames(psiMethod2);

		boolean shared = classUnderTest.methodsShareField(psiMethod1, psiMethod2);

		assertTrue(shared);
	}

	@Test
	void calculateLinesOfCode_whenClassIsNull_shouldReturnZero() {
		int loc = classUnderTest.calculateLinesOfCode(null);
		assertEquals(0, loc);
	}

	@Test
	void calculateLinesOfCode_countsNewLines() {
		String classContent = "class Test {\n    void method1() {}\n    void method2() {}\n}";
		when(mockPsiClass.getText()).thenReturn(classContent);

		int loc = classUnderTest.calculateLinesOfCode(mockPsiClass);

		assertEquals(4, loc); // 3 new lines + 1 for the last line
	}

	@Test
	public void calculateNumberOfFields_ShouldReturnFieldCount() {
		when(mockPsiClass.getFields()).thenReturn(new PsiField[]{psiField, psiField, psiField});
		assertEquals(3, classUnderTest.calculateNumberOfFields(mockPsiClass));
	}

	@Test
	public void calculateNumberOfPublicFields_ShouldCountOnlyPublicFields() {
		PsiField publicField = mock(PsiField.class);
		PsiField privateField = mock(PsiField.class);
		when(publicField.hasModifierProperty(PsiModifier.PUBLIC)).thenReturn(true);
		when(privateField.hasModifierProperty(PsiModifier.PUBLIC)).thenReturn(false);
		when(mockPsiClass.getFields()).thenReturn(new PsiField[]{publicField, privateField, publicField});
		assertEquals(2, classUnderTest.calculateNumberOfPublicFields(mockPsiClass));
	}

	@Test
	public void calculateNumberOfMethods_ShouldReturnMethodCount() {
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[]{psiMethod, psiMethod});
		assertEquals(2, classUnderTest.calculateNumberOfMethods(mockPsiClass));
	}

	@Test
	public void calculateNumberOfPublicMethods_ShouldCountOnlyPublicMethods() {
		PsiMethod publicMethod = mock(PsiMethod.class);
		PsiMethod privateMethod = mock(PsiMethod.class);
		when(publicMethod.hasModifierProperty(PsiModifier.PUBLIC)).thenReturn(true);
		when(privateMethod.hasModifierProperty(PsiModifier.PUBLIC)).thenReturn(false);
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[]{publicMethod, privateMethod, publicMethod});
		assertEquals(2, classUnderTest.calculateNumberOfPublicMethods(mockPsiClass));
	}

	// Test for inheritance calculations
	@Test
	public void calculateDepthOfInheritanceTree_WithMultipleLevels_ShouldCalculateDepth() {
		PsiClass grandSuperClass = mock(PsiClass.class);
		when(superClass.getSuperClass()).thenReturn(grandSuperClass);
		when(grandSuperClass.getSuperClass()).thenReturn(null);
		when(mockPsiClass.getSuperClass()).thenReturn(superClass);
		assertEquals(2, classUnderTest.calculateDepthOfInheritanceTree(mockPsiClass));
	}

	// Test for WMC calculations
	@Test
	public void calculateWmc_IncludingInheritedMethods_ShouldReturnCorrectWmc() {
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[]{psiMethod});
		when(superClass.getMethods()).thenReturn(new PsiMethod[]{superClassMethod});
		when(mockPsiClass.getSuperClass()).thenReturn(superClass);
		when(superClass.getSuperClass()).thenReturn(null);
		when(psiMethod.isConstructor()).thenReturn(false);
		when(psiMethod.hasModifierProperty(PsiModifier.STATIC)).thenReturn(false);
		when(psiMethod.getContainingClass()).thenReturn(mockPsiClass);
		assertEquals(2, classUnderTest.calculateWmc(mockPsiClass));  // Counting 1 own method + 1 inherited method
	}

	@Test
	public void calculateWmc_WithInheritedAndOwnedMethods_ShouldCountCorrectly() {
		when(superClass.getName()).thenReturn("SuperClass");
		when(superClass.getMethods()).thenReturn(new PsiMethod[] {superClassMethod});
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[] {psiMethod});
		when(psiMethod.isConstructor()).thenReturn(false);
		when(psiMethod.hasModifierProperty(PsiModifier.STATIC)).thenReturn(false);
		when(psiMethod.getContainingClass()).thenReturn(mockPsiClass);
		when(mockPsiClass.getName()).thenReturn("MyClass");
		when(mockPsiClass.getSuperClass()).thenReturn(superClass);
		when(superClass.getSuperClass()).thenReturn(objectClass);
		when(objectClass.getName()).thenReturn("Object");

		assertEquals(2, classUnderTest.calculateWmc(mockPsiClass)); // 1 from superClass + 1 own method
	}

	@Test
	void shouldAccumulateMethodsFromSuperClassesExcludingObject() {
		PsiMethod superClassMethod = mock(PsiMethod.class);
		when(superClass.getMethods()).thenReturn(new PsiMethod[]{superClassMethod});
		when(superClass.getName()).thenReturn("SuperClass");
		when(mockPsiClass.getSuperClass()).thenReturn(superClass);
		when(mockPsiClass.getMethods()).thenReturn(new PsiMethod[0]);

		assertEquals(1, classUnderTest.calculateWmc(mockPsiClass));
	}

	@Test
	public void shouldCollectPsiClassesFromMultipleSourceRoots_givenProjectWithSourceRoots() {
		VirtualFile root1 = mock(VirtualFile.class);
		VirtualFile root2 = mock(VirtualFile.class);
		VirtualFile[] sourceRoots = {root1, root2};
		when(projectRootManager.getContentSourceRoots()).thenReturn(sourceRoots);

		PsiDirectory directory1 = mock(PsiDirectory.class);
		PsiDirectory directory2 = mock(PsiDirectory.class);
		when(psiManager.findDirectory(root1)).thenReturn(directory1);
		when(psiManager.findDirectory(root2)).thenReturn(directory2);

		PsiJavaFile javaFile1 = mock(PsiJavaFile.class);
		PsiJavaFile javaFile2 = mock(PsiJavaFile.class);
		PsiClass psiClass1 = mock(PsiClass.class);
		PsiClass psiClass2 = mock(PsiClass.class);
		PsiClass[] classes1 = {psiClass1};
		PsiClass[] classes2 = {psiClass2};
		when(directory1.getFiles()).thenReturn(new PsiFile[]{javaFile1});
		when(directory2.getFiles()).thenReturn(new PsiFile[]{javaFile2});
		PsiDirectory subdirectory = mock(PsiDirectory.class);
		when(directory1.getSubdirectories()).thenReturn(new PsiDirectory[]{subdirectory});
		when(directory2.getSubdirectories()).thenReturn(new PsiDirectory[]{subdirectory});
		when(subdirectory.getFiles()).thenReturn(new PsiFile[]{});
		when(subdirectory.getSubdirectories()).thenReturn(new PsiDirectory[]{});
		when(javaFile1.getClasses()).thenReturn(classes1);
		when(javaFile2.getClasses()).thenReturn(classes2);

		Set<PsiClass> expectedClasses = new HashSet<>();
		expectedClasses.add(psiClass1);
		expectedClasses.add(psiClass2);

		Set<PsiClass> collectedClasses = classUnderTest.collectPsiClassesFromSrc(mockProject);

		assertEquals(expectedClasses, collectedClasses);
	}



	@Test
	public void shouldReturnEmptySet_givenProjectWithSourceRootsButNoPsiDirectories() {
		// given
		VirtualFile root = mock(VirtualFile.class);
		VirtualFile[] sourceRoots = {root};
		when(projectRootManager.getContentSourceRoots()).thenReturn(sourceRoots);
		when(psiManager.findDirectory(root)).thenReturn(null);

		// when
		Set<PsiClass> collectedClasses = classUnderTest.collectPsiClassesFromSrc(mockProject);

		// then
		assertTrue(collectedClasses.isEmpty());
	}

	@Test
	public void shouldRecursivelyCollectPsiClasses_givenNestedDirectoriesWithJavaFiles() {
		// given
		VirtualFile root = mock(VirtualFile.class);
		VirtualFile[] sourceRoots = {root};
		PsiDirectory directory = mock(PsiDirectory.class);
		PsiDirectory subdirectory = mock(PsiDirectory.class);
		PsiClass[] classes = {mockPsiClass};

		when(projectRootManager.getContentSourceRoots()).thenReturn(sourceRoots);
		when(psiManager.findDirectory(root)).thenReturn(directory);
		when(directory.getSubdirectories()).thenReturn(new PsiDirectory[]{subdirectory});
		when(directory.getFiles()).thenReturn(new PsiFile[]{});
		when(subdirectory.getFiles()).thenReturn(new PsiFile[]{psiJavaFile});
		when(subdirectory.getSubdirectories()).thenReturn(new PsiDirectory[]{});
		when(psiJavaFile.getClasses()).thenReturn(classes);

		when(projectRootManager.getContentSourceRoots()).thenReturn(sourceRoots);

		Set<PsiClass> expectedClasses = new HashSet<>();
		expectedClasses.add(mockPsiClass);

		// when
		Set<PsiClass> collectedClasses = classUnderTest.collectPsiClassesFromSrc(mockProject);

		// then
		assertEquals(expectedClasses, collectedClasses);
	}

	@Test
	public void shouldReturnPackageName_whenContainingFileIsPsiJavaFile_givenPsiClass() {
		// given
		when(mockPsiClass.getContainingFile()).thenReturn(psiJavaFile);
		when(psiJavaFile.getPackageName()).thenReturn("com.example");

		// when
		String packageName = classUnderTest.getPackageName(mockPsiClass);

		// then
		assertEquals("com.example", packageName);
	}

	@Test
	public void shouldReturnEmptyString_whenContainingFileIsNotPsiJavaFile_givenPsiClass() {
		// given
		when(mockPsiClass.getContainingFile()).thenReturn(psiFile); // use the already mocked PsiFile

		// when
		String packageName = classUnderTest.getPackageName(mockPsiClass);

		// then
		assertEquals("", packageName);
	}

	@Test
	public void shouldReturnClassName_whenClassHasName_givenPsiClass() {
		// given
		when(mockPsiClass.getName()).thenReturn("MyClass");

		// when
		String className = classUnderTest.getClassName(mockPsiClass);

		// then
		assertEquals("MyClass", className);
	}

	@Test
	public void shouldReturnNull_whenClassNameIsNull_givenPsiClass() {
		// given
		when(mockPsiClass.getName()).thenReturn(null);

		// when
		String className = classUnderTest.getClassName(mockPsiClass);

		// then
		assertNull(className);
	}

	@Test
	public void shouldReturnEmptyString_whenGetFilePath_givenNoContainingFile() {
		// given
		when(mockPsiClass.getContainingFile()).thenReturn(null);

		// when
		String filePath = classUnderTest.getFilePath(mockPsiClass);

		// then
		assertEquals("", filePath);
	}

	@Test
	public void shouldReturnEmptyString_whenGetFilePath_givenNoValidPsiFile() {
		//given
		when(mockPsiClass.getContainingFile()).thenReturn(psiFile);
		when(psiFile.getVirtualFile()).thenReturn(null);

		// when
		String filePath = classUnderTest.getFilePath(mockPsiClass);

		// then
		assertEquals("", filePath);
	}

	@Test
	public void shouldReturnFilePath_whenGetFilePath_givenValidPsiFile() {
		// given
		String expectedFilePath = "/path/to/file";
		when(mockPsiClass.getContainingFile()).thenReturn(psiFile);
		when(psiFile.getVirtualFile()).thenReturn(virtualFile);
		when(virtualFile.getPath()).thenReturn(expectedFilePath);

		// when
		String actualFilePath = classUnderTest.getFilePath(mockPsiClass);

		// then
		assertEquals(expectedFilePath, actualFilePath);
	}
}