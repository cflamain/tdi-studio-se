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
package org.talend.designer.unitemap.ui.proposal.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.proposal.ContextParameterProposal;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.unitemap.language.IDbLanguage;
import org.talend.designer.unitemap.managers.MapperManager;
import org.talend.designer.unitemap.model.tableentry.TableEntryLocation;
import org.talend.designer.unitemap.ui.visualmap.zone.Zone;

/**
 * ContentProposalProvider which initialize valid locations of Mapper. <br/>
 *
 * $Id: ExpressionProposalProvider.java 968 2006-12-12 10:59:26Z amaumont $
 *
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private MapperManager mapperManager;

    private List<IDataMapTable> tables;

    private IDbLanguage currentLanguage;

    private IContentProposalProvider[] otherContentProposalProviders;

    private ITableEntry currentModifiedEntry;

    /**
     * Constructs a new ProcessProposalProvider.
     *
     * @param tables
     * @param control
     */
    public ExpressionProposalProvider(MapperManager mapperManager,
            IContentProposalProvider[] otherContentProposalProviders) {
        super();
        this.mapperManager = mapperManager;
        this.currentLanguage = mapperManager.getCurrentLanguage();
        this.otherContentProposalProviders = otherContentProposalProviders;
    }

    public void init(IDataMapTable currentTable, Zone[] zones, ITableEntry currentEntry) {

        tables = new ArrayList<IDataMapTable>();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == Zone.INPUTS) {
                tables.addAll(mapperManager.getInputTables());
            } else if (zones[i] == Zone.OUTPUTS) {
                tables.addAll(mapperManager.getOutputTables());
            }
        }
        this.currentModifiedEntry = currentEntry;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
     */
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> proposals = new ArrayList<IContentProposal>();

        TableEntryLocation sourceEntryLocation = new TableEntryLocation();

        // Proposals based on process context
        for (IDataMapTable table : this.tables) {
            // proposals.add(new TableContentProposal(table, this.currentLanguage));
            List<IColumnEntry> dataMapTableEntries = table.getColumnEntries();
            for (IColumnEntry entrySource : dataMapTableEntries) {

                sourceEntryLocation.tableName = entrySource.getParentName();
                sourceEntryLocation.columnName = entrySource.getName();
                proposals.add(new EntryContentProposal(entrySource, this.currentLanguage));
            }
        }

        proposals.addAll(getContextProposal());
        
        IContentProposal[] res = new IContentProposal[proposals.size()];
        res = proposals.toArray(res);
        return res;
    }
    
    private List<IContentProposal> getContextProposal() {
    	List<IContentProposal> proposals = new ArrayList<IContentProposal>();
    	IProcess process = mapperManager.getComponent().getProcess();
    	if(mapperManager == null || mapperManager.getComponent() == null) {
    		return proposals;
    	}
        if (process != null) {
            // Proposals based on process context
            List<IContextParameter> ctxParams = process.getContextManager().getDefaultContext().getContextParameterList();
            for (IContextParameter ctxParam : ctxParams) {
                proposals.add(new ContextParameterProposal(ctxParam));
            }

        } else {
            List<ContextItem> allContextItem = ContextUtils.getAllContextItem();
            List<IContextParameter> ctxParams = new ArrayList<IContextParameter>();
            if (allContextItem != null) {
                for (ContextItem item : allContextItem) {
                    List<IContextParameter> tmpParams = new JobContextManager(item.getContext(), item.getDefaultContext())
                            .getDefaultContext().getContextParameterList();
                    ctxParams.addAll(tmpParams);
                }
            }
            for (IContextParameter ctxParam : ctxParams) {
                proposals.add(new ContextParameterProposal(ctxParam));
            }
        }

        // sort the list
        Collections.sort(proposals, new Comparator<IContentProposal>() {

            @Override
            public int compare(IContentProposal arg0, IContentProposal arg1) {
                return compareRowAndContextProposal(arg0.getLabel(), arg1.getLabel());
            }

        });
        return proposals;
    }
    
    private int compareRowAndContextProposal(String label0, String label1) {
        if (label0.startsWith("$row[") && label1.startsWith("context")) { //$NON-NLS-1$ //$NON-NLS-2$
            return 1;
        } else if (label1.startsWith("$row[") && label0.startsWith("context")) { //$NON-NLS-1$ //$NON-NLS-2$
            return -1;
        } else {
            return label0.compareToIgnoreCase(label1);
        }
    }

}
