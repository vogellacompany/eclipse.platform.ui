/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The LazyVirtualTableView is the VirtualTableView with lazy content.
 */
public class LazyVirtualTableView extends VirtualTableView {

	private List<String> elements;

	/**
	 * Create a new instance of the receiver.
	 */
	public LazyVirtualTableView() {
		super();
		initElements();
	}

	/**
	 *
	 */
	private void initElements() {
		elements = new ArrayList<String>();
		for (int i = 0; i < itemCount; i++) {
			elements.add("Element " + String.valueOf(i));
		}
	}

	@Override
	protected IContentProvider<Object> getContentProvider() {
		return new ILazyContentProvider<Object>() {

			@Override
			public void updateElement(int index) {
				viewer.replace(elements.get(index), index);
			}

			@Override
			public void dispose() {
				// Do Nothing
			}

			@Override
			public void inputChanged(Viewer<? extends Object> viewer, Object oldInput, Object newInput) {
				// Do nothing.	
			}
		};
	}

	@Override
	protected void doRemove(String[] selection, int[] selectionIndices) {
		for (int i = 0; i < selectionIndices.length; i++) {
			int index = selectionIndices[i];
			elements.remove(index);
		}
		super.doRemove(selection, selectionIndices);
	}

	@Override
	protected void resetInput() {
		viewer.setItemCount(itemCount);
		super.resetInput();
	}
}
