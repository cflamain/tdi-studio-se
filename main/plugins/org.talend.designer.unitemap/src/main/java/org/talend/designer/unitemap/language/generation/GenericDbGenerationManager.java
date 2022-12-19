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
package org.talend.designer.unitemap.language.generation;

import org.talend.core.model.process.INode;
import org.talend.designer.unitemap.UniteMapComponent;
import org.talend.designer.unitemap.language.GenericDbLanguage;

/**
 * wzhang class global comment. Detailled comment
 */
public class GenericDbGenerationManager extends DbGenerationManager {

    public GenericDbGenerationManager() {
        super(new GenericDbLanguage());
    }

    @Override
    public String buildSqlSelect(UniteMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    @Override
    public String buildSqlSelect(INode dbMapComponent, String outputTableName, String tabString) {
        // TODO Auto-generated method stub
        return null;
    }
}
