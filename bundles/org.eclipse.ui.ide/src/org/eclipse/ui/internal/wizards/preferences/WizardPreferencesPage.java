/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.preferences.PreferenceTransferElement;
import org.eclipse.ui.preferences.PreferenceTransferManager;

/**
 * Base class for preference export/import pages.
 * 
 * 
 * @since 3.1
 * 
 */
public abstract class WizardPreferencesPage extends WizardDataTransferPage {

    // widgets
    protected Combo destinationNameField;

    // constants
    private Button destinationBrowseButton;

    private Button overwriteExistingFilesCheckbox;

    private Table transfersTable;

    private Composite buttonComposite;

    private Button allButton;
	
	private Button chooseImportsButton;

	private Group group;

    // dialog store id constants
    private static final String STORE_DESTINATION_NAMES_ID = "WizardPreferencesExportPage1.STORE_DESTINATION_NAMES_ID";//$NON-NLS-1$

    private static final String STORE_OVERWRITE_EXISTING_FILES_ID = "WizardPreferencesExportPage1.STORE_OVERWRITE_EXISTING_FILES_ID";//$NON-NLS-1$

    private static final String TRANSFER_ALL_PREFERENCES_ID = "WizardPreferencesExportPage1.EXPORT_ALL_PREFERENCES_ID"; //$NON-NLS-1$

    private Hashtable imageTable;

    private PreferenceTransferElement[] transfers;

	private static final String STORE_DESTINATION_ID = null;
	
	/**
     * @param pageName
     */
    protected WizardPreferencesPage(String pageName) {
        super(pageName);
    }

    /**
     * Creates a new button with the given id.
     * <p>
     * The <code>Dialog</code> implementation of this framework method creates
     * a standard push button, registers for selection events including button
     * presses and registers default buttons with its shell. The button id is
     * stored as the buttons client data. Note that the parent's layout is
     * assumed to be a GridLayout and the number of columns in this layout is
     * incremented. Subclasses may override.
     * </p>
     * 
     * @param parent
     *            the parent composite
     * @param id
     *            the id of the button (see <code>IDialogConstants.*_ID</code>
     *            constants for standard dialog button ids)
     * @param label
     *            the label from the button
     * @param defaultButton
     *            <code>true</code> if the button is to be the default button,
     *            and <code>false</code> otherwise
     */
    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        // increment the number of columns in the button bar
        ((GridLayout) parent.getLayout()).numColumns++;

        Button button = new Button(parent, SWT.PUSH);
        button.setFont(parent.getFont());

        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(buttonData);

