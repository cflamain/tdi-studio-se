package org.talend.designer.unitemap.service;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.designer.core.IDbMapDesignerService;
import org.talend.designer.unitemap.command.UpdateELTMapComponentCommand;

public class DbMapDesignerService implements IDbMapDesignerService {

    @Override
    public Command getUpdateELTMapComponentCommand(INode targetNode, IConnection connection, String oldConnectionName,
            String newConnectionName) {
        return new UpdateELTMapComponentCommand(targetNode, connection, oldConnectionName, newConnectionName);
    }
}
