/*******************************************************************************
 * Copyright (c) 2007, 2014 Marcel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel <emmpeegee@gmail.com> - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343, 442747
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 442278
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class Snippet053StartEditorWithContextMenu extends SelectionAdapter {

	private TreeViewer<MyModel, MyModel> viewer;

	private class MyContentProvider implements ITreeContentProvider<MyModel, MyModel> {

		@Override
		public MyModel[] getElements(MyModel inputElement) {
			return inputElement.child.toArray(new MyModel[inputElement.child.size()]);
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer<? extends MyModel> viewer, MyModel oldInput, MyModel newInput) {

		}

		@Override
		public MyModel[] getChildren(MyModel parentElement) {
			return getElements(parentElement);
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

	}

	public class MyModel {
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

	public class MyEditingSupport extends EditingSupport<MyModel, MyModel> {

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

	public Snippet053StartEditorWithContextMenu(Shell shell) {
		viewer = new TreeViewer<MyModel, MyModel>(shell, SWT.BORDER);
		viewer.setContentProvider(new MyContentProvider());

		TreeViewerColumn<MyModel, MyModel> viewerColumn = new TreeViewerColumn<MyModel, MyModel>(viewer, SWT.NONE);
		viewerColumn.getColumn().setWidth(200);

		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		viewerColumn.setLabelProvider(new ColumnLabelProvider<MyModel>());

		TreeViewerEditor.create(viewer, new ColumnViewerEditorActivationStrategy<MyModel, MyModel>(viewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		}, ColumnViewerEditor.DEFAULT);

		Menu menu = new Menu(viewer.getControl());
		MenuItem renameItem = new MenuItem(menu, SWT.PUSH);
		renameItem.addSelectionListener(this);
		renameItem.setText("Rename");
		viewer.getTree().setMenu(menu);

		viewer.setInput(createModel());
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		IStructuredSelection<MyModel> selection = viewer.getStructuredSelection();
		if (!selection.isEmpty()) {
			viewer.editElement(selection.getFirstElement(), 0);
		}
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

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet053StartEditorWithContextMenu(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
