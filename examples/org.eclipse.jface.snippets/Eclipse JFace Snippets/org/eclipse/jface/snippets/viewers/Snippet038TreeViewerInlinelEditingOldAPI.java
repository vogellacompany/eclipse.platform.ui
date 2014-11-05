/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442747
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * A simple TreeViewer to demonstrate usage of inline editing
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet038TreeViewerInlinelEditingOldAPI {
	private class MyContentProvider implements ITreeContentProvider<MyModel, MyModel> {

		@Override
		public void dispose() {

		}

		@Override
		public MyModel getParent(MyModel element) {
			if (element == null) {
				return null;
			}

			return element.parent;
		}

		@Override
		public boolean hasChildren(MyModel element) {
			return element.child.size() > 0;
		}


		@Override
		public MyModel[] getChildren(MyModel parentElement) {
			return getElements(parentElement);
		}

		@Override
		public void inputChanged(Viewer<? extends MyModel> viewer, MyModel oldInput, MyModel newInput) {

		}

		@Override
		public MyModel[] getElements(MyModel inputElement) {
			return inputElement.child.toArray(new MyModel[inputElement.child.size()]);
		}
	}

	private class MyModel {
		public MyModel parent;
		public List<MyModel> child = new ArrayList<MyModel>();
		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}
			rv += counter;

			return rv;
		}
	}

	private class MyColumnLabelProvider extends ColumnLabelProvider<MyModel> {

		private int columnIndex;
		private Tree tree;

		public MyColumnLabelProvider(Tree tree, int columnIndex) {
			this.tree = tree;
			this.columnIndex = columnIndex;
		}

		@Override
		public String getText(MyModel element) {
			return "Column " + tree.getColumnOrder()[columnIndex] + " => " + element.toString();
		}
	}

	private class MyEditingSupport extends EditingSupport<MyModel, MyModel> {

		public MyEditingSupport(ColumnViewer<MyModel, MyModel> viewer) {
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

	public Snippet038TreeViewerInlinelEditingOldAPI(Shell shell) {
		final TreeViewer<MyModel, MyModel> viewer = new TreeViewer<MyModel, MyModel>(shell, SWT.FULL_SELECTION);

		createColumnFor(viewer, "Column 1", 0);
		createColumnFor(viewer, "Column 2", 1);

		viewer.setContentProvider(new MyContentProvider());
		viewer.setInput(createModel());
	}

	private MyModel createModel() {
		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				tmp.child.add(new MyModel(j, tmp));
			}
		}
		return root;
	}

	private void createColumnFor(TreeViewer<MyModel, MyModel> viewer, String label, int columnIndex) {
		TreeViewerColumn<MyModel, MyModel> viewerColumn = new TreeViewerColumn<MyModel, MyModel>(viewer, SWT.NONE);
		viewerColumn.getColumn().setWidth(200);
		viewerColumn.getColumn().setText(label);

		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		viewerColumn.setLabelProvider(new MyColumnLabelProvider(viewer.getTree(), columnIndex));
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet038TreeViewerInlinelEditingOldAPI(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
