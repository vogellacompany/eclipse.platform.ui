/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 402445 - [Viewers] Add generics to the JFace Viewer framework
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetLabelProvider extends LabelProvider<IWorkingSet> {
	private ResourceManager images;

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkingSetLabelProvider() {
		images = new LocalResourceManager(JFaceResources.getResources());
	}

	@Override
	public void dispose() {
		images.dispose();

		super.dispose();
	}

	@Override
	public Image getImage(IWorkingSet object) {
		ImageDescriptor imageDescriptor = object.getImageDescriptor();
		if (imageDescriptor == null) {
			return null;
		}

		Image icon = (Image) images.get(imageDescriptor);
		return icon;
	}

	@Override
	public String getText(IWorkingSet object) {
		return object.getLabel();
	}
}
