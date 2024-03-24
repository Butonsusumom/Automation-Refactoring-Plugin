package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.tsybulka.autorefactoringplugin.model.metric.ClassMetricType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;

import java.util.HashMap;

import java.util.*;

import static com.tsybulka.autorefactoringplugin.model.metric.ClassMetricType.*;

/**
 * Service perform calculation of OOP metrics
 */
public class MetricsCalculationService {

	public List<ClassMetrics> calculateProjectMetrics(Project project) {
		List<ClassMetrics> classMetrics = new ArrayList<>();
		List<PsiClass> classes = getProjectClasses(project);
		for (PsiClass psiClass : classes) {
					classMetrics.add(
					new ClassMetrics(getPackageName(psiClass), getClassName(psiClass), getFilePath(psiClass), calculateOopMetrics(psiClass, project)));
		}
		return classMetrics;
	}

	public HashMap<ClassMetricType, Integer> calculateOopMetrics(PsiClass psiClass, Project project) {
		HashMap<ClassMetricType, Integer> metrics = new HashMap<>();
		metrics.put(ClassMetricType.LINES_OF_CODE, calculateLinesOfCode(psiClass));
		metrics.put(ClassMetricType.NUMBER_OF_FIELDS, calculateNumberOfFields(psiClass));
		metrics.put(ClassMetricType.NUMBER_OF_PUBLIC_FIELDS, calculateNumberOfPublicFields(psiClass));
		metrics.put(ClassMetricType.NUMBER_OF_METHODS, calculateNumberOfMethods(psiClass));
		metrics.put(ClassMetricType.NUMBER_OF_PUBLIC_METHODS, calculateNumberOfPublicMethods(psiClass));
		metrics.put(ClassMetricType.WEIGHT_METHODS, calculateWmc(psiClass));
		metrics.put(ClassMetricType.NUMBER_OF_CHILDREN, calculateNumberOfChildren(psiClass, project));
		metrics.put(ClassMetricType.DEPTH_OF_INHERITANCE_TREE, calculateDepthOfInheritanceTree(psiClass));
		metrics.put(LACK_OF_COHESION_IN_METHOD, calculateLackOfCohesionInMethods(psiClass));
		metrics.put(ClassMetricType.FAN_IN, calculateFanIn(psiClass, project));
		metrics.put(ClassMetricType.FAN_OUT, calculateFanOut(psiClass));

		return metrics;
	}

	public int calculateFanIn(PsiClass psiClass, Project project) {
		Set<PsiClass> uniqueClassesReferencing = new HashSet<>();
		for (PsiMethod method : psiClass.getMethods()) {
			Query<PsiReference> query = ReferencesSearch.search(method, GlobalSearchScope.projectScope(project));
			for (PsiReference reference : query) {
				PsiElement element = reference.getElement();
				if (element instanceof PsiMethodCallExpression) {
					PsiMethod callingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
					if (callingMethod != null) {
						PsiClass containingClass = callingMethod.getContainingClass();
						if (containingClass != null && !containingClass.equals(psiClass)) {
							uniqueClassesReferencing.add(containingClass);
						}
					}
				}
			}
		}
		return uniqueClassesReferencing.size();
	}

	public int calculateFanOut(PsiClass psiClass) {
		Set<PsiClass> uniqueClassesCalled = new HashSet<>();
		for (PsiMethod method : psiClass.getMethods()) {
			for (PsiReference reference : ReferencesSearch.search(method)) {
				PsiElement element = reference.getElement();
				if (element instanceof PsiMethodCallExpression) {
					PsiMethod calledMethod = ((PsiMethodCallExpression)element).resolveMethod();
					if (calledMethod != null) {
						PsiClass containingClass = calledMethod.getContainingClass();
						if (containingClass != null && !containingClass.equals(psiClass)) {
							uniqueClassesCalled.add(containingClass);
						}
					}
				}
			}
		}
		return uniqueClassesCalled.size();
	}

	private int calculateLackOfCohesionInMethods(PsiClass psiClass) {
		// Simplified version: Count methods that do not share fields with other methods
		// This is a very basic approximation
		int sharedFields = 0;
		int totalMethodPairs = 0;
		PsiMethod[] methods = psiClass.getMethods();

		for (int i = 0; i < methods.length; i++) {
			for (int j = i + 1; j < methods.length; j++) {
				if (methodsShareField(methods[i], methods[j])) {
					sharedFields++;
				}
				totalMethodPairs++;
			}
		}

		return totalMethodPairs - sharedFields; // Simplified LCOM
	}