        button.setData(new Integer(id));
        button.setText(label);

        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
            button.setFocus();
        }
        return button;
    }

    /**
     * Add the passed value to self's destination widget's history
     * 
     * @param value
     *            java.lang.String
     */
    protected void addDestinationItem(String value) {
        destinationNameField.add(value);
    }

    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createTransferArea(composite);

        restoreWidgetValues();
        // updateWidgetEnablements();
        setPageComplete();
        setPreferenceTransfers();
        setControl(composite);

        giveFocusToDestination();
    }

    /**
     * @param composite
     */
    protected abstract void createTransferArea(Composite composite);

    protected final boolean validateDestinationGroup() {
        File file = new File(getDestinationValue());
        if (file.getPath().length() <= 0 || file.isDirectory()) {
            setMessage(PreferencesMessages.WizardPreferencesExportPage1_noPrefFile);
            return false;
        }

        return true;
    }

    /**
     * 
     */
    protected void setPreferenceTransfers() {
        PreferenceTransferElement[] transfers = getTransfers();
        transfersTable.removeAll();
        for (int i = 0; i < transfers.length; i++) {
            PreferenceTransferElement element = transfers[i];
            createItem(element, transfersTable);
        }
    }

    /*
     * return the PreferenceTransgerElements specified
     */
    protected PreferenceTransferElement[] getTransfers() {
        if (transfers == null)
            transfers = PreferenceTransferManager.getPreferenceTransfers();
        return transfers;
    }

    /**
     * @param element
     * @param table
     */
    private void createItem(PreferenceTransferElement element, Table table) {
        TableItem item = new TableItem(table, SWT.CHECK);
        item.setText(element.getName());
        item.setData(element);
        ImageDescriptor descriptor = element.getImageDescriptor();
        Image image = null;
        if (descriptor != null) {
            Hashtable images = getImageTable();
            image = (Image) images.get(descriptor);
            if (image == null) {
                image = descriptor.createImage();
                images.put(descriptor, image);
                item.setImage(image);
            }
        }

    }

    /**
     * @return <code>Hashtable</code> the table of images
     */
    private Hashtable getImageTable() {
        if (imageTable == null) {
            imageTable = new Hashtable(10);
        }
        return imageTable;
    }

    /**
     * @param composite
     */
    protected void createTransfersList(Composite composite) {

        allButton = new Button(composite, SWT.RADIO);
        allButton.setText(getAllButtonText());

        chooseImportsButton = new Button(composite, SWT.RADIO);
		chooseImportsButton.setText(getChooseButtonText());

        group = new Group(composite, SWT.NONE);
        group.setText(PreferencesMessages.WizardPreferencesExportPage1_preferences);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        group.setLayoutData(data);

        GridLayout layout = new GridLayout();
        group.setLayout(layout);

        transfersTable = new Table(group, SWT.CHECK | SWT.BORDER);
        transfersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Label text = new Label(group, SWT.WRAP);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        transfersTable.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                updateDescription();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                updateDescription();
            }

            private void updateDescription() {
                if (transfersTable.getSelectionCount() > 0) {
                    TableItem item = transfersTable.getSelection()[0];
                    text.setText(((PreferenceTransferElement) item.getData())
                            .getDescription());
                } else
                    text.setText(""); //$NON-NLS-1$
            }
        });

        allButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                updateTransferAll(allButton.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                updateTransferAll(allButton.getSelection());
            }
        });

        addSelectionButtons(group);

    }

    protected abstract String getChooseButtonText();

    protected abstract String getAllButtonText();

    /**
     * Add the selection and deselection buttons to the composite.
     * 
     * @param composite
     *            org.eclipse.swt.widgets.Composite
     */
    private void addSelectionButtons(Composite composite) {

        buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        buttonComposite.setLayout(layout);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        buttonComposite.setLayoutData(data);

        Button selectButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, IDEWorkbenchMessages.SelectionDialog_selectLabel, false);

        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(true);
            }
        };
        selectButton.addSelectionListener(listener);

        Button deselectButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, IDEWorkbenchMessages.SelectionDialog_deselectLabel, false);

        listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(false);

            }
        };
        deselectButton.addSelectionListener(listener);

    }

    /**
     * @param bool
     */
    protected void setAllChecked(boolean bool) {
        TableItem[] items = transfersTable.getItems();
        for (int i = 0; i < items.length; i++) {
            TableItem item = items[i];
            item.setChecked(bool);
        }
    }

    /**
     * Create the export destination specification widgets
     * 
     * @param parent
     *            org.eclipse.swt.widgets.Composite
     */
    protected void createDestinationGroup(Composite parent) {
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        new Label(destinationSelectionGroup, SWT.NONE)
                .setText(getDestinationLabel());

        // destination name entry field
        destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE
                | SWT.BORDER);
        destinationNameField.addListener(SWT.Modify, this);
        destinationNameField.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        destinationNameField.setLayoutData(data);

        // destination browse button
        destinationBrowseButton = new Button(destinationSelectionGroup,
                SWT.PUSH);
        destinationBrowseButton.setText(PreferencesMessages.PreferencesExport_browse);
        destinationBrowseButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL));
        destinationBrowseButton.addListener(SWT.Selection, this);

        new Label(parent, SWT.NONE); // vertical spacer
    }

    /**
     * Create the export options specification widgets.
     * 
     * @param parent
     *            org.eclipse.swt.widgets.Composite
     */
    protected void createOptionsGroup(Composite parent) {
        // options group
        Composite optionsGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));

        // overwrite... checkbox
        overwriteExistingFilesCheckbox = new Button(optionsGroup, SWT.CHECK
                | SWT.LEFT);
        overwriteExistingFilesCheckbox.setText(PreferencesMessages.ExportFile_overwriteExisting);
    }

    /**
     * Attempts to ensure that the specified directory exists on the local file
     * system. Answers a boolean indicating success.
     * 
     * @return boolean
     * @param directory
     *            java.io.File
     */
    protected boolean ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            if (!queryYesNoQuestion(PreferencesMessages.PreferencesExport_createTargetDirectory))
                return false;

            if (!directory.mkdirs()) {
                MessageDialog
                        .openError(
                                getContainer().getShell(),
                                PreferencesMessages.PreferencesExport_error,
                                PreferencesMessages.PreferencesExport_directoryCreationError);
                return false;
            }
        }
        return true;
    }

    /**
     * If the target for export does not exist then attempt to create it. Answer
     * a boolean indicating whether the target exists (ie.- if it either
     * pre-existed or this method was able to create it)
     * 
     * @return boolean
     */
    protected boolean ensureTargetIsValid(File file) {
        if (file.exists()) {
            if (!getOverwriteExisting()) {
                String msg = NLS.bind(PreferencesMessages.WizardPreferencesExportPage1_overwrite, file.getAbsolutePath());
                if (!queryYesNoQuestion(msg))
                    return false;
            }
            file.delete();
        } else if (!file.isDirectory()) {
            File parent = file.getParentFile();
            if (parent != null)
                file.getParentFile().mkdirs();
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.WizardDataTransferPage#saveWidgetValues()
     */
    protected void saveWidgetValues() {
        // allow subclasses to save values
        internalSaveWidgetValues();
    }

    /**
     * The Finish button was pressed. Try to do the required work now and answer
     * a boolean indicating success. If false is returned then the wizard will
     * not close.
     * 
     * @return boolean
     */
    public boolean finish() {
        // about to invoke the operation so save our state
        saveWidgetValues();

        IPreferenceFilter[] transfers = null;

        if (getTransferAll()) {
            // export all
            transfers = new IPreferenceFilter[1];
            
            // For export all create a preference filter that can export 
            // all nodes of the Instance and Configuration scopes
            transfers[0] = new IPreferenceFilter() {

                public String[] getScopes() {
                    return new String[] { InstanceScope.SCOPE,
                            ConfigurationScope.SCOPE };
                }

                public Map getMapping(String scope) {
                    return null;
                }
            };
        } else {
            transfers = getFilters();
        }

		boolean success = transfer(transfers);
		// if it was a successful tranfer then store the name of the file to use it on the next export
		if (success) {
			saveWidgetValues();
    }
		return success;
	}

    /**
     * @return the preference transfer filters
     */
    protected IPreferenceFilter[] getFilters() {
        IPreferenceFilter[] filters = null;
        PreferenceTransferElement[] transferElements;
        transferElements = getPreferenceTransferElements();
        if (transferElements != null) {
            filters = new IPreferenceFilter[transferElements.length];
            for (int j = 0; j < transferElements.length; j++) {
                PreferenceTransferElement element = transferElements[j];
                try {
                    filters[j] = element.getFilter();
                } catch (CoreException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                }
            }
        } else
            filters = new IPreferenceFilter[0];
        
        return filters;
    }

    /**
     * @return the list of transfer elements
     */
    protected PreferenceTransferElement[] getPreferenceTransferElements() {
        PreferenceTransferElement[] transferElements;
        // export selected transfer types
        TableItem[] items = transfersTable.getItems();
        List transferList = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            TableItem item = items[i];
            if (item.getChecked()) {
                transferList.add(item.getData());
            }
        }
		transferElements = new PreferenceTransferElement[transferList.size()];
        int i = 0;
        for (Iterator iter = transferList.iterator(); iter.hasNext();) {
            transferElements[i] = (PreferenceTransferElement) iter.next();
            i++;
        }
        return transferElements;
    }

    /**
     * @param transfers
     * @return boolean
     */
    protected abstract boolean transfer(IPreferenceFilter[] transfers);
    
    /**
     * Check whether the internal state of the page is complete and update the
     * dialog
     */
    public void setPageComplete() {
        boolean complete = true;

        if (!determinePageCompletion())
            complete = false;

        super.setPageComplete(complete);
    }

    /**
     * Answer the string to display in self as the destination type
     * 
     * @return java.lang.String
     */
    protected abstract String getDestinationLabel();

    /**
     * Answer the contents of self's destination specification widget
     * 
     * @return java.lang.String
     */
    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }

    /**
     * Set the current input focus to self's destination entry field
     */
    protected void giveFocusToDestination() {
        destinationNameField.setFocus();
    }

    /**
     * Open an appropriate destination browser so that the user can specify a
     * source to import from
     */
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), getFileDialogStyle());
        dialog.setText(getFileDialogTitle());
        dialog.setFilterPath(getDestinationValue());
        dialog.setFilterExtensions(new String[] { "*.epf" }); //$NON-NLS-1$
        String selectedFileName = dialog.open();

        if (selectedFileName != null)
            setDestinationValue(selectedFileName);
    }

    protected abstract String getFileDialogTitle();
	protected abstract int getFileDialogStyle();
	

	/**
     * Handle all events and enablements for widgets in this page
     * 
     * @param e
     *            Event
     */
    public void handleEvent(Event e) {
        Widget source = e.widget;

        if (source == destinationBrowseButton)
            handleDestinationBrowseButtonPressed();

        updatePageCompletion();
    }

    /**
     * Hook method for saving widget values for restoration by the next instance
     * of this class.
     */
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null)
                directoryNames = new String[0];

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
			String current = getDestinationValue();
			if (current != null && !current.equals("")) { //$NON-NLS-1$
				settings.put(STORE_DESTINATION_ID, current);				
			}
            // options
            if (overwriteExistingFilesCheckbox != null) {
                settings.put(STORE_OVERWRITE_EXISTING_FILES_ID,
                    overwriteExistingFilesCheckbox.getSelection());
            }
			settings.put(TRANSFER_ALL_PREFERENCES_ID, allButton.getSelection());

        }
    }

    /**
     * Hook method for restoring widget values to the values that they held last
     * time this wizard was used to completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
		boolean all = true;
        if (settings != null) {
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames != null) {
            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++)
                addDestinationItem(directoryNames[i]);

				String current = settings.get(STORE_DESTINATION_ID);
				if (current != null)
					setDestinationValue(current);
            // options
            if (overwriteExistingFilesCheckbox != null) {
                overwriteExistingFilesCheckbox.setSelection(settings
                    .getBoolean(STORE_OVERWRITE_EXISTING_FILES_ID));
            }
				all = settings.getBoolean(TRANSFER_ALL_PREFERENCES_ID);
			}
		}
			if (all)
				allButton.setSelection(true);
			else
				chooseImportsButton.setSelection(true);
            
			updateTransferAll(all);
        }

    private boolean getOverwriteExisting() {
        return overwriteExistingFilesCheckbox.getSelection();
    }

    private boolean getTransferAll() {
        return allButton.getSelection();
    }

    private void updateTransferAll(boolean transferAll) {
        transfersTable.setEnabled(!transferAll);
        buttonComposite.setEnabled(!transferAll);
        group.setEnabled(!transferAll);
    }
	
    /**
     * Set the contents of self's destination specification widget to the passed
     * value
     * 
     * @param value
     *            java.lang.String
     */
    protected void setDestinationValue(String value) {
        destinationNameField.setText(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    public void dispose() {
        super.dispose();
        if (imageTable == null)
            return;

        for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
            ((Image) i.next()).dispose();
        }
        imageTable = null;
        transfers = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.WizardDataTransferPage#allowNewContainerName()
     */
    protected boolean allowNewContainerName() {
        return true;
    }
}
