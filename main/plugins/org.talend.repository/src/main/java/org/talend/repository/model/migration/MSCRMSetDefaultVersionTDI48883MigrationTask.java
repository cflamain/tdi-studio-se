//============================================================================
//
//Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.repository.model.migration;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class MSCRMSetDefaultVersionTDI48883MigrationTask extends AbstractJobMigrationTask{

	@Override
	public Date getOrder() {
	    GregorianCalendar gc = new GregorianCalendar(2022, 11, 12, 11, 0, 0);
	    return gc.getTime();
	}

	@Override
	public ExecutionResult execute(Item item) {
	    ProcessType processType = getProcessType(item);
	    if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
		return ExecutionResult.NOTHING_TO_DO;
	    }

	    String [] componentsNameToAffect = new String [] {
		    "tMicrosoftCrmInput",
		    "tMicrosoftCrmOutput"
	    };

	    IComponentConversion setDefaultVersion = new IComponentConversion() {

		@Override
		public void transform(NodeType node) {
		    String authType = ComponentUtilities.getNodePropertyValue(node, "AUTH_TYPE");
		    if ("ONLINE".equals(authType)) {
			String apiVersion = ComponentUtilities.getNodePropertyValue(node, "API_VERSION");
			if(apiVersion == null || apiVersion.trim().isEmpty()){
				ComponentUtilities.addNodeProperty(node, "API_VERSION", "CLOSED_LIST");
				ComponentUtilities.setNodeValue(node, "API_VERSION","API_2011");
			}
		    }
		    else if ("ON_PREMISE".equals(authType)){
			String msCrmVersion = ComponentUtilities.getNodePropertyValue(node, "MS_CRM_VERSION");
			if(msCrmVersion == null || msCrmVersion.trim().isEmpty()){
				ComponentUtilities.addNodeProperty(node, "MS_CRM_VERSION", "CLOSED_LIST");
				ComponentUtilities.setNodeValue(node, "MS_CRM_VERSION", "CRM_2011");
			}
		    }
		}
	    };

	    boolean modified = false;
	    for (String componentName : componentsNameToAffect) {
		IComponentFilter componentFilter = new NameComponentFilter(componentName);
		try {
		    modified |= ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
			    Collections.singletonList(setDefaultVersion));
		} catch (PersistenceException e) {
		    ExceptionHandler.process(e);
		    return ExecutionResult.FAILURE;
		}
	    }
	    if (modified) {
		return ExecutionResult.SUCCESS_NO_ALERT;
	    } else {
		return ExecutionResult.NOTHING_TO_DO;
	    }
	}


}