	private boolean methodsShareField(PsiMethod method1, PsiMethod method2) {
		Set<String> fields1 = extractFieldNames(method1);
		Set<String> fields2 = extractFieldNames(method2);
		fields1.retainAll(fields2); // Intersection of fields1 and fields2
		return !fields1.isEmpty();
	}

	private Set<String> extractFieldNames(PsiMethod method) {
		final Set<String> fieldNames = new HashSet<>();
		method.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitReferenceExpression(PsiReferenceExpression expression) {
				super.visitReferenceExpression(expression);
				if (expression.resolve() instanceof PsiField) {
					PsiField field = (PsiField) expression.resolve();
					fieldNames.add(field.getName());
				}
			}
		});
		return fieldNames;
	}


	private int calculateLinesOfCode(PsiClass psiClass) {
		PsiFile containingFile = psiClass.getContainingFile();
		return (containingFile == null) ? 0 : StringUtil.countNewLines(containingFile.getText()) + 1;
	}

	private int calculateNumberOfFields(PsiClass psiClass) {
		return psiClass.getFields().length;
	}

	private int calculateNumberOfPublicFields(PsiClass psiClass) {
		int count = 0;
		for (PsiField field : psiClass.getFields()) {
			if (field.hasModifierProperty(PsiModifier.PUBLIC)) {
				count++;
			}
		}
		return count;
	}

	private int calculateNumberOfMethods(PsiClass psiClass) {
		return psiClass.getMethods().length;
	}

	private int calculateNumberOfPublicMethods(PsiClass psiClass) {
		int count = 0;
		for (PsiMethod method : psiClass.getMethods()) {
			if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
				count++;
			}
		}
		return count;
	}

	// Example for Weight of Class (WMC), simplified as total methods count here.
	private int calculateWmc(PsiClass psiClass) {
		return psiClass.getMethods().length; // Simplification, should be refined
	}

	private int calculateNumberOfChildren(PsiClass psiClass, Project project) {
		// Use GlobalSearchScope.allScope(project) to search the entire project
		Query<PsiClass> query = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.allScope(project), true);
		return query.findAll().size();
	}
	private int calculateDepthOfInheritanceTree(PsiClass psiClass) {
		int depth = 0;
		PsiClass current = psiClass;
		while (current != null) {
			depth++;
			current = current.getSuperClass();
		}
		return depth - 1; // Subtract 1 to exclude the class itself
	}


	List<PsiClass> getProjectClasses(Project project) {
		List<PsiClass> userClasses = new ArrayList<>();
		PsiManager psiManager = PsiManager.getInstance(project);
		ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

		Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.allScope(project));

		for (VirtualFile virtualFile : virtualFiles) {
			// Check if the file is under source content and not in libraries
			if (fileIndex.isInSourceContent(virtualFile) && !fileIndex.isInLibrary(virtualFile)) {
				PsiFile psiFile = psiManager.findFile(virtualFile);
				if (psiFile != null) {
					// Collect only class definitions
					for (PsiClass psiClass : PsiTreeUtil.findChildrenOfType(psiFile, PsiClass.class)) {
						// Ensure the PsiClass is directly defined in the file, not referenced
						if (psiClass.getParent() instanceof PsiFile) {
							userClasses.add(psiClass);
						}
					}
				}
			}
		}
		return userClasses;
	}

	public static String getPackageName(PsiClass psiClass) {
		// Get the containing file of the PsiClass
		PsiFile containingFile = psiClass.getContainingFile();
		if (containingFile instanceof PsiJavaFile) {
			// Cast to PsiJavaFile to access the getPackageName method
			PsiJavaFile javaFile = (PsiJavaFile) containingFile;
			return javaFile.getPackageName();
		}
		return "";
	}

	static String getClassName(PsiClass psiClass) {
		return psiClass.getName();
	}

	String getFilePath(PsiClass psiClass) {
		PsiFile containingFile = psiClass.getContainingFile();
		if (containingFile != null) {
			VirtualFile virtualFile = containingFile.getVirtualFile();
			if (virtualFile != null) {
				return virtualFile.getPath(); // Returns the full path to the file
			}
		}
		return "";
	}
}