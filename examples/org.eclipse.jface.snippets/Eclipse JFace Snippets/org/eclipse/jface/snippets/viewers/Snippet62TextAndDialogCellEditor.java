/*******************************************************************************
 * Copyright (c) 2006, 2014 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Hendrik Still <hendrik.still@gammas.de> - Bug 417676
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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates usage of {@link TextAndDialogCellEditor}. The email column uses
 * the TextAndDialogCellEditor; othe columns use ordinary {@link TextCellEditor}
 * s.
 *
 * @author Eric Rizzo
 *
 */
public class Snippet62TextAndDialogCellEditor {

	public class Person {
		public String givenname;
		public String surname;
		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}

		@Override
		public String toString() {
			return '[' + givenname + ' ' + surname + ' ' + email + ']';
		}
	}

	protected abstract class AbstractEditingSupport extends
			EditingSupport<Person, List<Person>> {
		private CellEditor editor;

		public AbstractEditingSupport(TableViewer<Person, List<Person>> viewer,
				CellEditor anEditor) {
			super(viewer);
			this.editor = anEditor;
		}

		@Override
		protected boolean canEdit(Person element) {
			return editor != null;
		}

		@Override
		protected CellEditor getCellEditor(Person element) {
			return editor;
		}

		@Override
		protected void setValue(Person element, Object value) {
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		protected abstract void doSetValue(Person element, Object value);
	}

	public Snippet62TextAndDialogCellEditor(Shell shell) {
		TableViewer<Person, List<Person>> v = new TableViewer<Person, List<Person>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance(Person.class));

		TableViewerColumn<Person, List<Person>> column = new TableViewerColumn<Person, List<Person>>(
				v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Givenname");
		column.getColumn().setMoveable(true);
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.givenname;
			}
		});

		column.setEditingSupport(new AbstractEditingSupport(v,
				new TextCellEditor(v.getTable())) {

			@Override
			protected Object getValue(Person element) {
				return element.givenname;
			}

			@Override
			protected void doSetValue(Person element, Object value) {
				element.givenname = value.toString();
			}

		});

		column = new TableViewerColumn<Person, List<Person>>(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Surname");
		column.getColumn().setMoveable(true);
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.surname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(v,
				new TextCellEditor(v.getTable())) {
			@Override
			protected Object getValue(Person element) {
				return element.surname;
			}

			@Override
			protected void doSetValue(Person element, Object value) {
				element.surname = value.toString();
			}

		});

		column = new TableViewerColumn<Person, List<Person>>(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("E-Mail");
		column.getColumn().setMoveable(true);
    column.setLabelProvider(new ColumnLabelProvider<Person>() {
			@Override
			public String getText(Person element) {
				return element.email;
			}

		});

		TextAndDialogCellEditor cellEditor = new TextAndDialogCellEditor(
				v.getTable());
		cellEditor.setDialogMessage("Enter email address");
		column.setEditingSupport(new AbstractEditingSupport(v, cellEditor) {

			@Override
			protected Object getValue(Person element) {
				return element.email;
			}

			@Override
			protected void doSetValue(Person element, Object value) {
				element.email = value.toString();
			}

			// Print out the model after each edit to verify its values are
			// updated correctly
			@Override
			protected void saveCellEditorValue(CellEditor cellEditor,
					ViewerCell<Person> cell) {
				super.saveCellEditorValue(cellEditor, cell);
				System.out.println(cell.getElement());
			}
		});

		List<Person> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private List<Person> createModel() {
		List<Person> elements = new ArrayList<Person>(4);
		elements.add(new Person("Tom", "Schindl", "tom.schindl@bestsolution.at"));
		elements.add(new Person("Tod", "Creasey", "Tod_Creasey@ca.ibm.com"));
		elements.add(new Person("Wayne", "Beaton", "wayne@eclipse.org"));
		elements.add(new Person("Jeanderson", "Beaton", "wayne@eclipse.org"));
		elements.add(new Person("Lars", "Vogel", "lars.vogel@gmail.com"));
		elements.add(new Person("Hendrik", "Still", "hendrik.still@gammas.de"));
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet62TextAndDialogCellEditor(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
