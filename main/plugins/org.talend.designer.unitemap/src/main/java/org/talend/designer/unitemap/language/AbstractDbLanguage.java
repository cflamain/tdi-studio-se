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
package org.talend.designer.unitemap.language;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.designer.unitemap.language.operator.IDbOperatorManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: AbstractLanguage.java 1877 2007-02-06 17:16:43Z amaumont $
 *
 */
public abstract class AbstractDbLanguage implements IDbLanguage {

    private IDbOperatorManager operatorsManager;

    protected ICodeProblemsChecker codeChecker;

    /**
     *
     * Database joints.
     *
     * $Id$
     *
     */
    public static enum JOIN implements IJoinType {
        UNION("UNION"), //$NON-NLS-1$
        UNION_ALL("UNION ALL"), //$NON-NLS-1$
        MINUS("MINUS"), //$NON-NLS-1$
        INTERSECT("INTERSECT"); //$NON-NLS-1$

        String label;

        JOIN(String label) {
            this.label = label;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        @Override
        public String getLabel() {
            return this.label;
        }

        public static IJoinType getJoin(String joinType) {
            if (joinType == null) {
                return UNION;
            }
            return valueOf(joinType);
        }

    };

    /**
     *
     */
    public AbstractDbLanguage(IDbOperatorManager operatorsManager) {
        this.operatorsManager = operatorsManager;

    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.designer.dbmap.language.IDbLanguage#getLocation(org.talend.designer.dbmap.model.tableentry.
     * TableEntryLocation)
     */
    @Override
    public String getLocation(String tableName, String columnName) {
        return StringHelper.replacePrms(getTemplateTableColumnVariable(), new Object[] { tableName, columnName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getLocation(java.lang.String)
     */
    @Override
    public String getLocation(String tableName) {
        return StringHelper.replacePrms(getTemplateTableVariable(), new Object[] { tableName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getCodeChecker()
     */
    @Override
    public ICodeProblemsChecker getCodeChecker() {
        return codeChecker;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getAvailableJoins()
     */
    @Override
    public IJoinType[] getAvailableJoins() {
        return JOIN.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getJoin(java.lang.String)
     */
    @Override
    public IJoinType getJoin(String joinType) {
        return JOIN.getJoin(joinType);
    }

    @Override
    public List<IJoinType> unuseWithExplicitJoin() {
        List<IJoinType> joins = new ArrayList<IJoinType>();
        joins.add(JOIN.UNION);
        return joins;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getCouplePattern()
     */
    @Override
    public String getLocationPattern() {
        return DbLanguageConstants.LOCATION_PATTERN;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getPREFIX_FIELD_NAME_REGEXP()
     */
    @Override
    public String getPrefixFieldRegexp() {
        return DbLanguageConstants.PREFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getPREFIX_TABLE_NAME_REGEXP()
     */
    @Override
    public String getPrefixTableRegexp() {
        return DbLanguageConstants.PREFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSUFFIX_FIELD_NAME_REGEXP()
     */
    @Override
    public String getSuffixFieldRegexp() {
        return DbLanguageConstants.SUFFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSUFFIX_TABLE_NAME_REGEXP()
     */
    @Override
    public String getSuffixTableRegexp() {
        return DbLanguageConstants.SUFFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSubstPatternForPrefixColumnName()
     */
    @Override
    public String getSubstPatternForPrefixColumnName() {
        return DbLanguageConstants.SUBST_PATTERN_FOR_PREFIX_COLUMN_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSubstPatternForReplaceLocation()
     */
    @Override
    public String getSubstPatternForReplaceLocation() {
        return DbLanguageConstants.SUBST_PATTERN_FOR_REPLACE_LOCATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getTEMPLATE_TABLE_COLUMN_VARIABLE()
     */
    @Override
    public String getTemplateTableColumnVariable() {
        return DbLanguageConstants.TEMPLATE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTEMPLATE_PROCESS_COLUMN_VARIABLE()
     */
    @Override
    public String getTemplateVarsColumnVariable() {
        return DbLanguageConstants.TEMPLATE_VARS_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE()
     */
    @Override
    public String getTemplateGeneratedCodeTableColumnVariable() {
        return DbLanguageConstants.TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getAndCondition()
     */
    @Override
    public String getAndCondition() {
        return DbLanguageConstants.AND_CONDITION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getPrefixField()
     */
    @Override
    public String getPrefixField() {
        return DbLanguageConstants.PREFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getPrefixTable()
     */
    @Override
    public String getPrefixTable() {
        return DbLanguageConstants.PREFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getSuffixField()
     */
    @Override
    public String getSuffixField() {
        return DbLanguageConstants.SUFFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getSuffixTable()
     */
    @Override
    public String getSuffixTable() {
        return DbLanguageConstants.SUFFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTemplateTableVariable()
     */
    @Override
    public String getTemplateTableVariable() {
        return DbLanguageConstants.TEMPLATE_TABLE_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getOperatorsManager()
     */
    @Override
    public IDbOperatorManager getOperatorsManager() {
        return operatorsManager;
    }

}
