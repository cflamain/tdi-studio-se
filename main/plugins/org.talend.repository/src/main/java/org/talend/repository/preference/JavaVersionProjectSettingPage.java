// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.preference;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.AbstractProjectSettingPage;
import org.talend.core.runtime.util.ModuleAccessHelper;
import org.talend.repository.i18n.Messages;

public class JavaVersionProjectSettingPage extends AbstractProjectSettingPage {

    private Combo javaVersionCombo;

    private String javaVersion;

    private Button accessCheckbox;

    private CLabel accessWarnLabel;

    private Label accessConfigLabel;

    private CTabFolder tabFolder;

    private StyledText defaultText;

    private StyledText customText;

    public JavaVersionProjectSettingPage() {
        noDefaultAndApplyButton();
    }

    @Override
    protected String getPreferenceName() {
        return CoreRuntimePlugin.PLUGIN_ID;
    }

    @Override
    protected void createFieldEditors() {
        javaVersion = JavaUtils.getProjectJavaVersion();
        if (javaVersion == null) {
            javaVersion = JavaUtils.DEFAULT_VERSION;
            JavaUtils.updateProjectJavaVersion(javaVersion);
        }
        Composite parent = getFieldEditorParent();
        parent.setLayout(new GridLayout(2, true));
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        Label javaVersionLabel = new Label(parent, SWT.NONE);
        javaVersionLabel.setText(Messages.getString("JavaVersionProjectSettingPage.versionLabel")); //$NON-NLS-1$
        GridData labelData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        javaVersionLabel.setLayoutData(labelData);
        javaVersionCombo = new Combo(parent, SWT.READ_ONLY);
        GridData comboData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        GC gc = new GC(javaVersionCombo);
        Point labelSize = gc.stringExtent(JavaUtils.DEFAULT_VERSION);
        gc.dispose();
        int hint = labelSize.x + (ITabbedPropertyConstants.HSPACE * 14);
        comboData.widthHint = hint;
        javaVersionCombo.setLayoutData(comboData);
        javaVersionCombo.setItems(JavaUtils.AVAILABLE_VERSIONS.toArray(new String[] {}));
        javaVersionCombo.select(JavaUtils.AVAILABLE_VERSIONS.indexOf(javaVersion));

        accessCheckbox = new Button(parent, SWT.CHECK);
        accessCheckbox.setText(Messages.getString("JavaVersionProjectSettingPage.internalAccessLabel")); //$NON-NLS-1$
        boolean selected = getPreferenceStore().getBoolean(JavaUtils.ALLOW_JAVA_INTERNAL_ACCESS);
        accessCheckbox.setSelection(selected);
        GridData checkboxData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1);
        accessCheckbox.setLayoutData(checkboxData);
        accessCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selection = accessCheckbox.getSelection();
                accessWarnLabel.setVisible(selection);
                accessConfigLabel.setVisible(selection);
                tabFolder.setVisible(accessCheckbox.getSelection());
            }

        });

        accessWarnLabel = new CLabel(parent, SWT.NONE);
        accessWarnLabel.setText(Messages.getString("JavaVersionProjectSettingPage.internalAccessWarningLabel"));//$NON-NLS-1$
        accessWarnLabel.setImage(ImageProvider.getImage(EImage.WARNING_ICON));
        accessWarnLabel.setVisible(selected);

        accessConfigLabel = new Label(parent, SWT.NONE);
        accessConfigLabel.setText(Messages.getString("JavaVersionProjectSettingPage.accessSettingLabel")); //$NON-NLS-1$
        accessConfigLabel.setVisible(accessCheckbox.getSelection());
        GridData scriptLabelData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1);
        accessConfigLabel.setLayoutData(scriptLabelData);

        createTabArea(parent);
    }

    private void createTabArea(Composite parent) {
        tabFolder = new CTabFolder(parent, SWT.BORDER);
        // tabFolder.setTabPosition(SWT.BOTTOM);
        tabFolder.setSimple(false);

        GridData data = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
        tabFolder.setLayoutData(data);
        data.heightHint = 280;
        data.minimumHeight = 280;
        data.widthHint = 500;
        data.minimumWidth = 500;
        tabFolder.setVisible(accessCheckbox.getSelection());
        tabFolder.setBorderVisible(accessCheckbox.getSelection());

        CTabItem defaultTabItem = new CTabItem(tabFolder, SWT.NULL);
        defaultTabItem.setText(Messages.getString("JavaVersionProjectSettingPage.defaultSettingTabLabel")); //$NON-NLS-1$
        CTabItem customTabItem = new CTabItem(tabFolder, SWT.NULL);
        customTabItem.setText(Messages.getString("JavaVersionProjectSettingPage.customSettingTabLabel")); //$NON-NLS-1$

        tabFolder.setSelection(defaultTabItem);

        int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        defaultText = new StyledText(tabFolder, style);
        defaultText.setText(getDefaultSettings());
        defaultText.setBackground(new Color(null, 233, 233, 233));
        defaultText.setEditable(false);
        defaultTabItem.setControl(defaultText);

        customText = new StyledText(tabFolder, style);
        customText.setText(getCustomSettings());
        customTabItem.setControl(customText);
    }

    private String getDefaultSettings() {
        String content = ""; //$NON-NLS-1$
        try {
            content = IOUtils.toString(ModuleAccessHelper.getConfigFileURL().toURI(), "UTF-8"); //$NON-NLS-1$
        } catch (URISyntaxException | IOException e) {
            ExceptionHandler.process(e);
        }
        return content;
    }

    private String getCustomSettings() {
        String content = getPreferenceStore().getString(JavaUtils.CUSTOM_ACCESS_SETTINGS);
        if (StringUtils.isBlank(content)) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n");//$NON-NLS-1$
            builder.append("# Setup Java internal modules for your custom components,\n");//$NON-NLS-1$
            builder.append("# Settings will be applied to Jobs which use these components.\n");//$NON-NLS-1$
            builder.append("#tSnowflakeConnection=java.base/java.nio\n");//$NON-NLS-1$
            builder.append("\n");//$NON-NLS-1$
            builder.append("# Setup global Java internal modules,\n");//$NON-NLS-1$
            builder.append("# Settings will be applied to all Jobs.\n");//$NON-NLS-1$
            builder.append("#GLOBAL=java.base/java.lang,java.base/java.io\n");//$NON-NLS-1$
            content = builder.toString();
        }
        return content;
    }

    @Override
    public boolean performOk() {
        if (javaVersionCombo == null) {
            return true;
        }
        String newVersion = javaVersionCombo.getItem(javaVersionCombo.getSelectionIndex());
        if (!newVersion.equals(javaVersion)) {
            JavaUtils.updateProjectJavaVersion(newVersion);
        }
        getPreferenceStore().setValue(JavaUtils.ALLOW_JAVA_INTERNAL_ACCESS, accessCheckbox.getSelection());
        String customContent = customText.getText();
        if (StringUtils.isNotBlank(customContent)) {
            if (!customContent.startsWith("\n") && !customContent.startsWith("\r\n")) { //$NON-NLS-1$ //$NON-NLS-2$
                customContent = "\n" + customContent; //$NON-NLS-1$
            }
            getPreferenceStore().setValue(JavaUtils.CUSTOM_ACCESS_SETTINGS, customContent);
            ModuleAccessHelper.reset();
        }
        return super.performOk();
    }

}
