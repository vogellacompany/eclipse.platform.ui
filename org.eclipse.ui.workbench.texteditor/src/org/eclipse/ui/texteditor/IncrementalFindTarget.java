/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;

import java.text.MessageFormat;
import java.util.Stack;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * An incremental find target. Replace is always disabled.
 * @since 2.0
 */
class IncrementalFindTarget implements IFindReplaceTarget, IFindReplaceTargetExtension, VerifyKeyListener, MouseListener, FocusListener, ISelectionChangedListener, ITextListener {

	/** The string representing rendered tab */
	private final static String TAB= EditorMessages.getString("Editor.FindIncremental.render.tab"); //$NON-NLS-1$
	private final static String REVERSE= EditorMessages.getString("Editor.FindIncremental.reverse"); //$NON-NLS-1$
	private final static String WRAPPED= EditorMessages.getString("Editor.FindIncremental.wrapped"); //$NON-NLS-1$

	/** The text viewer to operate on */
	private final ITextViewer fTextViewer;
	/** The status line manager for output */
	private final IStatusLineManager fStatusLine;
	/** The find replace target to delegate find requests */
	private final IFindReplaceTarget fTarget;

	/** The current find string */
	private StringBuffer fFindString= new StringBuffer();
	/** The position of the first upper case character, -1 if none */
	private int fCasePosition;
	/** The position in the stack of the first wrap search, -1 if none */
	private int fWrapPosition;
	/** The position of the last successful find */
	private int fCurrentIndex;	
	/** A flag indicating if last find was successful */
	private boolean fFound;	
	/** A flag indicating if the last search was forward */
	private boolean fForward= true;
	/** A flag indicating listeners are installed. */
	private boolean fInstalled;
	/**
	 * A flag indicating that a search is currently active.
	 * Used to ignore selection callbacks generated by the incremental search itself.
	 */
	private boolean fSearching;
	/** The current find stack */
	private Stack fSessionStack;
	/** The previous search string */
	private String fPrevFindString= ""; //$NON-NLS-1$

	private class SearchResult {
		int selection, length, index, findLength;
		boolean found, forward;

		public SearchResult() {
			Point p= fTarget.getSelection();
			selection= p.x;
			length= p.y;
			index= fCurrentIndex;
			findLength= fFindString.length();
			found= fFound;
			forward= fForward;
		}

	}

	private void saveState() {
		fSessionStack.push(new SearchResult());
	}

	private void restoreState() {

		StyledText text= fTextViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		SearchResult searchResult= null;
		if (!fSessionStack.empty())
			searchResult= (SearchResult) fSessionStack.pop();

		if (searchResult == null) {
			text.getDisplay().beep();
			return;
		}

		text.setSelectionRange(searchResult.selection, searchResult.length);
		text.showSelection();

		// relies on the contents of the StringBuffer
		fFindString.setLength(searchResult.findLength);
		fCurrentIndex= searchResult.index;
		fFound= searchResult.found;
		fForward= searchResult.forward;

		// Recalculate the indices
		if (fFindString.length() < fCasePosition)
			fCasePosition= -1;
		if (fSessionStack.size() < fWrapPosition)
			fWrapPosition= -1;
	}

	/**
	 * Sets the direction for the next search.
	 * This can be called before <code>beginSession</code> to set the initial search direction.
	 * @param forward <code>true</code> if the next search should be forward
	 * @see beginSession
	 */
	public void setDirection(boolean forward) {
		fForward= forward;
	}

