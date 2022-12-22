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
package org.talend.designer.unitemap.language.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.unitemap.UniteMapComponent;
import org.talend.designer.unitemap.external.data.ExternalDbMapData;
import org.talend.designer.unitemap.external.data.ExternalDbMapEntry;
import org.talend.designer.unitemap.external.data.ExternalDbMapTable;
import org.talend.designer.unitemap.language.generation.DbGenerationManager;
import org.talend.designer.unitemap.language.generation.DbMapSqlConstants;
import org.talend.designer.unitemap.language.generation.MapExpressionParser;
import org.talend.designer.unitemap.model.tableentry.TableEntryLocation;
import org.talend.designer.unitemap.utils.DataMapExpressionParser;

/**
 *
 * wzhang class global comment. Detailled comment
 */
public class OracleGenerationManager extends DbGenerationManager {

    private static final String JOIN = "(+)";

    public OracleGenerationManager() {
        super(new OracleLanguage());
    }

    @Override
    public String buildSqlSelect(UniteMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    /**
     *
     * ggu Comment method "buildSqlSelect".
     *
     * @param component
     * @param outputTableName
     * @param tabSpaceString
     * @return
     */
    @Override
    public String buildSqlSelect(UniteMapComponent dbMapComponent, String outputTableName, String tabString) {
     // TODO Auto-generated method stub
        if (dbMapComponent instanceof UniteMapComponent) {
            List<? extends IConnection> incomingConnections = dbMapComponent.getIncomingConnections();
            if (incomingConnections.size() == 0) {
                // when call from javajet (tELTOracleUniteMap -> tEltOracleOutput). incomingConnections are empty
                incomingConnections = dbMapComponent.getRealGraphicalNode().getIncomingConnections();
            }
            boolean firstSql = true;
            StringBuilder sqlText = new StringBuilder();
            for (IConnection ic : incomingConnections) {
                IExternalNode externalNode = ic.getSource().getExternalNode();
                if (externalNode instanceof DbMapComponent) {
                    ExternalDbMapData externalData = dbMapComponent.getExternalData();
                    String uniteType = "";
                    if (externalData != null) {
                        List<ExternalDbMapTable> inputTables = externalData.getInputTables();
                        for (ExternalDbMapTable edt : inputTables) {
                            if (ic.getName().equals(edt.getName())) {
                                uniteType = edt.getJoinType();
                                break;
                            }
                        }
                    }
                    String sql = getUniteSql(((DbMapComponent) externalNode)
                            .getGenerationManager()
                            .buildSqlSelect((DbMapComponent) externalNode, ic.getUniqueName()));
                    if (!firstSql) {
                        sqlText.append("\n").append("+ \"").append(uniteType).append("\" +").append("\n");
                    }
                    sqlText.append(sql);
                }
                firstSql = false;
            }
            return sqlText.toString();
        }
        return "";
    }

    @Override
    protected String getSpecialRightJoin(ExternalDbMapTable table) {
        // when use oracle's right join (+)
        // if (language.getJoin(table.getJoinType()) == OracleLanguage.ORACLEJOIN.RIGHT_OUTER_JOIN_ORACLE) {
        // return JOIN;
        // }
        return DbMapSqlConstants.EMPTY;
    }

    @Override
    protected String getSpecialLeftJoin(ExternalDbMapTable table) {
        // when use oracle's left join (+)
        if (language.getJoin(table.getJoinType()) == OracleLanguage.ORACLEJOIN.EXCEPT) {
            return JOIN + DbMapSqlConstants.SPACE;
        }
        return DbMapSqlConstants.EMPTY;
    }

    @Override
    protected String addQuoteForSpecialChar(String expression, UniteMapComponent component) {
        if (expression == null) {
            return expression;
        }
        List<String> specialList = new ArrayList<String>();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
        if (inputConnections == null) {
            return expression;
        }
        for (IConnection iconn : inputConnections) {
            IMetadataTable metadataTable = iconn.getMetadataTable();
            if (metadataTable != null) {
                List<IMetadataColumn> lColumn = metadataTable.getListColumns();
                for (IMetadataColumn co : lColumn) {
                    String columnLabel = co.getOriginalDbColumnName();
                    if (columnLabel == null) {
                        columnLabel = co.getLabel();
                    }
                    String exp = MetadataToolHelper.validateValueNoLengthLimit(columnLabel);
                    if (!exp.equals(columnLabel)) {
                        specialList.add(columnLabel);
                    }
                }
            }
        }
        for (String specialColumn : specialList) {
            if (expression.contains(specialColumn)) {
                if (map.get(expression) == null) {
                    List<String> list = new ArrayList<String>();
                    list.add(specialColumn);
                    map.put(expression, list);
                } else {
                    List<String> list = map.get(expression);
                    list.add(specialColumn);
                }
            }
        }
        if (map.size() > 0) {
            List<String> list = map.get(expression);
            Collections.sort(list);
            String specialColumn = list.get(list.size() - 1);
            if (expression.contains(specialColumn)) {
                int begin = expression.indexOf(specialColumn);
                int length = specialColumn.length();
                int allLength = expression.length();
                if (specialColumn.trim().startsWith("\\\"") && specialColumn.trim().endsWith("\\\"")) {
                    return expression;
                }
                String quote = getQuote(component);
                if ("\"".equals(quote)) {
                    quote = "\\\"";
                }
                expression = expression.substring(0, begin) + quote + expression.substring(begin, begin + length)
                        + quote + expression.substring(begin + length, allLength);
//                expression = adaptQuoteForColumnName(component,expression);
                return expression;
            }
        }
        return expression;
    }
    

    @Override
    protected boolean needAlias(List<IMetadataColumn> columns, ExternalDbMapEntry dbMapEntry, String expression) {
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(language);
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        if (tableEntriesLocationsSources.length > 1) {
            return true;
        } else {
            for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
                TableEntryLocation location = tableEntriesLocationsSource;
                String entryName = getAliasOf(dbMapEntry.getName());
                if (location != null && entryName != null && !entryName.startsWith("_") //$NON-NLS-1$
                        && !entryName.equals(location.columnName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected String initExpression(UniteMapComponent component, ExternalDbMapEntry dbMapEntry) {
        String quote = getQuote(component);
        String quto_mark = TalendQuoteUtils.QUOTATION_MARK;
        String expression = dbMapEntry.getExpression();
        if (expression != null) {
            List<Map<String, String>> itemNameList = null;

            // MapExpressionParser mapParser = new MapExpressionParser("((\\s*(\\w+)\\s*\\.)*)(\\w+)");
            // List<String> parseInTableEntryLocations = mapParser.parseInTableEntryLocations2(expression);
            // for (String entryLocation : parseInTableEntryLocations) {
            //
            // }

            // context.schema.context.table.column
            // context.schema.table.column
            // schema.context.table.column
            // schema.table.column
            // table.column
            // add \\w*#* for oracle 12 , schema name start with C##
            MapExpressionParser mapParser1 = new MapExpressionParser("((\\s*(\\w*#*\\w+)\\s*\\.)*)([\\w\\(\\)]+)"); //$NON-NLS-1$
            // if it's nvl(Table.column , value)
            if (expression.toUpperCase().startsWith("NVL(") && expression.endsWith(")")
                    && expression.indexOf(",") != -1) {
                String withoutNVL = expression.substring(4, expression.length() - 1);
                itemNameList = mapParser1.parseInTableEntryLocations2(withoutNVL);
            } else {
                itemNameList = mapParser1.parseInTableEntryLocations2(expression);
            }

            if (itemNameList == null || itemNameList.isEmpty()) {
                MapExpressionParser mapParser2 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                itemNameList = mapParser2.parseInTableEntryLocations(expression);
            }

            String quto_markParser = "[\\\\]?\\" + quto_mark; //$NON-NLS-1$
            for (Map<String, String> itemNamemap : itemNameList) {
                Set<Entry<String, String>> set = itemNamemap.entrySet();
                Iterator<Entry<String, String>> ite = set.iterator();
                while (ite.hasNext()) {
                    Entry<String, String> entry = ite.next();
                    String columnValue = entry.getKey();
                    String tableValue = entry.getValue();
                    boolean aliasFlag = false;
                    String tableNameValue = tableValue;
                    // find original table name if tableValue is alias
                    String originaltableName = tableValue;
                    ExternalDbMapData externalData = component.getExternalData();
                    final List<ExternalDbMapTable> inputTables = externalData.getInputTables();
                    for (ExternalDbMapTable inputTable : inputTables) {
                        if (inputTable.getAlias() != null && inputTable.getAlias().equals(tableValue)) {
                            originaltableName = inputTable.getTableName();
                            tableNameValue = inputTable.getAlias();
                            aliasFlag = true;
                        }
                    }

                    List<IConnection> inputConnections = (List<IConnection>) component.getIncomingConnections();
                    if (inputConnections != null) {

                        for (IConnection iconn : inputConnections) {
                            IMetadataTable metadataTable = iconn.getMetadataTable();
                            String tName = iconn.getName();
                            if ((originaltableName.equals(tName) || tableValue.equals(tName))
                                    && metadataTable != null) {
                                List<IMetadataColumn> lColumn = metadataTable.getListColumns();
                                String tableName = metadataTable.getTableName();
                                String tableColneName = tableName;
                                tableColneName = MetadataToolHelper.validateTableName(tableColneName);
                                if (tableValue.contains(".") && tableName != null) { //$NON-NLS-1$
                                    MapExpressionParser mapParser2 =
                                            new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                                    List<Map<String, String>> tableNameList =
                                            mapParser2.parseInTableEntryLocations(tableValue);

                                    for (Map<String, String> tableNameMap : tableNameList) {
                                        Set<Entry<String, String>> setTable = tableNameMap.entrySet();
                                        Iterator<Entry<String, String>> iteTable = setTable.iterator();

                                        while (iteTable.hasNext()) {
                                            Entry<String, String> tableEntry = iteTable.next();
                                            String tableLabel = tableEntry.getKey();
                                            String schemaValue = tableEntry.getValue();
                                            if (tableLabel.equals(metadataTable.getLabel())
                                                    && tableColneName.equals(tableLabel)) {
                                                tableName = tableName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$//$NON-NLS-2$
                                                expression = expression
                                                        .replaceFirst(tableValue, schemaValue + "." + tableName); //$NON-NLS-1$
                                            }
                                        }

                                    }
                                } else if (tableName != null) {
                                    if (tableValue.equals(metadataTable.getLabel())
                                            && tableColneName.equals(tableValue)) {
                                        tableName = tableName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        expression = expression.replaceFirst(tableValue, tableName);
                                    }
                                }
                                INode source = iconn.getSource();
                                String handledTableName = "";
                                boolean inputIsELTDBMap = false;
                                String schemaValue = "";
                                String table = "";
                                boolean hasSchema = false;
                                IElementParameter schemaParam = source.getElementParameter("ELT_SCHEMA_NAME");
                                IElementParameter tableParam = source.getElementParameter("ELT_TABLE_NAME");
                                if (schemaParam != null && schemaParam.getValue() != null) {
                                    schemaValue = schemaParam.getValue().toString();
                                }
                                if (tableParam != null && tableParam.getValue() != null) {
                                    table = tableParam.getValue().toString();
                                }
                                String schemaNoQuote = TalendTextUtils.removeQuotes(schemaValue);
                                String tableNoQuote = TalendTextUtils.removeQuotes(table);
                                hasSchema = !"".equals(schemaNoQuote);
                                for (IMetadataColumn co : lColumn) {
                                    if (columnValue.equals(co.getLabel())) {
                                        String oriName = co.getOriginalDbColumnName();
                                        // if OriginalDbColumn is empty , still use label to generate sql
                                        if (oriName == null || "".equals(oriName)) { //$NON-NLS-1$
                                            continue;
                                        }
                                        if (expression.trim().equals(tableValue + "." + oriName)) {
                                            if (hasSchema && !aliasFlag) {
                                                expression = getTableName(iconn, schemaNoQuote, quote) + "."
                                                        + getTableName(iconn, tableNoQuote, quote) + "."
                                                        + getColumnName(iconn, oriName, quote);
                                                expression = expression.replaceAll(quto_markParser, "\\\\" + quto_mark); //$NON-NLS-1$
                                            } else {
                                                expression = getTableName(iconn, tableValue, quote) + "."
                                                        + getColumnName(iconn, oriName, quote);
                                                expression = expression.replaceAll(quto_markParser, "\\\\" + quto_mark); //$NON-NLS-1$
                                            }
                                            continue;
                                        }
                                        if (expression.trim().equals(originaltableName + "." + oriName)) {
                                            expression = originaltableName + "." + getColumnName(iconn, oriName, quote);
                                            expression = expression.replaceAll(quto_markParser, "\\\\" + quto_mark); //$NON-NLS-1$
                                            continue;
                                        }
                                        // if it is temp delived table, use label to generate sql
                                        if (iconn.getLineStyle() == EConnectionType.TABLE_REF) {
                                            continue;
                                        }
                                        if (!isRefTableConnection(iconn) && isAddQuotesInColumns()) {
                                            oriName = getColumnName(iconn, oriName, quote);
                                        } else {
                                            oriName = oriName.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                        String quotedTableName = getTableName(iconn, tableValue, quote);
                                        quotedTableName = adaptQuoteForTableAndColumnName(component, quotedTableName);
                                        // expression = expression.replaceAll(tableValue, quotedTableName);
                                        // expression = expression.replaceFirst(tableValue + "\\." + co.getLabel(),
                                        // //$NON-NLS-1$
                                        // quotedTableName + "\\." + oriName); //$NON-NLS-1$
                                        // change replaceFirst to manually replace the String because some special chars
                                        // may not be replaced because of the regular expression
                                        int begin = expression.indexOf(tableValue + "." + co.getLabel());
                                        int length = (tableValue + "." + co.getLabel()).length();
                                        expression = expression.substring(0, begin) + quotedTableName + "." + oriName
                                                + expression.substring(begin + length);
                                        expression = replaceAuotes(component, expression, quto_markParser, quto_mark);
                                    }
                                }

                            }
                        }
                    }
                }
            }

        }

        return expression;
    }

    @Override
    public String buildSqlSelect(INode source, String outputTableName, String tabString) {
        // TODO Auto-generated method stub
        if (source.getExternalNode() instanceof UniteMapComponent) {
            UniteMapComponent uniteMapComponent = (UniteMapComponent) source.getExternalNode();
            return buildSqlSelect(uniteMapComponent, outputTableName, tabString);
        }
        return "";
    }

    private String getUniteSql(String buildSqlSelect) {

        return "\"(\" + " + buildSqlSelect + "+ \")\"";
    }
}
