/*
 * jSite - a tool for uploading websites into Freenet
 * Copyright (C) 2006 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package de.todesbaum.jsite.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.mime.DefaultMIMETypes;
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;
import javax.swing.ListModel;

/**
 * Wizard page that lets the user manage the files of a project.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProjectFilesPage extends TWizardPage implements ActionListener, ListSelectionListener, DocumentListener, FileScannerListener, ChangeListener {

    /** The project. */
    private Project project;
    /** The “scan files” action. */
    private Action scanAction;
    /** The “edit container” action. */
    private Action editContainerAction;
    /** The “add container” action. */
    private Action addContainerAction;
    /** The “delete container” action. */
    protected Action deleteContainerAction;
    /** The “ignore hidden files” checkbox. */
    private JCheckBox ignoreHiddenFilesCheckBox;
    /** The list of project files. */
    private JList projectFileList;
    /** The “default file” checkbox. */
    public JCheckBox defaultFileCheckBox;
    /** The “insert” checkbox. */
    private JCheckBox fileOptionsInsertCheckBox;
    /** The “insert redirect” checkbox. */
    private JCheckBox fileOptionsInsertRedirectCheckBox;
    /** The “custom key” textfield. */
    private JTextField fileOptionsCustomKeyTextField;
    /** The “rename” check box. */
    private JCheckBox fileOptionsRenameCheckBox;
    /** The “new name” text field. */
    private JTextField fileOptionsRenameTextField;
    /** The “mime type” combo box. */
    private JComboBox fileOptionsMIMETypeComboBox;
    /** The “mime type” combo box model. */
    private DefaultComboBoxModel containerComboBoxModel;
    /** The “container” combo box. */
    private JComboBox fileOptionsContainerComboBox;
    /** The “edition replacement range” spinner. */
    private JSpinner replaceEditionRangeSpinner;
    /** The “replacement” check box. */
    private JCheckBox replacementCheckBox;

    /**
     * Creates a new project file page.
     *
     * @param wizard
     *            The wizard the page belongs to
     */
    public ProjectFilesPage(final TWizard wizard) {
        super(wizard);
        pageInit();
    }

    /**
     * Initializes the page and all its actions and components.
     */
    private void pageInit() {
        createActions();
        setLayout(new BorderLayout(12, 12));
        add(createProjectFilesPanel(), BorderLayout.CENTER);



    }

    /**
     * Creates all actions.
     */
    private void createActions() {
        scanAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.rescan")) {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent actionEvent) {
                actionScan();
            }
        };
        scanAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
        scanAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.rescan.tooltip"));

        addContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.add-container")) {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent actionEvent) {
                actionAddContainer();
            }
        };
        addContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.add-container.tooltip"));
        addContainerAction.setEnabled(false);

        editContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.edit-container")) {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent actionEvent) {
                actionEditContainer();
            }
        };
        editContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.edit-container.tooltip"));
        editContainerAction.setEnabled(false);

        deleteContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.delete-container")) {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent actionEvent) {
                actionDeleteContainer();
            }
        };
        deleteContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.delete-container.tooltip"));
        deleteContainerAction.setEnabled(false);

        I18nContainer.getInstance().registerRunnable(new Runnable() {

            @SuppressWarnings("synthetic-access")
            public void run() {
                scanAction.putValue(Action.NAME, I18n.getMessage("jsite.project-files.action.rescan"));
                scanAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.rescan.tooltip"));
                addContainerAction.putValue(Action.NAME, I18n.getMessage("jsite.project-files.action.add-container"));
                addContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.add-container.tooltip"));
                editContainerAction.putValue(Action.NAME, I18n.getMessage("jsite.project-files.action.edit-container"));
                editContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.edit-container.tooltip"));
                deleteContainerAction.putValue(Action.NAME, I18n.getMessage("jsite.project-files.action.delete-container"));
                deleteContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.delete-container.tooltip"));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pageAdded(final TWizard wizard) {
        actionScan();
        this.wizard.setPreviousName(I18n.getMessage("jsite.wizard.previous"));
        this.wizard.setNextName(I18n.getMessage("jsite.project-files.insert-now"));
        this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));

        System.out.println("pageAdded__ pFilesPage");

        //   Thread t=new Thread

    }

    /**
     * Creates the panel contains the project file list and options.
     *
     * @return The created panel
     */
    private JComponent createProjectFilesPanel() {



        JPanel projectFilesPanel = new JPanel(new BorderLayout(12, 12));

        projectFileList = new JList();
        projectFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectFileList.setMinimumSize(new Dimension(250, projectFileList.getPreferredSize().height));
        projectFileList.addListSelectionListener(this);




        projectFilesPanel.add(new JScrollPane(projectFileList), BorderLayout.CENTER);

        JPanel fileOptionsAlignmentPanel = new JPanel(new BorderLayout(12, 12));
        projectFilesPanel.add(fileOptionsAlignmentPanel, BorderLayout.PAGE_END);
        JPanel fileOptionsPanel = new JPanel(new GridBagLayout());
        fileOptionsAlignmentPanel.add(fileOptionsPanel, BorderLayout.PAGE_START);

        ignoreHiddenFilesCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.ignore-hidden-files"));
        ignoreHiddenFilesCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.ignore-hidden-files.tooltip"));
        ignoreHiddenFilesCheckBox.setName("ignore-hidden-files");
        ignoreHiddenFilesCheckBox.addActionListener(this);
        
        ignoreHiddenFilesCheckBox.setSelected(true);
        
        fileOptionsPanel.add(ignoreHiddenFilesCheckBox, new GridBagConstraints(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        fileOptionsPanel.add(new JButton(scanAction), new GridBagConstraints(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));

        final JLabel fileOptionsLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.project-files.file-options") + "</b></html>");
        fileOptionsPanel.add(fileOptionsLabel, new GridBagConstraints(0, 2, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));

        defaultFileCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.default"));
        defaultFileCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.default.tooltip"));
        defaultFileCheckBox.setName("default-file");
        defaultFileCheckBox.addActionListener(this);
        defaultFileCheckBox.setEnabled(true);

        fileOptionsPanel.add(defaultFileCheckBox, new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));

        fileOptionsInsertCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.insert"), true);
        fileOptionsInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert.tooltip"));
        fileOptionsInsertCheckBox.setName("insert");
        fileOptionsInsertCheckBox.setMnemonic(KeyEvent.VK_I);
        fileOptionsInsertCheckBox.addActionListener(this);
        fileOptionsInsertCheckBox.setEnabled(false);

        fileOptionsPanel.add(fileOptionsInsertCheckBox, new GridBagConstraints(0, 4, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

        fileOptionsCustomKeyTextField = new JTextField(45);
        fileOptionsCustomKeyTextField.setToolTipText(I18n.getMessage("jsite.project-files.custom-key.tooltip"));
        fileOptionsCustomKeyTextField.setEnabled(false);
        fileOptionsCustomKeyTextField.getDocument().addDocumentListener(this);

        fileOptionsInsertRedirectCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.insert-redirect"), false);
        fileOptionsInsertRedirectCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert-redirect.tooltip"));
        fileOptionsInsertRedirectCheckBox.setName("insert-redirect");
        fileOptionsInsertRedirectCheckBox.setMnemonic(KeyEvent.VK_R);
        fileOptionsInsertRedirectCheckBox.addActionListener(this);
        fileOptionsInsertRedirectCheckBox.setEnabled(false);

        final TLabel customKeyLabel = new TLabel(I18n.getMessage("jsite.project-files.custom-key") + ":", KeyEvent.VK_K, fileOptionsCustomKeyTextField);
        fileOptionsPanel.add(fileOptionsInsertRedirectCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
        fileOptionsPanel.add(customKeyLabel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
        fileOptionsPanel.add(fileOptionsCustomKeyTextField, new GridBagConstraints(2, 5, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

        fileOptionsRenameCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.rename"), false);
        fileOptionsRenameCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.rename.tooltip"));
        fileOptionsRenameCheckBox.setName("rename");
        fileOptionsRenameCheckBox.setMnemonic(KeyEvent.VK_N);
        fileOptionsRenameCheckBox.addActionListener(this);
        fileOptionsRenameCheckBox.setEnabled(false);

        fileOptionsRenameTextField = new JTextField();
        fileOptionsRenameTextField.setEnabled(false);
        fileOptionsRenameTextField.getDocument().addDocumentListener(new DocumentListener() {

            @SuppressWarnings("synthetic-access")
            private void storeText(DocumentEvent documentEvent) {
                FileOption fileOption = getSelectedFile();
                Document document = documentEvent.getDocument();
                int documentLength = document.getLength();
                try {
                    fileOption.setChangedName(document.getText(0, documentLength).trim());
                } catch (BadLocationException ble1) {
                    /* ignore, it should never happen. */
                }
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                storeText(documentEvent);
            }

            public void insertUpdate(DocumentEvent documentEvent) {
                storeText(documentEvent);
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                storeText(documentEvent);
            }
        });

        fileOptionsPanel.add(fileOptionsRenameCheckBox, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
        fileOptionsPanel.add(fileOptionsRenameTextField, new GridBagConstraints(2, 6, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

        fileOptionsMIMETypeComboBox = new JComboBox(DefaultMIMETypes.getAllMIMETypes());
        fileOptionsMIMETypeComboBox.setToolTipText(I18n.getMessage("jsite.project-files.mime-type.tooltip"));
        fileOptionsMIMETypeComboBox.setName("project-files.mime-type");
        fileOptionsMIMETypeComboBox.addActionListener(this);
        fileOptionsMIMETypeComboBox.setEditable(true);
        fileOptionsMIMETypeComboBox.setEnabled(false);

        final TLabel mimeTypeLabel = new TLabel(I18n.getMessage("jsite.project-files.mime-type") + ":", KeyEvent.VK_M, fileOptionsMIMETypeComboBox);
        fileOptionsPanel.add(mimeTypeLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
        fileOptionsPanel.add(fileOptionsMIMETypeComboBox, new GridBagConstraints(1, 7, 4, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

        containerComboBoxModel = new DefaultComboBoxModel();
        fileOptionsContainerComboBox = new JComboBox(containerComboBoxModel);
        fileOptionsContainerComboBox.setToolTipText(I18n.getMessage("jsite.project-files.container.tooltip"));
        fileOptionsContainerComboBox.setName("project-files.container");
        fileOptionsContainerComboBox.addActionListener(this);
        fileOptionsContainerComboBox.setEnabled(false);
        fileOptionsContainerComboBox.setVisible(false);

        final TLabel containerLabel = new TLabel(I18n.getMessage("jsite.project-files.container") + ":", KeyEvent.VK_C, fileOptionsContainerComboBox);
        containerLabel.setVisible(false);
        JButton addContainerButton = new JButton(addContainerAction);
        addContainerButton.setVisible(false);
        JButton editContainerButton = new JButton(editContainerAction);
        editContainerButton.setVisible(false);
        JButton deleteContainerButton = new JButton(deleteContainerAction);
        deleteContainerButton.setVisible(false);
        fileOptionsPanel.add(containerLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
        fileOptionsPanel.add(fileOptionsContainerComboBox, new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
        fileOptionsPanel.add(addContainerButton, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
        fileOptionsPanel.add(editContainerButton, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
        fileOptionsPanel.add(deleteContainerButton, new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

        JPanel fileOptionsReplacementPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        fileOptionsReplacementPanel.setBorder(new EmptyBorder(-6, -6, -6, -6));

        replacementCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.replacement"));
        replacementCheckBox.setName("project-files.replace-edition");
        replacementCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.replacement.tooltip"));
        replacementCheckBox.addActionListener(this);
        replacementCheckBox.setEnabled(false);
        replacementCheckBox.setVisible(false);
        fileOptionsReplacementPanel.add(replacementCheckBox);

        replaceEditionRangeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        replaceEditionRangeSpinner.setName("project-files.replace-edition-range");
        replaceEditionRangeSpinner.setToolTipText(I18n.getMessage("jsite.project-files.replacement.edition-range.tooltip"));
        replaceEditionRangeSpinner.addChangeListener(this);
        replaceEditionRangeSpinner.setEnabled(false);
        replaceEditionRangeSpinner.setVisible(false);
        final JLabel editionRangeLabel = new JLabel(I18n.getMessage("jsite.project-files.replacement.edition-range"));
        editionRangeLabel.setVisible(false);
        fileOptionsReplacementPanel.add(editionRangeLabel);
        fileOptionsReplacementPanel.add(replaceEditionRangeSpinner);

        fileOptionsPanel.add(fileOptionsReplacementPanel, new GridBagConstraints(0, 9, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));

        I18nContainer.getInstance().registerRunnable(new Runnable() {

            @SuppressWarnings("synthetic-access")
            public void run() {
                ignoreHiddenFilesCheckBox.setText(I18n.getMessage("jsite.project-files.ignore-hidden-files"));
                 ignoreHiddenFilesCheckBox.setSelected(true);
                ignoreHiddenFilesCheckBox.setToolTipText(I18n.getMessage("jsite.projet-files.ignore-hidden-files.tooltip"));
                fileOptionsLabel.setText("<html><b>" + I18n.getMessage("jsite.project-files.file-options") + "</b></html>");
                defaultFileCheckBox.setText(I18n.getMessage("jsite.project-files.default"));
                defaultFileCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.default.tooltip"));
                fileOptionsInsertCheckBox.setText(I18n.getMessage("jsite.project-files.insert"));
                fileOptionsInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert.tooltip"));
                fileOptionsInsertRedirectCheckBox.setText(I18n.getMessage("jsite.project-files.insert-redirect"));
                fileOptionsInsertRedirectCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert-redirect.tooltip"));
                fileOptionsCustomKeyTextField.setToolTipText(I18n.getMessage("jsite.project-files.custom-key.tooltip"));
                customKeyLabel.setText(I18n.getMessage("jsite.project-files.custom-key") + ":");
                fileOptionsRenameCheckBox.setText("jsite.project-files.rename");
                fileOptionsRenameCheckBox.setToolTipText("jsite.project-files.rename.tooltip");
                fileOptionsMIMETypeComboBox.setToolTipText(I18n.getMessage("jsite.project-files.mime-type.tooltip"));
                mimeTypeLabel.setText(I18n.getMessage("jsite.project-files.mime-type") + ":");
                fileOptionsContainerComboBox.setToolTipText(I18n.getMessage("jsite.project-files.container.tooltip"));
                containerLabel.setText(I18n.getMessage("jsite.project-files.container") + ":");
                replacementCheckBox.setText(I18n.getMessage("jsite.project-files.replacement"));
                replacementCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.replacement.tooltip"));
                replaceEditionRangeSpinner.setToolTipText(I18n.getMessage("jsite.project-files.replacement.edition-range.tooltip"));
                editionRangeLabel.setText(I18n.getMessage("jsite.project-files.replacement.edition-range"));
            }
        });

        return projectFilesPanel;
    }

    /**
     * Sets the project whose files to manage.
     *
     * @param project
     *            The project whose files to manage
     */
    public void setProject(final Project project) {
        this.project = project;
        setHeading(MessageFormat.format(I18n.getMessage("jsite.project-files.heading"), project.getName()));
        setDescription(I18n.getMessage("jsite.project-files.description"));
        ignoreHiddenFilesCheckBox.setSelected(true);
        I18nContainer.getInstance().registerRunnable(new Runnable() {

            public void run() {
                setHeading(MessageFormat.format(I18n.getMessage("jsite.project-files.heading"), project.getName()));
                setDescription(I18n.getMessage("jsite.project-files.description"));
            }
        });
    }

    /**
     * Returns a list of all project files.
     *
     * @return All project files
     */
    public String getBestMatchingFile(String[] names) {

        int indexIndex = -1;
        int isHtml = -1;
        String mainStr = "";


        for (int i = 0; i < names.length; i++) {
            if (names[i].lastIndexOf('.') == -1) {
                continue;
            }
            if (names[i].lastIndexOf('.') == 0) {
                continue;
            }



            String nameB = names[i].substring(0, names[i].lastIndexOf('.'));
            String nameN = names[i].substring(names[i].lastIndexOf('.') + 1);
            System.out.println(nameB + "__" + nameN);

            if (nameB.equals("index")) {
                indexIndex = i;
            }

            if (nameN.equals("html") || nameN.equals("hml")) {
                isHtml = i;
            }

            if (indexIndex == isHtml
                    && indexIndex != -1) {
                mainStr = names[indexIndex];

            }

        }
        System.out.println(indexIndex + "__" + isHtml);


        if (mainStr.equals("")) {
            {
                if (isHtml != -1) {
                    mainStr = names[isHtml];
                }
                if (indexIndex != -1) {
                    mainStr = names[indexIndex];
                }
            }
        }
        System.out.println(mainStr);




        return mainStr;
    }

    private List<String> getProjectFiles() {
        List<String> files = new ArrayList<String>();

        String[] fileNames = new String[projectFileList.getModel().getSize()];

        for (int index = 0, size = projectFileList.getModel().getSize(); index < size; index++) {
            files.add((String) projectFileList.getModel().getElementAt(index));
            System.out.println("->" + projectFileList.getModel().getElementAt(index).toString());

            fileNames[index] = projectFileList.getModel().getElementAt(index).toString();



        }
         String bestName = getBestMatchingFile(fileNames);
        System.out.println("--*--"+bestName);
int      selIndex=0;
        for (int index = 0, size = projectFileList.getModel().getSize(); index < size; index++) {
            files.add((String) projectFileList.getModel().getElementAt(index));
            
            String str= projectFileList.getModel().getElementAt(index).toString();
            if (str.equals(bestName)){
            selIndex=index;
            }
            
            System.out.println("->" + projectFileList.getModel().getElementAt(index).toString());

            fileNames[index] = projectFileList.getModel().getElementAt(index).toString();



        }  
        
       
   System.err.println("--*--"+selIndex);

        
        projectFileList.setSelectedIndex(selIndex);
        String filename = (String) projectFileList.getSelectedValue();

        project.setIndexFile(filename);

        return files;
    }

    /**
     * Updates the container combo box model.
     */
    private void rebuildContainerComboBox() {
        /* scan files for containers */
        List<String> files = getProjectFiles();
        List<String> containers = new ArrayList<String>(); // ComboBoxModel
        // sucks. No
        // contains()!
        containers.add("");
        for (String filename : files) {
            String container = project.getFileOption(filename).getContainer();
            if (!containers.contains(container)) {
                containers.add(container);
            }
        }
        Collections.sort(containers);
        containerComboBoxModel.removeAllElements();
        for (String container : containers) {
            containerComboBoxModel.addElement(container);
        }
    }

    //
    // ACTIONS
    //
    /**
     * Rescans the project’s files.
     */
    private void actionScan() {
        projectFileList.clearSelection();
        projectFileList.setListData(new Object[0]);

        wizard.setNextEnabled(false);
        wizard.setPreviousEnabled(false);
        wizard.setQuitEnabled(false);

        FileScanner fileScanner = new FileScanner(project);
        fileScanner.addFileScannerListener(this);
        new Thread(fileScanner).start();
    }

    /**
     * Adds a container.
     */
    private void actionAddContainer() {
        String containerName = JOptionPane.showInputDialog(wizard, I18n.getMessage("jsite.project-files.action.add-container.message") + ":", null, JOptionPane.INFORMATION_MESSAGE);
        if (containerName == null) {
            return;
        }
        containerName = containerName.trim();
        String filename = (String) projectFileList.getSelectedValue();
        FileOption fileOption = project.getFileOption(filename);
        fileOption.setContainer(containerName);
        rebuildContainerComboBox();
        fileOptionsContainerComboBox.setSelectedItem(containerName);
    }

    /**
     * Edits the container.
     */
    private void actionEditContainer() {
        String selectedFilename = (String) projectFileList.getSelectedValue();
        FileOption fileOption = project.getFileOption(selectedFilename);
        String oldContainerName = fileOption.getContainer();
        String containerName = JOptionPane.showInputDialog(wizard, I18n.getMessage("jsite.project-files.action.edit-container.message") + ":", oldContainerName);
        if (containerName == null) {
            return;
        }
        if (containerName.equals("")) {
            fileOption.setContainer("");
            fileOptionsContainerComboBox.setSelectedItem("");
            return;
        }
        List<String> files = getProjectFiles();
        for (String filename : files) {
            fileOption = project.getFileOption(filename);
            if (fileOption.getContainer().equals(oldContainerName)) {
                fileOption.setContainer(containerName);
            }
        }
        rebuildContainerComboBox();
        fileOptionsContainerComboBox.setSelectedItem(containerName);
    }

    /**
     * Deletes the container.
     */
    private void actionDeleteContainer() {
        if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.action.delete-container.message"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            String containerName = (String) fileOptionsContainerComboBox.getSelectedItem();
            List<String> files = getProjectFiles();
            for (String filename : files) {
                FileOption fileOption = project.getFileOption(filename);
                if (fileOption.getContainer().equals(containerName)) {
                    fileOption.setContainer("");
                }
            }
            fileOptionsContainerComboBox.setSelectedItem("");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates the file list.
     */
    public void fileScannerFinished(FileScanner fileScanner) {
        final boolean error = fileScanner.isError();
        if (!error) {
            final List<String> files = fileScanner.getFiles();
            SwingUtilities.invokeLater(new Runnable() {

                @SuppressWarnings("synthetic-access")
                public void run() {
                    projectFileList.setListData(files.toArray(new String[files.size()]));
                    projectFileList.clearSelection();
                    rebuildContainerComboBox();
                }
            });
            Set<String> entriesToRemove = new HashSet<String>();
            Iterator<String> filenames = new HashSet<String>(project.getFileOptions().keySet()).iterator();
            while (filenames.hasNext()) {
                String filename = filenames.next();
                if (!files.contains(filename)) {
                    entriesToRemove.add(filename);
                }
            }
            for (String filename : entriesToRemove) {
                project.setFileOption(filename, null);
            }
        } else {
            JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.scan-error"), null, JOptionPane.ERROR_MESSAGE);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @SuppressWarnings("synthetic-access")
            public void run() {
                wizard.setPreviousEnabled(true);
                wizard.setNextEnabled(!error);
                wizard.setQuitEnabled(true);
            }
        });
    }

    /**
     * Returns the {@link FileOption file options} for the currently selected
     * file.
     *
     * @return The {@link FileOption}s for the selected file, or {@code null} if
     *         no file is selected
     */
    private FileOption getSelectedFile() {
        String filename = (String) projectFileList.getSelectedValue();
        if (filename == null) {
            return null;
        }
        return project.getFileOption(filename);
    }

    //
    // INTERFACE ActionListener
    //
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if ((source instanceof JCheckBox) && ("ignore-hidden-files".equals(((JCheckBox) source).getName()))) {
            project.setIgnoreHiddenFiles(((JCheckBox) source).isSelected());
            actionScan();
            return;
        }
        String filename = (String) projectFileList.getSelectedValue();
        if (filename == null) {
            return;
        }
        FileOption fileOption = project.getFileOption(filename);
        if (source instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) source;
            if ("default-file".equals(checkBox.getName())) {
                if (checkBox.isSelected()) {
                    project.setIndexFile(filename);
                } else {
                    project.setIndexFile(null);
                }
            } else if ("insert".equals(checkBox.getName())) {
                boolean isInsert = checkBox.isSelected();
                fileOption.setInsert(isInsert);
                if (!isInsert) {
                    fileOptionsContainerComboBox.setSelectedItem("");
                }
                fileOptionsInsertRedirectCheckBox.setEnabled(!isInsert);
            } else if ("insert-redirect".equals(checkBox.getName())) {
                boolean isInsertRedirect = checkBox.isSelected();
                fileOption.setInsertRedirect(isInsertRedirect);
                fileOptionsCustomKeyTextField.setEnabled(isInsertRedirect);
            } else if ("rename".equals(checkBox.getName())) {
                boolean isRenamed = checkBox.isSelected();
                fileOptionsRenameTextField.setEnabled(isRenamed);
                fileOption.setChangedName(isRenamed ? fileOptionsRenameTextField.getText() : "");
            } else if ("project-files.replace-edition".equals(checkBox.getName())) {
                boolean replaceEdition = checkBox.isSelected();
                fileOption.setReplaceEdition(replaceEdition);
                replaceEditionRangeSpinner.setEnabled(replaceEdition);
            }
        } else if (source instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) source;
            if ("project-files.mime-type".equals(comboBox.getName())) {
                fileOption.setMimeType((String) comboBox.getSelectedItem());
            } else if ("project-files.container".equals(comboBox.getName())) {
                String containerName = (String) comboBox.getSelectedItem();
                fileOption.setContainer(containerName);
                boolean enabled = !"".equals(containerName);
                editContainerAction.setEnabled(enabled);
                deleteContainerAction.setEnabled(enabled);
                if (enabled) {
                    fileOptionsInsertCheckBox.setSelected(true);
                }
            }
        }
    }

    //
    // INTERFACE ListSelectionListener
    //
    /**
     * {@inheritDoc}
     */
    public void valueChanged(ListSelectionEvent e) {
        String filename = (String) projectFileList.getSelectedValue();
        boolean enabled = filename != null;
        boolean insert = fileOptionsInsertCheckBox.isSelected();

        System.out.println("valueChanged" + "pFilesPage 712");

        defaultFileCheckBox.setEnabled(enabled);
        fileOptionsInsertCheckBox.setEnabled(enabled);
        fileOptionsRenameCheckBox.setEnabled(enabled);
        fileOptionsMIMETypeComboBox.setEnabled(enabled);
        fileOptionsContainerComboBox.setEnabled(enabled);
        addContainerAction.setEnabled(enabled);
        editContainerAction.setEnabled(enabled);
        deleteContainerAction.setEnabled(enabled);
        replacementCheckBox.setEnabled(enabled && insert);
        if (filename != null) {
            FileOption fileOption = project.getFileOption(filename);
            defaultFileCheckBox.setSelected(filename.equals(project.getIndexFile()));
            fileOptionsInsertCheckBox.setSelected(fileOption.isInsert());
            fileOptionsInsertRedirectCheckBox.setEnabled(!fileOption.isInsert());
            fileOptionsInsertRedirectCheckBox.setSelected(fileOption.isInsertRedirect());
            fileOptionsCustomKeyTextField.setEnabled(fileOption.isInsertRedirect());
            fileOptionsCustomKeyTextField.setText(fileOption.getCustomKey());
            fileOptionsRenameCheckBox.setSelected(fileOption.hasChangedName());
            fileOptionsRenameTextField.setEnabled(fileOption.hasChangedName());
            fileOptionsRenameTextField.setText(fileOption.getChangedName());
            fileOptionsMIMETypeComboBox.getModel().setSelectedItem(fileOption.getMimeType());
            fileOptionsContainerComboBox.setSelectedItem(fileOption.getContainer());
            replacementCheckBox.setSelected(fileOption.getReplaceEdition());
            replaceEditionRangeSpinner.setValue(fileOption.getEditionRange());
            replaceEditionRangeSpinner.setEnabled(fileOption.getReplaceEdition());
        } else {
            defaultFileCheckBox.setSelected(false);
            fileOptionsInsertCheckBox.setSelected(true);
            fileOptionsInsertRedirectCheckBox.setEnabled(false);
            fileOptionsInsertRedirectCheckBox.setSelected(false);
            fileOptionsCustomKeyTextField.setEnabled(false);
            fileOptionsCustomKeyTextField.setText("CHK@");
            fileOptionsRenameCheckBox.setEnabled(false);
            fileOptionsRenameCheckBox.setSelected(false);
            fileOptionsRenameTextField.setEnabled(false);
            fileOptionsRenameTextField.setText("");
            fileOptionsMIMETypeComboBox.getModel().setSelectedItem(DefaultMIMETypes.DEFAULT_MIME_TYPE);
            fileOptionsContainerComboBox.setSelectedItem("");
            replacementCheckBox.setSelected(false);
            replaceEditionRangeSpinner.setValue(0);
        }
    }

    //
    // INTERFACE DocumentListener
    //
    /**
     * Updates the options of the currently selected file with the changes made
     * in the “custom key” textfield.
     *
     * @param documentEvent
     *            The document event to process
     */
    private void processDocumentUpdate(DocumentEvent documentEvent) {
        String filename = (String) projectFileList.getSelectedValue();
        if (filename == null) {
            return;
        }
        FileOption fileOption = project.getFileOption(filename);
        Document document = documentEvent.getDocument();
        try {
            String text = document.getText(0, document.getLength());
            fileOption.setCustomKey(text);
        } catch (BadLocationException ble1) {
            /* ignore. */
        }
    }

    /**
     * {@inheritDoc}
     */
    public void changedUpdate(DocumentEvent documentEvent) {
        processDocumentUpdate(documentEvent);
    }

    /**
     * {@inheritDoc}
     */
    public void insertUpdate(DocumentEvent documentEvent) {
        processDocumentUpdate(documentEvent);
    }

    /**
     * {@inheritDoc}
     */
    public void removeUpdate(DocumentEvent documentEvent) {
        processDocumentUpdate(documentEvent);
    }

    //
    // INTERFACE ChangeListener
    //
    /**
     * {@inheritDoc}
     */
    public void stateChanged(ChangeEvent changeEvent) {
        String filename = (String) projectFileList.getSelectedValue();
        if (filename == null) {
            return;
        }
        FileOption fileOption = project.getFileOption(filename);
        Object source = changeEvent.getSource();
        if (source instanceof JSpinner) {
            JSpinner spinner = (JSpinner) source;
            fileOption.setEditionRange((Integer) spinner.getValue());
        }
    }
}
