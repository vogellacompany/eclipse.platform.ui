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
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TreeViewer to demonstrate usage
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet026TreeViewerTabEditing {
	public Snippet026TreeViewerTabEditing(final Shell shell) {
		Button b = new Button(shell, SWT.PUSH);
		b.setText("Remove the second column");
		final TreeViewer<MyModel, MyModel> v = new TreeViewer<MyModel, MyModel>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.getTree().setLinesVisible(true);
		v.getTree().setHeaderVisible(true);
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int columnCount = v.getTree().getColumnCount();
				if (columnCount > 1)
					v.getTree().getColumn(1).dispose();
			}

		});

		TreeViewerFocusCellManager<MyModel, MyModel> focusCellManager = new TreeViewerFocusCellManager<MyModel, MyModel>(
				v, new FocusCellOwnerDrawHighlighter<MyModel, MyModel>(v));
		ColumnViewerEditorActivationStrategy<MyModel, MyModel> actSupport = new ColumnViewerEditorActivationStrategy<MyModel, MyModel>(
				v) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		int feature = ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TreeViewerEditor.create(v, focusCellManager, actSupport, feature);
		final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());

		String[] columLabels = { "Column 1", "Column 2", "Column 3" };
		String[] labelPrefix = { "Column 1 => ", "Column 2 => ", "Column 3 => " };

		for (int i = 0; i < columLabels.length; i++) {
			TreeViewerColumn<MyModel, MyModel> column = new TreeViewerColumn<MyModel, MyModel>(
					v, SWT.NONE);
			column.getColumn().setWidth(200);
			column.getColumn().setMoveable(true);
			column.getColumn().setText(columLabels[i]);
			column.setLabelProvider(createColumnLabelProvider(labelPrefix[i]));
			column.setEditingSupport(createEditingSupportFor(v, textCellEditor));
		}
		v.setContentProvider(new MyContentProvider());
		v.setInput(createModel());
	}

  private ColumnLabelProvider<MyModel> createColumnLabelProvider(
			final String prefix) {
    return new ColumnLabelProvider<MyModel>() {

			@Override
			public String getText(MyModel element) {
				return prefix + element.toString();
			}

		};
	}

	private EditingSupport<MyModel, MyModel> createEditingSupportFor(
			final TreeViewer<MyModel, MyModel> viewer,
			final TextCellEditor textCellEditor) {
		return new EditingSupport<MyModel, MyModel>(viewer) {

			@Override
			protected CellEditor getCellEditor(MyModel element) {
				return textCellEditor;
			}

			@Override
			protected boolean canEdit(MyModel element) {
				return false;
			}

			@Override
			protected Object getValue(MyModel element) {
				return element.counter + "";
			}

			@Override
			protected void setValue(MyModel element, Object value) {
				element.counter = Integer
						.parseInt(value.toString());
				viewer.update(element, null);
				viewer.update(element, null);
			}
		};
	}

	private MyModel createModel() {
		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		MyModel subItem;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				subItem = new MyModel(j, tmp);
				subItem.child.add(new MyModel(j * 100, subItem));
				tmp.child.add(subItem);
			}
		}
		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet026TreeViewerTabEditing(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private class MyContentProvider implements
			ITreeContentProvider<MyModel, MyModel> {

		@Override
		public MyModel[] getElements(MyModel inputElement) {
			MyModel[] myModels = new MyModel[inputElement.child.size()];
			return inputElement.child.toArray(myModels);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer<? extends MyModel> viewer,
				MyModel oldInput, MyModel newInput) {

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

}
