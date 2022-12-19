package org.talend.designer.dbmap;

import org.talend.designer.abstractmap.AbstractMapComponent;
import org.talend.designer.dbmap.language.generation.AbstractDbGenerationManager;

public abstract class AbstractUniteMapComponent extends AbstractMapComponent {

    public abstract AbstractDbGenerationManager getGenerationManager();
}
