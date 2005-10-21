/******************************************************************************* * Copyright (c) 2005 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.jface.menus;import org.eclipse.core.commands.common.NotDefinedException;import org.eclipse.jface.util.ListenerList;import org.eclipse.jface.util.Util;/** * <p> * A logical grouping of menu items and widgets. This grouping can also take on * a physical appearance in the form of separators. * </p> * <p> * Clients may instantiate this class, but must not extend. * </p> * <p> * <strong>EXPERIMENTAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> *  * @since 3.2 */public final class SGroup extends MenuElement {	/**	 * The class providing dynamic menu elements to this group. If this value is	 * <code>null</code>, then there are no dynamic elements.	 */	private IDynamicMenu dynamic;	/**	 * The locations in which this menu element appears. This value may be empty	 * or <code>null</code>.	 */	private SLocation[] locations;	/**	 * Whether separators should be drawn before and after this group, as	 * appropriate.	 */	public boolean separatorsVisible = true;	/**	 * Constructs a new instance of <code>SGroup</code>.	 * 	 * @param id	 *            The identifier of the group to create; must not be	 *            <code>null</code>	 */	public SGroup(final String id) {		super(id);	}	/**	 * Adds a listener to this group that will be notified when this group's	 * state changes.	 * 	 * @param listener	 *            The listener to be added; must not be <code>null</code>.	 */	public final void addListener(final IGroupListener listener) {		if (listenerList == null) {			listenerList = new ListenerList(1);		}		listenerList.add(listener);	}	/**	 * <p>	 * Defines this group by indicating whether the separators are visible. The	 * locations and dynamic menu are optional. The defined property	 * automatically becomes <code>true</code>.	 * </p>	 * 	 * @param separatorsVisible	 *            Whether separators should be drawn before and after this	 *            group, as appropriate.	 * @param locations	 *            The locations in which this group will appear; may be	 *            <code>null</code> or empty.	 * @param dynamic	 *            The class providing dynamic menu elements to this group; may	 *            be <code>null</code>.	 */	public final void define(final boolean separatorsVisible,			SLocation[] locations, final IDynamicMenu dynamic) {		if ((locations != null) && (locations.length == 0)) {			locations = null;		}		GroupEvent event = null;		if (isListenerAttached()) {			final boolean separatorsChanged = this.separatorsVisible != separatorsVisible;			final boolean locationsChanged = !Util.equals(this.locations,					locations);			final boolean dynamicChanged = !Util.equals(this.dynamic, dynamic);			final boolean definedChanged = !this.defined;			event = new GroupEvent(this, separatorsChanged, locationsChanged,					dynamicChanged, definedChanged);		}		this.separatorsVisible = separatorsVisible;		this.locations = locations;		this.dynamic = dynamic;		this.defined = true;		fireGroupChanged(event);	}	/**	 * Tests whether this group is equal to another object. A group is only	 * equal to another group with the same id.	 * 	 * @param object	 *            The object with which to compare; may be <code>null</code>.	 * @return <code>true</code> if the groups are equal; <code>false</code>	 *         otherwise.	 */	public final boolean equals(final Object object) {		// Check if they're the same.		if (object == this) {			return true;		}		// Check if they're the same type.		if (!(object instanceof SGroup))			return false;		// Check each property in turn.		final SGroup group = (SGroup) object;		return Util.equals(id, group.id);	}	/**	 * Notifies listeners to this group that it has changed in some way.	 * 	 * @param event	 *            The event to fire; may be <code>null</code>.	 */	private final void fireGroupChanged(final GroupEvent event) {		if (event == null) {			return;		}		if (listenerList != null) {			final Object[] listeners = listenerList.getListeners();			for (int i = 0; i < listeners.length; i++) {				final IGroupListener listener = (IGroupListener) listeners[i];				listener.groupChanged(event);			}		}	}	/**	 * Returns the class generating dynamic menu elements for this group.	 * 	 * @return The class generating dynamic menu elements for this group; never	 *         <code>null</code>.	 * @throws NotDefinedException	 *             If the handle is not currently defined.	 */	public final IDynamicMenu getDynamic() throws NotDefinedException {		if (!isDefined()) {			throw new NotDefinedException(					"Cannot get the dynamic class from an undefined group"); //$NON-NLS-1$		}		return dynamic;	}	/**	 * Returns the locations for this group. This performs a copy of the	 * internal data structure.	 * 	 * @return The locations for this group; never <code>null</code>.	 * @throws NotDefinedException	 *             If the handle is not currently defined.	 */	public final SLocation[] getLocations() throws NotDefinedException {		if (!isDefined()) {			throw new NotDefinedException(					"Cannot get the locations from an undefined group"); //$NON-NLS-1$		}		final SLocation[] result = new SLocation[locations.length];		System.arraycopy(locations, 0, result, 0, locations.length);		return result;	}	/**	 * Whether separators should be drawn around the group.	 * 	 * @return <code>true</code> if the separators should be drawn;	 *         <code>false</code> otherwise.	 * @throws NotDefinedException	 *             If the handle is not currently defined.	 */	public final boolean isSeparatorsVisible() throws NotDefinedException {		if (!isDefined()) {			throw new NotDefinedException(					"Cannot get whether the separators are visible from an undefined group"); //$NON-NLS-1$		}		return separatorsVisible;	}	/**	 * Removes a listener from this group.	 * 	 * @param listener	 *            The listener to be removed; must not be <code>null</code>.	 */	public final void removeListener(final IGroupListener listener) {		if (listenerList != null) {			listenerList.remove(listener);		}		if (listenerList.isEmpty()) {			listenerList = null;		}	}	/**	 * The string representation of this group -- for debugging purposes only.	 * This string should not be shown to an end user.	 * 	 * @return The string representation; never <code>null</code>.	 */	public final String toString() {		if (string == null) {			final StringBuffer stringBuffer = new StringBuffer();			stringBuffer.append("SGroup("); //$NON-NLS-1$			stringBuffer.append(id);			stringBuffer.append(',');			stringBuffer.append(separatorsVisible);			stringBuffer.append(',');			stringBuffer.append(locations);			stringBuffer.append(',');			try {				stringBuffer.append(dynamic);			} catch (final Exception e) {				// A bogus toString() in third-party code. Ignore.				stringBuffer.append(e.getClass().getName());			}			stringBuffer.append(',');			stringBuffer.append(defined);			stringBuffer.append(')');			string = stringBuffer.toString();		}		return string;	}	/**	 * Makes this group become undefined. This has the side effect of changing	 * the locations and dynamic class to <code>null</code>. Notification is	 * sent to all listeners.	 */	public final void undefine() {		string = null;		GroupEvent event = null;		if (isListenerAttached()) {			final boolean separatorsChanged = this.separatorsVisible != true;			final boolean locationsChanged = locations != null;			final boolean dynamicChanged = dynamic != null;			final boolean definedChanged = this.defined;			event = new GroupEvent(this, separatorsChanged, locationsChanged,					dynamicChanged, definedChanged);		}		defined = false;		separatorsVisible = true;		locations = null;		dynamic = null;		fireGroupChanged(event);	}}