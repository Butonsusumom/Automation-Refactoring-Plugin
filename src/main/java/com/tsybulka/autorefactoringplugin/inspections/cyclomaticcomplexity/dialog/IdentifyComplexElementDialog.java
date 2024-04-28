package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class IdentifyComplexElementDialog extends DialogWrapper {

	private JPanel myMainPane;
	private JTextArea codeTextArea;

	public IdentifyComplexElementDialog(@Nullable Project project, boolean canBeParent) {
		super(project, canBeParent);
		setTitle("Extract Element into Method");
		init();
	}

	protected void init() {
		super.init();
	}

	public void setElement(PsiElement element) {
		setLabelContent(element.getText());
	}

	public void setElement(PsiElement[] elements) {
		StringBuilder contentBuilder = new StringBuilder();
		for (PsiElement element : elements) {
			contentBuilder.append(element.getText()).append(" ");
		}
		setLabelContent(contentBuilder.toString());
	}

	private void setLabelContent(String content) {
		codeTextArea.setText(content);
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return myMainPane;
	}
}
