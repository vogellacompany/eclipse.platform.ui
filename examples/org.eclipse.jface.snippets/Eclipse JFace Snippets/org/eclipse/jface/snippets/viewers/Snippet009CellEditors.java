/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

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
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setLabelProvider(new LabelProvider<MyModel>());
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));
		v.setCellModifier(new ICellModifier<MyModel>() {

			@Override
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem) element;
				((MyModel) item.getData()).counter = Integer.parseInt(value
						.toString());
				v.update((MyModel) item.getData(), null);
			}

			@Override
			public boolean canModify(MyModel element, String property) {
				return element.counter % 2 == 0;
			}

			@Override
			public Object getValue(MyModel element, String property) {
				return element.counter + "";
			}

		});
		v.setColumnProperties(new String[] { "column1" });
		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()) });

		List<MyModel> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>(10);
		for (int i = 0; i < 10; i++) {
			elements.add(i, new MyModel(i));
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
