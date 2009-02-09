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
package org.talend.designer.core.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.contexts.Contexts;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * Track the active Process being edited. <br/>
 * 
 * $Id$
 * 
 */
public class ActiveProcessTracker implements IPartListener {

    private static ActiveProcessTracker apt = null;

    public static void initialize() {
        if (apt == null) {
            apt = new ActiveProcessTracker();
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(apt);
        }
    }

    private static IProcess currentProcess;

    private static IProcess lastProcessOpened;

    public IProcess getJobFromActivatedEditor(IWorkbenchPart part) {
        IWorkbenchPart testedPart = part;
        if (!(part instanceof AbstractMultiPageTalendEditor)) {
            testedPart = part.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
        }

        if (testedPart instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) testedPart;
            mpte.setName();
            Contexts.setTitle(mpte.getTitle());
            IProcess process = mpte.getTalendEditor().getProcess();
            return process;
        } else {
            // No editor
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(final IWorkbenchPart part) {
        if (part instanceof AbstractMultiPageTalendEditor) {
            ComponentUtilities.setExtraEntryVisible(((AbstractMultiPageTalendEditor) part).showExtraPaletteEntry());
        }
        // else if (part instanceof TalendPaletteView) {
        // ComponentUtilities.updatePalette(true);
        // } else if (part instanceof PaletteView) {
        // ComponentUtilities.updatePalette(false);
        // }

        // IProcess process = getJobFromActivatedEditor(part);
        // if (process != null) {
        // currentProcess = process;
        // if (process instanceof Process) {
        // Process p = (Process) process;
        // if (!p.isReadOnly() && p.isActivate()) {
        // p.checkDifferenceWithRepository();
        // }
        // }
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        IProcess process = getJobFromActivatedEditor(part);
        if (process != null && currentProcess != process) {
            currentProcess = process;
            setContextsView(process);
            // setStatsAndLogsView(process);
            JobSettings.switchToCurJobSettingsView();

            // if (process instanceof Process) {
            // Process p = (Process) process;
            // if (!p.isReadOnly() && p.isActivate()) {
            // p.checkDifferenceWithRepository();
            // }
            // }
        }
    }

    /**
     * ftang Comment method "setStatsAndLogsView".
     * 
     * @param process
     */
    // private void setStatsAndLogsView(IProcess process) {
    // StatsAndLogs.setTitle("Job " + process.getProperty().getLabel()); //$NON-NLS-1$
    // StatsAndLogs.switchToCurStatsAndLogsView();
    // }
    /**
     * qzhang Comment method "setProblemsView".
     * 
     * @param process
     */
    private void addJobInProblemView(IProcess process) {
        Problems.addProcess(process);

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        service.setActiveProcess(process);
    }

    /**
     * qzhang Comment method "setProblemsView".
     * 
     * @param process
     */
    private void setContextsView(IProcess process) {

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        service.setActiveProcess(process, false);

        // Contexts.setTitle("Job " + process.getProperty().getLabel()); //$NON-NLS-1$
        Contexts.switchToCurContextsView();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        if (part instanceof AbstractMultiPageTalendEditor && currentProcess != null) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            if (mpte.isKeepPropertyLocked()) {
                currentProcess = null;
                return;
            }

            IProcess process = getJobFromActivatedEditor(part);
            if (process != null) {
                Problems.removeProblemsByProcess(process);
                Problems.removeJob(process);
                IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
                service.removeProcess(process);

                if (currentProcess == process) {
                    Contexts.setTitle(""); //$NON-NLS-1$
                    Contexts.clearAll();
                    JobSettings.cleanDisplay();
                    if (lastProcessOpened == currentProcess) {
                        lastProcessOpened = null;
                    }
                    currentProcess = null;
                }
                UIUtils.closeSqlBuilderDialogs(process.getName());
            }

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
        IProcess process = getJobFromActivatedEditor(part);
        if (process != null && (part instanceof AbstractMultiPageTalendEditor)) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            mpte.getTalendEditor().savePaletteState();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
        if (part instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            if (mpte.isJobAlreadyOpened()) {
                mpte.updateChildrens();
                // close the first editor and keep the new one. (so only one will remain)
                IEditorReference[] ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditors(
                        mpte.getEditorInput(), mpte.getEditorId(), IWorkbenchPage.MATCH_INPUT);
                IEditorPart editorPart = ref[0].getEditor(false);
                editorPart.doSave(new NullProgressMonitor());
                ((AbstractMultiPageTalendEditor) editorPart).setKeepPropertyLocked(true);
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editorPart, false);
            }
        }
        IProcess process = getJobFromActivatedEditor(part);
        if (process != null && currentProcess != process && lastProcessOpened != process) {
            lastProcessOpened = process;
            addJobInProblemView(process);
            // JobSettings.switchToCurJobSettingsView();
        }
    }

    /**
     * Getter for currentProcess.
     * 
     * @return the currentProcess
     */
    public static Process getCurrentProcess() {
        return (Process) currentProcess;
    }
}
