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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448143
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * TableViewer: Hide full selection
 *
 */
public class Snippet007FullSelection {

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

	public Snippet007FullSelection(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(100);
		column.setText("Column 1");
		TableViewerColumn<MyModel, List<MyModel>> viewerColumn1 = new TableViewerColumn<MyModel, List<MyModel>>(v,
				column);
		viewerColumn1.setLabelProvider(new ColumnLabelProvider<MyModel>());
		viewerColumn1.setEditingSupport(new EditColumns(v));

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(100);
		column.setText("Column 2");
		TableViewerColumn<MyModel, List<MyModel>> viewerColumn2 = new TableViewerColumn<MyModel, List<MyModel>>(v,
				column);
		viewerColumn2.setLabelProvider(new ColumnLabelProvider<MyModel>());
		viewerColumn2.setEditingSupport(new EditColumns(v));

		List<MyModel> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		v.getTable().addListener(SWT.EraseItem, new Listener() {

			@Override
			public void handleEvent(Event event) {
				event.detail &= ~SWT.SELECTED;
			}
		});

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
		new Snippet007FullSelection(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

	private class EditColumns extends EditingSupport<MyModel, List<MyModel>> {

		public EditColumns(ColumnViewer<MyModel, List<MyModel>> viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(MyModel element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(MyModel element) {
			return true;
		}

		@Override
		protected Object getValue(MyModel element) {
			return element.counter + "";
		}

		@Override
		protected void setValue(MyModel element, Object value) {
			element.counter = Integer.parseInt(value.toString());
			getViewer().update(element, null);
		}

	}

}
