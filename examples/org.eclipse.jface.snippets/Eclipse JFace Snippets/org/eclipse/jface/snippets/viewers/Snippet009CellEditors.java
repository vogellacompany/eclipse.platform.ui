/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343, 448143
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Edit cell values in a table
 *
 */
public class Snippet009CellEditors {
	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet009CellEditors(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));

		TableViewerColumn<MyModel, List<MyModel>> viewerColumn = new TableViewerColumn<MyModel, List<MyModel>>(v,
				SWT.NONE);
		viewerColumn.getColumn().setText("Column1");
		viewerColumn.getColumn().setWidth(300);
		viewerColumn.setLabelProvider(new ColumnLabelProvider<MyModel>());
		viewerColumn.setEditingSupport(new EditingSupport<MyModel, List<MyModel>>(v) {

			@Override
			protected void setValue(MyModel element, Object value) {
				element.counter = Integer.parseInt(value.toString());
				getViewer().update(element, null);
			}

			@Override
			protected Object getValue(MyModel element) {
				return element.counter + "";
			}

			@Override
			protected CellEditor getCellEditor(MyModel element) {
				return new TextCellEditor((Composite) getViewer().getControl());
			}

			@Override
			protected boolean canEdit(MyModel element) {
				return element.counter % 2 == 0;
			}
		});

		List<MyModel> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();
		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
		}

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet009CellEditors(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
