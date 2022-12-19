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
package org.talend.designer.unitemap.language.teradata;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.INode;
import org.talend.designer.unitemap.UniteMapComponent;
import org.talend.designer.unitemap.external.data.ExternalDbMapTable;
import org.talend.designer.unitemap.language.generation.DbGenerationManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: GenerationManager.java 1299 2007-01-05 14:53:10Z amaumont $
 *
 */
public class TeradataGenerationManager extends DbGenerationManager {

    public TeradataGenerationManager() {
        super(new TeradataLanguage());
    }

    @Override
    public String buildSqlSelect(UniteMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    @Override
    protected ExternalDbMapTable removeUnmatchingEntriesWithColumnsOfMetadataTable(ExternalDbMapTable externalDbMapTable,
            IMetadataTable metadataTable) {
        return externalDbMapTable; // keep original, don't change
    }

    @Override
    public String buildSqlSelect(INode dbMapComponent, String outputTableName, String tabString) {
        // TODO Auto-generated method stub
        return null;
    }
}
