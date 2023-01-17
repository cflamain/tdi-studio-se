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
package com.talend.excel.xssf.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFComment;

/**
 * created by wwang on 2012-9-27 Detailled comment
 *
 */
public class DefaultTalendSheetContentsHandler implements TalendXSSFSheetXMLHandler.TalendSheetContentsHandler {

    private DataBufferCache cache = null;

    private List<String> row = null;

    private boolean stop = false;

    private int currentColumnIndex = -1;

    private int lastColumnIndex = -1;

    public DefaultTalendSheetContentsHandler(DataBufferCache cache) {
        this.cache = cache;
    }

    @Override
    public void startRow(int rowNum) {
        row = new ArrayList<String>();
    }

    @Override
    public void endRow(int rowNum) {
        cache.writeData(row);
        row = null;
        // when each row end ,reset lastColumnIndex
        lastColumnIndex = -1;
        if (stop) {
            throw new EnoughDataException("Get enough data,now stop the xml parse action");
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue,XSSFComment comment) {
        checkHasNullValue(cellReference);
        row.add(formattedValue);
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
    }

    @Override
    public void endSheet() {

    }

    public void stop() {
        this.stop = true;
    }

    /**
     * when currentCellReferecnce is "A1" ,and next currentCellReferecnce is "C1",we need add a null value for "B1"
     */
    private void checkHasNullValue(String cellReference) {
        /*
        Avoid the NPE for old format.
        When the row is like that:
        <row><c t="inlineStr"><is><t>Cell text</t></is></c></row>
        the cellReference (comes from XSSFSheetXMLHandler::startElement) is null
        because it's taken from the <c> attribute 'r', for new format the same row is looks like this
        <row r="1" collapsed="false" outlineLevel="0" customHeight="false" hidden="false" ht="12.8" customFormat="false">
            <c r="A1" t="s" s="0"><v>0</v></c>
        </row>
        and the cellReference is "A1"
        */
        if (cellReference == null) return;
        currentColumnIndex = ColumnUtil.calculateIndexOfColumn(cellReference);
        // Might be the empty string.
        for (int i = lastColumnIndex; i < currentColumnIndex - 1; i++) {
            row.add(null);
        }
        // Update column
        if (currentColumnIndex > -1) {
            lastColumnIndex = currentColumnIndex;
        }
    }

}
