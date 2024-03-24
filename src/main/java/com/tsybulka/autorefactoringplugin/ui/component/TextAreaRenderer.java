package com.tsybulka.autorefactoringplugin.ui.component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextAreaRenderer extends JScrollPane implements TableCellRenderer {
	JTextArea textarea;

	public TextAreaRenderer() {
		textarea = new JTextArea();
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setFont(new JLabel().getFont());
		textarea.setBorder(null); // No border for the text area itself
		getViewport().setBorder(null); // No border for the scroll pane viewport
		setBorder(null); // No border for the scroll pane
		getViewport().add(textarea);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
												   boolean isSelected, boolean hasFocus,
												   int row, int column) {
		// Match the font with the table's default font
		textarea.setFont(table.getFont());

		// Match the text area's foreground and background with the table's
		if (isSelected) {
			textarea.setForeground(table.getSelectionForeground());
			textarea.setBackground(table.getSelectionBackground());
		} else {
			textarea.setForeground(table.getForeground());
			textarea.setBackground(table.getBackground());
		}

		if (value != null) {
			textarea.setText(value.toString());
		} else {
			textarea.setText("");
		}
		adjustRowHeight(table, row, column);
		return this;
	}

	// Method to adjust the row height
	private void adjustRowHeight(JTable table, int row, int column) {
		int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
		setSize(new Dimension(cWidth, 1000));  // Set a large assumed height for calculation
		int prefH = getPreferredSize().height; // Get the preferred height with the current width

		// Adjust the height based on the text area's content
		// If there's an extra line being considered, we might subtract the height of a single line
		FontMetrics metrics = textarea.getFontMetrics(textarea.getFont());
		int lineHeight = metrics.getHeight();
		if (textarea.getLineCount() > 1 && textarea.getText().endsWith("\n")) {
			prefH -= lineHeight; // Adjust for the extra line height if the last line is empty
		}

		while (row < table.getRowCount() && table.getRowHeight(row) < prefH + 2) {
			table.setRowHeight(row, prefH + 2);
		}
	}
}
