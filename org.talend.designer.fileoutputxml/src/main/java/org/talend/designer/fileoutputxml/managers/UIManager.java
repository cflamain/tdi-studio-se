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
package org.talend.designer.fileoutputxml.managers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.fileoutputxml.data.Element;
import org.talend.designer.fileoutputxml.data.FOXTreeNode;
import org.talend.designer.fileoutputxml.ui.FOXUI;

/**
 * UI Manager<br/>
 * 
 * $Id: UIManager.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 * 
 */
public class UIManager {

    private FOXUI foxUI;

    private int uiResponse = SWT.NONE;

    private FOXManager foxManager;

    /**
     * UIManager constructor .
     * 
     * @param foxManager
     */
    public UIManager(FOXManager foxManager) {
        this.foxManager = foxManager;
    }

    /**
     * Getter for foxManager.
     * 
     * @return the foxManager
     */
    public FOXManager getFoxManager() {
        return this.foxManager;
    }

    /**
     * Sets the foxManager.
     * 
     * @param foxManager the foxManager to set
     */
    public void setFoxManager(FOXManager foxManager) {
        this.foxManager = foxManager;
    }

    /**
     * Getter for foxUI.
     * 
     * @return the foxUI
     */
    public FOXUI getFoxUI() {
        return this.foxUI;
    }

    /**
     * Sets the foxUI.
     * 
     * @param foxUI the foxUI to set
     */
    public void setFoxUI(FOXUI foxUI) {
        this.foxUI = foxUI;
    }

    /**
     * Getter for uiResponse.
     * 
     * @return the uiResponse
     */
    public int getUiResponse() {
        return this.uiResponse;
    }

    /**
     * Sets the uiResponse.
     * 
     * @param uiResponse the uiResponse to set
     */
    public void setUiResponse(int uiResponse) {
        this.uiResponse = uiResponse;
    }

    /**
     * DOC qiang.zhang Comment method "closeFOX".
     * 
     * @param reponse
     */
    public void closeFOX(int response) {

        if (response == SWT.CANCEL) {
            setUiResponse(SWT.CANCEL);
        }

        if (response == SWT.OK) {
            if (saveAllData()) {
                setUiResponse(SWT.OK);
            } else {
                setUiResponse(SWT.CANCEL);
            }
        }
        Composite parent = foxUI.getFoxUIParent();
        if (parent instanceof Shell) {
            ((Shell) parent).close();
        }

    }

    /**
     * 
     * DOC xzhang Comment method "autoMap".
     */
    public void autoMap() {
        FOXTreeNode root = this.foxManager.getTreeData().get(0);
        List<FOXTreeNode> mappableNodes = new ArrayList<FOXTreeNode>();
        getMappableNode((Element) root, mappableNodes);
        List<IMetadataColumn> schemaData = this.foxManager.getSchemaData();

        for (IMetadataColumn column : schemaData) {
            for (FOXTreeNode node : mappableNodes) {
                if (node.getLabel().equals(column.getLabel())) {
                    node.setColumn(column);
                    break;
                }
            }
        }
        this.foxUI.refreshXMLViewer(root);
        this.foxUI.redrawLinkers();
    }

    private void getMappableNode(Element node, List<FOXTreeNode> mappableNodes) {
        if (node.getElementChildren().size() == 0) {
            if (node.getColumn() == null) {
                mappableNodes.add(node);
            }
        }
        for (FOXTreeNode attri : node.getAttributeChildren()) {
            if (attri.getColumn() == null) {
                mappableNodes.add(attri);
            }
        }
        for (FOXTreeNode elem : node.getElementChildren()) {
            getMappableNode((Element) elem, mappableNodes);
        }
    }

    /**
     * DOC gke Comment method "saveAllData".
     */
    private boolean saveAllData() {
        return foxManager.saveDataToComponent();

    }

}
