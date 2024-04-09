package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetricsCalculationServiceUnitTest {
	@Mock
	private PsiClass psiClass;

	@Mock
	private PsiFile psiFile;

	@Mock
	private VirtualFile virtualFile;


	@Test
	public void testGetFilePath_ContainingFileIsNull() {
		MetricsCalculationService service = new MetricsCalculationService();
		when(psiClass.getContainingFile()).thenReturn(null);

		String filePath = service.getFilePath(psiClass);

		assertEquals("", filePath);
	}

	@Test
	public void testGetFilePath_VirtualFileIsNull() {
		MetricsCalculationService service = new MetricsCalculationService();
		when(psiClass.getContainingFile()).thenReturn(psiFile);
		when(psiFile.getVirtualFile()).thenReturn(null);

		String filePath = service.getFilePath(psiClass);

		assertEquals("", filePath);
	}

	@Test
	public void testGetFilePath_Success() {
		MetricsCalculationService service = new MetricsCalculationService();
		String expectedFilePath = "/path/to/file";
		when(psiClass.getContainingFile()).thenReturn(psiFile);
		when(psiFile.getVirtualFile()).thenReturn(virtualFile);
		when(virtualFile.getPath()).thenReturn(expectedFilePath);

		String filePath = service.getFilePath(psiClass);

		assertEquals(expectedFilePath, filePath);
	}
}