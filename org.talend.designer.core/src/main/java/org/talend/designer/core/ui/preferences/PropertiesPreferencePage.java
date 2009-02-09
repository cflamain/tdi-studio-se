// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.preferences;

import org.eclipse.gmf.runtime.common.ui.preferences.AbstractPreferencePage;
import org.eclipse.gmf.runtime.common.ui.preferences.FontFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

/**
 * yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: PropertiesPreferencePage.java 下午02:13:35 2007-8-24 +0000 (2007-8-24) yzhang $
 * 
 */
public class PropertiesPreferencePage extends AbstractPreferencePage {

    private final String groupName = "Font"; //$NON-NLS-1$

    private FontFieldEditor memoFontEditor = null;

    /**
     * yzhang PropertiesPreferencePage constructor comment.
     */
    public PropertiesPreferencePage() {
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.common.ui.preferences.AbstractPreferencePage#addFields(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void addFields(Composite parent) {
        Composite main = createPageLayout(parent);
        createFontAndColorGroup(main);
    }

    /**
     * yzhang Comment method "createPageLayout".
     * 
     * @param parent
     * @return
     */
    private Composite createPageLayout(Composite parent) {
        Composite main = new Composite(parent, SWT.NULL);
        main.setLayout(new GridLayout());
        main.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        return main;
    }

    /**
     * yzhang Comment method "createFontAndColorGroup".
     * 
     * @param parent
     * @return
     */
    protected Composite createFontAndColorGroup(Composite parent) {

        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(3, false));
        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        composite.setLayoutData(gridData);
        group.setText(groupName);

        addFontAndColorFields(composite);

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        composite.setLayout(layout);

        return group;
    }

    protected void addFontAndColorFields(Composite composite) {
        memoFontEditor = new FontFieldEditor(TalendDesignerPrefConstants.MEMO_TEXT_FONT, Messages.getString("PropertiesPreferencePage.textFont"), composite); //$NON-NLS-1$
        addField(memoFontEditor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.common.ui.preferences.AbstractPreferencePage#initHelp()
     */
    @Override
    protected void initHelp() {
        // do nothing.
    }

}