	/**
	 * Creates an instance of an incremental find target.
	 * @param viewer the text viewer to operate on
	 * @param manager the status line manager for output
	 */
	public IncrementalFindTarget(ITextViewer viewer, IStatusLineManager manager) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(manager);
		fTextViewer= viewer;
		fStatusLine= manager;
		fTarget= viewer.getFindReplaceTarget();
	}

	/*
	 * @see IFindReplaceTarget#canPerformFind()
	 */
	public boolean canPerformFind() {
		return fTarget.canPerformFind();
	}

	/*
	 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
	 */
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		return fTarget.findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
	}

	/*
	 * @see IFindReplaceTarget#getSelection()
	 */
	public Point getSelection() {
		return fTarget.getSelection();
	}

	/*
	 * @see IFindReplaceTarget#getSelectionText()
	 */
	public String getSelectionText() {
		return fTarget.getSelectionText();
	}

	/*
	 * @see IFindReplaceTarget#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}

	/*
	 * @see IFindReplaceTarget#replaceSelection(String)
	 */
	public void replaceSelection(String text) {
	}

	/*
	 * @see IFindReplaceTargetExtension#beginSession()
	 */
	public void beginSession() {
		fSearching= true;

		// Workaround since some accelerators get handled directly by the OS
		if (fInstalled) {
			saveState();
			repeatSearch(fForward);
			updateStatus();
			fSearching= false;
			return;
		}

		fFindString.setLength(0);
		fSessionStack= new Stack();
		fCasePosition= -1;		
		fWrapPosition= -1;
		fFound= true;

		// clear initial selection
		StyledText text= fTextViewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			fCurrentIndex= text.getCaretOffset();
			text.setSelection(fCurrentIndex);
		} else {
			fCurrentIndex= 0;
		}
		
		install();

		// Set the mark
		if (fTextViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) fTextViewer).setMark(fCurrentIndex);

		updateStatus();
		
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).beginSession();
			
		fSearching= false;
	}

	/*
	 * @see IFindReplaceTargetExtension#endSession()
	 */
	public void endSession() {
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// will uninstall itself
	}

	/*
	 * @see IFindReplaceTargetExtension#getScope()
	 */
	public IRegion getScope() {
		return null;
	}

	/*
	 * @see IFindReplaceTargetExtension#setGlobal(boolean)
	 */
	public void setGlobal(boolean global) {
	}

	/*
	 * @see IFindReplaceTargetExtension#setScope(IRegion)
	 */
	public void setScope(IRegion scope) {
	}
	
	/*
	 * @see IFindReplaceTargetExtension#setReplaceAllMode(boolean)
	 */
	public void setReplaceAllMode(boolean replaceAll) {
	}
	
	/**
	 * Installs this target. I.e. adds all required listeners.
	 */
	private void install() {

		if (fInstalled)
			return;

		StyledText text= fTextViewer.getTextWidget();
		if (text == null)
			return;
		
		text.addMouseListener(this);
		text.addFocusListener(this);
		fTextViewer.addTextListener(this);

		ISelectionProvider selectionProvider= fTextViewer.getSelectionProvider();
		if (selectionProvider != null)
			selectionProvider.addSelectionChangedListener(this);
					
		if (fTextViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) fTextViewer).prependVerifyKeyListener(this);
		else
			text.addVerifyKeyListener(this);
		
		fInstalled= true;
	}
	
	/**
	 * Uninstalls itself. I.e. removes all listeners installed in <code>install</code>.
	 */
	private void uninstall() {

		fTextViewer.removeTextListener(this);

		ISelectionProvider selectionProvider= fTextViewer.getSelectionProvider();
		if (selectionProvider != null)
			selectionProvider.removeSelectionChangedListener(this);

		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			text.removeMouseListener(this);
			text.removeFocusListener(this);
		}
				
		if (fTextViewer instanceof ITextViewerExtension) {
			((ITextViewerExtension) fTextViewer).removeVerifyKeyListener(this);

		} else {
			if (text != null)
				text.removeVerifyKeyListener(this);
		}
		
		fInstalled= false;
	}

	/**
	 * Updates the status line.
	 */
	private void updateStatus() {

		if (!fInstalled)
			return;

		String string= fFindString.toString();
		String wrapPrefix= fWrapPosition == -1 ? "" : WRAPPED; //$NON-NLS-1$
		String reversePrefix= fForward ? "" : REVERSE; //$NON-NLS-1$

		if (!fFound) {
			String pattern= EditorMessages.getString("Editor.FindIncremental.not_found.pattern"); //$NON-NLS-1$
			statusError(MessageFormat.format(pattern, new Object[] { reversePrefix, wrapPrefix, string }));

		} else {
			String pattern= EditorMessages.getString("Editor.FindIncremental.found.pattern"); //$NON-NLS-1$
			statusMessage(MessageFormat.format(pattern, new Object[] { reversePrefix, wrapPrefix, string }));
		}
	}

	/*
	 * @see VerifyKeyListener#verifyKey(VerifyEvent)
	 */
	public void verifyKey(VerifyEvent event) {

		if (!event.doit)
			return;

		fSearching= true;
		if (event.character == 0) {
	
			switch (event.keyCode) {
			
			// ALT, CTRL, ARROW_LEFT, ARROW_RIGHT == leave	
			case SWT.ARROW_LEFT:
			case SWT.ARROW_RIGHT:
			case SWT.HOME:
			case SWT.END:
			case SWT.PAGE_DOWN:
			case SWT.PAGE_UP:
				leave();
				break;

			case SWT.ARROW_DOWN:
				saveState();
				setDirection(true);
				repeatSearch(fForward);
				event.doit= false;				
				break;

			case SWT.ARROW_UP:
				saveState();
				setDirection(false);
				repeatSearch(fForward);
				event.doit= false;				
				break;			
			}
	
		// event.character != 0
		} else {
			
			switch (event.character) {
			
			// ESC, CR = quit
			case 0x1B:
			case 0x0D:
				leave();
				event.doit= false;
				break;
				
			// backspace	and delete
			case 0x08:
			case 0x7F:
				restoreState();
				event.doit= false;
				break;		
			
			default:
				if (event.stateMask == 0 || event.stateMask == SWT.SHIFT) {
					saveState();
					addCharSearch(event.character);
					event.doit= false;
				}
				break;
			}		
		}
		updateStatus();
		fSearching= false;
	}

	/**
	 * Repeats the last search while possibly changing the direciton.
	 * @param forward <code>true</code> iff the next search should be forward
	 */
	private boolean repeatSearch(boolean forward) {
		if (fFindString.length() == 0)
			fFindString= new StringBuffer(fPrevFindString);

		String string= fFindString.toString();
		if (string.length() == 0) {
			fFound= true;
			return true;
		}

		StyledText text= fTextViewer.getTextWidget();
		// Cannot use fTarget.getSelection since that does not return which side of the
		// selection the caret is on.
		int startIndex= text.getCaretOffset();
		if (!forward)
			startIndex -= 1;

		// Check to see if a wrap is necessary
		if (!fFound && (fForward == forward)) {
			startIndex= -1;
			if (fWrapPosition == -1)
				fWrapPosition= fSessionStack.size();
		}
		fForward = forward;

		// Find the string
		text.setRedraw(false);
		int index= fTarget.findAndSelect(startIndex, string, fForward, fCasePosition != -1, false);

		// Set the caret on the left if the search is reversed
		if (!forward) {
			Point p= fTarget.getSelection();
			text.setSelectionRange(p.x + p.y, -p.y);
			p= null;
		}
		text.setRedraw(true);

		// Take appropriate action
		boolean found = (index != -1);
		if (!found && fFound) {
			text= fTextViewer.getTextWidget();
			if (text != null && !text.isDisposed())
				text.getDisplay().beep();
		}

		if (found)
			fCurrentIndex= startIndex;

		fFound= found;
		return found;
	}

	/**
	 * Adds the given character to the search string and repeats the search with the last parameters.
	 * @param c the character to append to the search pattern
	 */
	private boolean addCharSearch(char c) {
		// Add char to pattern
		if (fCasePosition == -1 && Character.isUpperCase(c) && Character.toLowerCase(c) != c)
			fCasePosition= fFindString.length();

		fFindString.append(c);
		String string= fFindString.toString();
		StyledText text= fTextViewer.getTextWidget();

		text.setRedraw(false);
		int index= fTarget.findAndSelect(fCurrentIndex, string, fForward, fCasePosition != -1, false);

		// Set the caret on the left if the search is reversed
		if (!fForward) {
			Point p= fTarget.getSelection();
			text.setSelectionRange(p.x + p.y, -p.y);
		}
		text.setRedraw(true);

		// Take appropriate action
		boolean found = (index != -1);
		if (!found && fFound) {
			text= fTextViewer.getTextWidget();
			if (text != null && !text.isDisposed())
				text.getDisplay().beep();
		}

		fFound= found;
		return found;
	}

	/**
	 * Leaves this incremental search session.
	 */
	private void leave() {
		if (fFindString.length() != 0) 
			fPrevFindString= fFindString.toString();
		statusClear();
		uninstall();				
		fSessionStack = null;
	}

	/*
	 * @see ITextListener#textChanged(TextEvent)
	 */
	public void textChanged(TextEvent event) {
		leave();
	}

	/*
	 * @see MouseListener#mouseDoubleClick(MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
		leave();		
	}

	/*
	 * @see MouseListener#mouseDown(MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		leave();
	}

	/*
	 * @see MouseListener#mouseUp(MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		leave();
	}

	/*
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		leave();
	}

	/*
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		leave();
	}

	/**
	 * Sets the given string as status message, clears the status error message.
	 * @param string the status message
	 */
	private void statusMessage(String string) {
		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(escapeTabs(string));
	}

	/**
	 * Sets the status error message, clears the status message.
	 * @param string the status error message
	 */
	private void statusError(String string) {
		fStatusLine.setErrorMessage(escapeTabs(string));
		fStatusLine.setMessage(""); //$NON-NLS-1$
	}

	/**
	 * Clears the status message and the status error message.
	 */
	private void statusClear() {
		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(""); //$NON-NLS-1$
	}
	
	/**
	 * Translates all tab characters into a proper status line presentation.
	 * @param string the string in which to translate the tabs
	 * @return the given string with all tab characters replace with a proper status line presentation
	 */
	private String escapeTabs(String string) {
		StringBuffer buffer= new StringBuffer();

		int begin= 0;
		int end= string.indexOf('\t', begin);
		
		while (end >= 0) {
			buffer.append(string.substring(begin, end));
			buffer.append(TAB);
			begin= end + 1;
			end= string.indexOf('\t', begin);
		}
		buffer.append(string.substring(begin));
		
		return buffer.toString();
	}
	
	/*
	 * @see IFindReplaceTargetExtension#getLineSelection()
	 */
	public Point getLineSelection() {
		if (fTarget instanceof IFindReplaceTargetExtension)
			return ((IFindReplaceTargetExtension) fTarget).getLineSelection();
		
		return null; // XXX should not return null
	}

	/*
	 * @see IFindReplaceTargetExtension#setSelection(int, int)
	 */
	public void setSelection(int offset, int length) {
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).setSelection(offset, length);
	}

	/*
	 * @see IFindReplaceTargetExtension#setScopeHighlightColor(Color)
	 */
	public void setScopeHighlightColor(Color color) {
	}

	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		if (!fSearching)
			leave();
	}
}
