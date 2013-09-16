/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Dinko Ivanov - bug 164365
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448143
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Editor Activation on DoubleClick instead of single.
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet021CellEditorsOnDoubleClick {

	private class MyEditingSupport extends EditingSupport<MyModel, List<MyModel>> {

		private boolean enabled;

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public MyEditingSupport(ColumnViewer<MyModel, List<MyModel>> viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(MyModel element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(MyModel element) {
			return enabled && element.counter % 2 == 0;
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

	public Snippet021CellEditorsOnDoubleClick(Shell shell) {
		final Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(table);
		final MyEditingSupport editingSupport = new MyEditingSupport(v);

		table.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editingSupport.setEnabled(false);
			}

		});

		table.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editingSupport.setEnabled(true);
				TableItem[] selection = table.getSelection();

				if (selection.length != 1) {
					return;
				}

				TableItem item = table.getSelection()[0];

				for (int i = 0; i < table.getColumnCount(); i++) {
					if (item.getBounds(i).contains(event.x, event.y)) {
						v.editElement(v.getStructuredSelection().getFirstElement(), i);
						editingSupport.setEnabled(false);
						break;
					}
				}
			}

		});

		TableViewerColumn<MyModel, List<MyModel>> viewerColumn = new TableViewerColumn<MyModel, List<MyModel>>(v,
				SWT.NONE);
		viewerColumn.getColumn().setWidth(200);
		viewerColumn.setLabelProvider(new ColumnLabelProvider<MyModel>());
		viewerColumn.setEditingSupport(editingSupport);

		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));

		v.setInput(createModel());
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
		new Snippet021CellEditorsOnDoubleClick(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
