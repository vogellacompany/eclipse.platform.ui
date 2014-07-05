/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     IBM - Improvement for Bug 159625 [Snippets] Update Snippet011CustomTooltips to reflect new API
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet011CustomTooltips {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		TableViewer<String, Object> v = new TableViewer<String, Object>(shell,
				SWT.FULL_SELECTION);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		v.setContentProvider(ArrayContentProvider.getInstance(String.class));
		ColumnViewerToolTipSupport.enableFor(v, ToolTip.NO_RECREATE);
    CellLabelProvider<String> labelProvider = new CellLabelProvider<String>() {

			@Override
      public String getToolTipText(String element) {
				return "Tooltip (" + element + ")";
			}

			@Override
			public Point getToolTipShift(String object) {
				return new Point(5, 5);
			}

			@Override
			public int getToolTipDisplayDelayTime(String object) {
				return 2000;
			}

			@Override
			public int getToolTipTimeDisplayed(String object) {
				return 5000;
			}

			@Override
			public void update(ViewerCell<String> cell) {
				cell.setText(cell.getElement().toString());

			}
		};

		TableViewerColumn<String, Object> column = new TableViewerColumn<String, Object>(
				v, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Column 1");
		column.getColumn().setWidth(100);
		String[] values = new String[] { "one", "two", "three", "four", "five",
				"six", "seven", "eight", "nine", "ten" };
		v.setInput(values);

		shell.setSize(200, 200);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

}
