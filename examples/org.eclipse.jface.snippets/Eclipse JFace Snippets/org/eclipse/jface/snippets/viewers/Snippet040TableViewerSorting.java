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
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Jeanderson Candido (http://jeandersonbc.github.io) - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Example usage of ViewerComparator in tables to allow sorting
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet040TableViewerSorting {

	public class Person {
		public String givenname;
		public String surname;
		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}
	}

	protected abstract class AbstractEditingSupport extends
			EditingSupport<Person, List<Person>> {
		private TextCellEditor editor;

		public AbstractEditingSupport(TableViewer<Person, List<Person>> viewer) {
			super(viewer);
			this.editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Person element) {
			return true;
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

		protected abstract void doSetValue(Object element, Object value);
	}

	public Snippet040TableViewerSorting(Shell shell) {

		TableViewer<Person, List<Person>> viewer = new TableViewer<Person, List<Person>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(ArrayContentProvider
				.getInstance(Person.class));

		TableViewerColumn<Person, List<Person>> column = createColumnFor(
				viewer, "Givenname");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.givenname;
			}
		});

		column.setEditingSupport(new AbstractEditingSupport(viewer) {

			@Override
			protected Object getValue(Person element) {
				return element.givenname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).givenname = value.toString();
			}

		});

		ColumnViewerSorter cSorter = new ColumnViewerSorter(viewer, column) {

			@Override
			protected int doCompare(Viewer<List<Person>> viewer, Person e1,
					Person e2) {
				return e1.givenname.compareToIgnoreCase(e2.givenname);
			}

		};

		column = createColumnFor(viewer, "Surname");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.surname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(viewer) {

			@Override
			protected Object getValue(Person element) {
				return element.surname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).surname = value.toString();
			}

		});

		new ColumnViewerSorter(viewer, column) {

			@Override
			protected int doCompare(Viewer<List<Person>> viewer, Person e1,
					Person e2) {
				return e1.surname.compareToIgnoreCase(e2.surname);
			}

		};

		column = createColumnFor(viewer, "E-Mail");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.email;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(viewer) {

			@Override
			protected Object getValue(Person element) {
				return element.email;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).email = value.toString();
			}

		});

		new ColumnViewerSorter(viewer, column) {

			@Override
			protected int doCompare(Viewer<List<Person>> viewer, Person e1,
					Person e2) {
				return e1.email.compareToIgnoreCase(e2.email);
			}

		};

		viewer.setInput(createModel());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		cSorter.setSorter(cSorter, ColumnViewerSorter.ASC);
	}

	private TableViewerColumn<Person, List<Person>> createColumnFor(
			TableViewer<Person, List<Person>> viewer, String label) {
		TableViewerColumn<Person, List<Person>> column = new TableViewerColumn<Person, List<Person>>(
				viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);

		return column;
	}

	private List<Person> createModel() {
		return Arrays.asList(new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at"), new Person("Boris", "Bokowski",
				"Boris_Bokowski@ca.ibm.com"), new Person("Tod", "Creasey",
				"Tod_Creasey@ca.ibm.com"), new Person("Wayne", "Beaton",
				"wayne@eclipse.org"), new Person("Jeanderson", "Candido",
				"jeandersonbc@gmail.com"), new Person("Lars", "Vogel",
				"Lars.Vogel@gmail.com"), new Person("Hendrik", "Still",
				"hendrik.still@gammas.de"));
	}

	private static abstract class ColumnViewerSorter extends
			ViewerComparator<Person, List<Person>> {

		public static final int ASC = 1;
		public static final int NONE = 0;
		public static final int DESC = -1;

		private int direction = 0;
		private TableViewerColumn<Person, List<Person>> column;
		private ColumnViewer<Person, List<Person>> viewer;

		public ColumnViewerSorter(ColumnViewer<Person, List<Person>> viewer,
				TableViewerColumn<Person, List<Person>> column) {
			this.column = column;
			this.viewer = viewer;
			SelectionAdapter selectionAdapter = createSelectionAdapter();
			this.column.getColumn().addSelectionListener(selectionAdapter);
		}

		private SelectionAdapter createSelectionAdapter() {
			return new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (ColumnViewerSorter.this.viewer.getComparator() != null) {
						if (ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this) {
							int tdirection = ColumnViewerSorter.this.direction;
							if (tdirection == ASC) {
								setSorter(ColumnViewerSorter.this, DESC);
							} else if (tdirection == DESC) {
								setSorter(ColumnViewerSorter.this, NONE);
							}
						} else {
							setSorter(ColumnViewerSorter.this, ASC);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				}
			};
		}

		public void setSorter(ColumnViewerSorter sorter, int direction) {
			Table columnParent = column.getColumn().getParent();
			if (direction == NONE) {
				columnParent.setSortColumn(null);
				columnParent.setSortDirection(SWT.NONE);
				viewer.setComparator(null);

			} else {
				columnParent.setSortColumn(column.getColumn());
				sorter.direction = direction;
				columnParent.setSortDirection(direction == ASC ? SWT.DOWN
						: SWT.UP);

				if (viewer.getComparator() == sorter) {
					viewer.refresh();
				} else {
					viewer.setComparator(sorter);
				}

			}
		}

		@Override
		public int compare(Viewer<List<Person>> viewer, Person e1, Person e2) {
			return direction * doCompare(viewer, e1, e2);
		}

		protected abstract int doCompare(Viewer<List<Person>> viewer,
				Person e1, Person e2);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet040TableViewerSorting(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
