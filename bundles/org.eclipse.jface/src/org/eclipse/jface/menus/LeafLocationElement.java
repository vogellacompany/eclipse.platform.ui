/******************************************************************************* * Copyright (c) 2005 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.jface.menus;/** * <p> * A leaf element within a location. This provides the most specific piece of * information as to where the menu element should appear. The <code>path</code> * specifies where within the popup menu the menu element should be placed. * </p> * <p> * Clients must not implement or extend. * </p> * <p> * <strong>EXPERIMENTAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> *  * @since 3.2 * @see org.eclipse.jface.menus.SBar * @see org.eclipse.jface.menus.SPopup */public abstract class LeafLocationElement implements LocationElement {	/**	 * The path separator used to separate menu containers.	 */	public static final char PATH_SEPARATOR = '/';	/**	 * The path within this location element to the final location. The path is	 * a slash-delimited list of menu elements.	 */	private final String path;	/**	 * Constructs a new instance of <code>LeafLocationElement</code>.	 * 	 * @param path	 *            The path to the final location. If this value is	 *            <code>null</code>, it means that it should be inserted at	 *            the top-level of the location.	 */	public LeafLocationElement(final String path) {		this.path = path;	}	/**	 * Returns the full path for this location. The path is a slash-delimited	 * list of menu elements.	 * 	 * @return The full path. If this value is <code>null</code>, it means	 *         that it should be inserted at the top-level of the location.	 */	public final String getPath() {		return path;	}}