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
package org.talend.designer.runprocess.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessContextManager;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.ProcessComposite;
import org.talend.designer.runprocess.ui.actions.ClearPerformanceAction;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public class DefaultProcessViewHelper implements IProcessViewHelper {

    private static Logger log = Logger.getLogger(ProcessView.class);

    /**
     * DOC amaumont Comment method "getProcessComposite".
     * 
     * @param container
     * @return
     */
    public ProcessComposite getProcessComposite(final Composite container) {
        return new ProcessComposite(container, SWT.NONE);
    }

}
