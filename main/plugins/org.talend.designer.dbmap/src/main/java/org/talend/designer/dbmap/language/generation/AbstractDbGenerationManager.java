package org.talend.designer.dbmap.language.generation;

import org.talend.core.model.process.INode;

public abstract class AbstractDbGenerationManager {

    public abstract String buildSqlSelect(INode dbMapComponent, String outputTableName, String tabString);
}
