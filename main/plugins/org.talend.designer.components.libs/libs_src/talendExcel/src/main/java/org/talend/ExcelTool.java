package org.talend;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

import com.monitorjbl.xlsx.StreamingReader;

public class ExcelTool {

    private Workbook wb = null;

    private String sheetName = null;

    private Sheet sheet = null;

    private Workbook preWb = null;
    
    private Sheet preSheet = null;

    private Row curRow = null;

    private Row preRow = null;

    private Cell curCell = null;

    private Cell preCell = null;

    private boolean appendWorkbook = false;

    private boolean appendSheet = false;
    
    private boolean streamingAppend = false;

    private int startX = 0;

    private int curY = 0;

    private int xOffset = 0;

    private boolean isAbsY = false;

    private int absX = 0;

    private int absY = 0;

    private boolean keepCellFormat = false;

    private Font font = null;

    private Map<String, CellStyle> cellStylesMapping;

    private boolean recalculateFormula = false;

    private int rowAccessWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;// used in auto flush

    private boolean isTrackAllColumns = false;

    private boolean isTruncateExceedingCharacters = false;

    private static final int CELL_CHARACTERS_LIMIT = 32767;

    private String password = null;
    
    private Map<CellStyle, CellStyle> existedOriginToClone;

    public ExcelTool() {
        cellStylesMapping = new HashMap<>();
    }

    public void setAppend(boolean appendWorkbook, boolean appendSheet, boolean streamingAppend) {
        this.appendWorkbook = appendWorkbook;
        this.appendSheet = appendSheet;
        if(this.appendWorkbook) {
            this.streamingAppend = streamingAppend;
    }
    }

    public void setXY(boolean isAbsY, int absX, int absY, boolean keepFormat) {
        this.isAbsY = isAbsY;
        this.absX = absX;
        this.absY = absY;
        this.keepCellFormat = keepFormat;
    }

    public void setSheet(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setRecalculateFormula(boolean recalculateFormula) {
        this.recalculateFormula = recalculateFormula;
    }

    public void prepareStream() {
        streamingAppend = false;
        wb = new SXSSFWorkbook(null,rowAccessWindowSize,false,true);
        sheet = wb.createSheet(sheetName);
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    public void prepareXlsxFile(String fileName) throws Exception {
        File xlsxFile = new File(fileName);
        if (xlsxFile.exists()) {
            if (isAbsY && keepCellFormat) {
                initPreXlsx(fileName);
            }
            if (appendWorkbook) {
                appendActionForFile(fileName);
            } else {
                xlsxFile.delete();
                wb = new SXSSFWorkbook(null,rowAccessWindowSize,false,true);
                sheet = wb.createSheet(sheetName);
            }
        } else {
            wb = new SXSSFWorkbook(null,rowAccessWindowSize,false,true);
            sheet = wb.createSheet(sheetName);
        }
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    public void prepareXlsmFile(String fileName) throws Exception {
        File xlsmFile = new File(fileName);
        if (xlsmFile.exists()) {
            if (isAbsY && keepCellFormat) {
                initPreXlsx(fileName);
            }
            if (appendWorkbook) {
                appendActionForFile(fileName);
            } else {
                xlsmFile.delete();
                wb = new SXSSFWorkbook(new XSSFWorkbook(XSSFWorkbookType.XLSM), rowAccessWindowSize,false,true);

                sheet = wb.createSheet(sheetName);
            }
        } else {
            wb = new SXSSFWorkbook(new XSSFWorkbook(XSSFWorkbookType.XLSM), rowAccessWindowSize,false,true);
            sheet = wb.createSheet(sheetName);
        }
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    private void appendActionForFile(String fileName) throws Exception {
        if (this.streamingAppend) {
            wb = new SXSSFWorkbook(rowAccessWindowSize);

            try (InputStream fis = new FileInputStream(fileName);
                 Workbook preWorkbookForAppend =
                     StreamingReader.builder().rowCacheSize(100).bufferSize(4096)
                         .password(password).open(fis)) {

                Iterator<Sheet> sheets = preWorkbookForAppend.sheetIterator();
                while (sheets.hasNext()) {
                    Sheet preSheet = sheets.next();
                    Sheet targetSheet = null;
                    if (sheetName.equals(preSheet.getSheetName())) {
                        sheet = wb.createSheet(sheetName);

                        //need to skip the same name sheet if not append sheet
                        if(!appendSheet) {
                            continue;
                        }

                        targetSheet = sheet;
                    } else {
                        targetSheet = wb.createSheet(preSheet.getSheetName());
                    }

                    int y = 0;

                    Iterator<Row> rows = preSheet.rowIterator();
                    while (rows.hasNext()) {
                        curRow = targetSheet.createRow(y);
                        y++;
                        if(targetSheet == sheet) {
                            curY = y;// store the append point
                        }
                        xOffset = 0;

                        Row row = rows.next();
                        Iterator<Cell> cells = row.cellIterator();
                        while (cells.hasNext()) {
                            Cell cell = cells.next();
                            CellType cellType = cell.getCellType();
                            CellStyle cellStyle = getPreCellStyleForStreamingAppend(cell);

                            curCell = curRow.createCell(startX + xOffset, cellType);
                            xOffset++;

                            switch (cellType) {
                                case BLANK:
                                    if (cellStyle != null) curCell.setCellStyle(cellStyle);
                                    curCell.setBlank();
                                    break;
                                case BOOLEAN:
                                    curCell.setCellValue(cell.getBooleanCellValue());
                                    if (cellStyle != null) curCell.setCellStyle(cellStyle);
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        curCell.setCellValue(cell.getDateCellValue());
                                    } else {
                                        curCell.setCellValue(cell.getNumericCellValue());
                                    }
                                    if (cellStyle != null) curCell.setCellStyle(cellStyle);
                                    break;
                                case STRING:
                                    curCell.setCellValue(cell.getStringCellValue());
                                    if (cellStyle != null) curCell.setCellStyle(cellStyle);
                                    break;
                                case FORMULA:
                                    curCell.setCellValue(cell.getCellFormula());
                                    if (cellStyle != null) curCell.setCellStyle(cellStyle);
                                    break;
                                case _NONE:
                                    break;
                                case ERROR:
                                    break;// now only ignore error cell as streaming read api don't support to read that
                                //throw new RuntimeException("Unsupported Cell Type for streaming append: " + cellType);
                                default:
                                    throw new RuntimeException("Unknown Cell Type: " + cellType);
                            }
                        }
                    }
                }

                if (sheet == null) {
                    sheet = wb.createSheet(sheetName);
                }
            }
            return;
        }

        // if use file instread of streaming, will throw an exception, TODO check why
        try (InputStream inp = new FileInputStream(fileName)) {
        wb = WorkbookFactory.create(inp, password);
        sheet = wb.getSheet(sheetName);
        if (sheet != null) {
            if (appendSheet) {
                if (sheet.getLastRowNum() != 0 || sheet.getRow(0) != null) {
                    curY = sheet.getLastRowNum() + 1;
                }
            } else {
                wb.removeSheetAt(wb.getSheetIndex(sheetName));
                sheet = wb.createSheet(sheetName);
            }
        } else {
            sheet = wb.createSheet(sheetName);
        }
    }
    }

    /**
     *
     * @return start insert row index.
     */
    public int getStartRow() {
        return curY;
    }

    private void initPreXlsx(String fileName) throws Exception {
        // if use file instread of streaming, will throw an exception, TODO check why
        try (InputStream preIns = new FileInputStream(fileName)) {
        preWb = WorkbookFactory.create(preIns, password);
        preSheet = preWb.getSheet(sheetName);
    }
    }

    public void setFont(String fontName) {
        if (StringUtils.isNotEmpty(fontName)) {
            if (!streamingAppend) {
                //TODO this will make duplicted font and stored in excel file, not right for some case, fix it
            font = wb.createFont();
            font.setFontName(fontName);
            } else {
                XSSFFont newFont = new XSSFFont();
                newFont.setFontName(fontName);
                Font existedFont = wb.findFont(newFont.getBold(), newFont.getColor(), newFont.getFontHeight(), newFont.getFontName(), newFont.getItalic(), newFont.getStrikeout(), newFont.getTypeOffset(), newFont.getUnderline());
                if(existedFont!=null) {
                    font = existedFont;
                } else {
                    font = wb.createFont();
                    font.setFontName(fontName);
        }
    }
        }
    }

    public void addRow() {
        if (isAbsY && keepCellFormat) {
            preRow = (preSheet != null) ? preSheet.getRow(curY) : null;
        }
        curRow = sheet.getRow(curY);
        if (curRow == null) {
            curRow = sheet.createRow(curY);
        }
        if (keepCellFormat) {
            short rowHeight = curRow.getHeight();
            if (rowHeight != -1) {
                curRow.setHeight(rowHeight);
            }
        }
        curY = curY + 1;
        xOffset = 0;
    }

    private void addCell() {
        if (isAbsY && keepCellFormat) {
            preCell = (preRow != null) ? preRow.getCell(startX + xOffset) : null;
        }
        curCell = curRow.createCell(startX + xOffset);
        xOffset++;
    }

    private CellStyle getNormalCellStyle() {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get("normal") != null) {
                return cellStylesMapping.get("normal");
            } else {
                Font searchFont = null;
                if (font == null) {
                    searchFont = new XSSFFont();
                } else {
                    searchFont = font;
                }

                CellStyle style = searchCellStyleFromPre(searchFont, (short) 0);

                if (style == null) {
                    style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                }

                cellStylesMapping.put("normal", style);
                return style;
            }
        }
        return preCellStyle;
    }

    private CellStyle searchCellStyleFromPre(Font searchFont, short dataFormat) {
        if (streamingAppend && existedOriginToClone != null && existedOriginToClone.size() < 1000) {
            //fetch cell style by font name and data pattern for date
            for (Map.Entry<CellStyle, CellStyle> cc : existedOriginToClone.entrySet()) {
                CellStyle cellStyle = cc.getKey();

                if ((dataFormat == cellStyle.getDataFormat()) && (sameFont(searchFont, wb.getFontAt(cellStyle.getFontIndexAsInt())))) {
                    return cc.getValue();
                }
            }
        }
        return null;
    }

    private boolean sameFont(Font f1, Font f2) {
        return f1.getBold() == f2.getBold()
                && f1.getColor() == f2.getColor()
                && f1.getFontHeight() == f2.getFontHeight()
                && f1.getFontName().equalsIgnoreCase(f2.getFontName())
                && f1.getItalic() == f2.getItalic()
                && f1.getStrikeout() == f2.getStrikeout()
                && f1.getTypeOffset() == f2.getTypeOffset()
                && f1.getUnderline() == f2.getUnderline();
    }

    private CellStyle getDateCellStyle(String pattern) {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get(pattern) != null) {
                return cellStylesMapping.get(pattern);
            } else {
                short dataFormat = 0; //default data format id
                if (StringUtils.isNotEmpty(pattern)) {
                    dataFormat = wb.getCreationHelper().createDataFormat().getFormat(pattern);
                }

                Font searchFont = null;
                if (font == null) {
                    searchFont = new XSSFFont();
                } else {
                    searchFont = font;
                }
                CellStyle style = searchCellStyleFromPre(searchFont, dataFormat);

                if (style == null) {
                    style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                if (StringUtils.isNotEmpty(pattern)) {
                        style.setDataFormat(dataFormat);
                }
                }

                cellStylesMapping.put(pattern, style);
                return style;
            }
        }
        return preCellStyle;
    }

    private CellStyle getPreCellStyleForStreamingAppend(Cell preCell) {
        if (existedOriginToClone == null) {
            existedOriginToClone = new HashMap<>();
        }
        CellStyle preCellStyle = preCell.getCellStyle();
        if (preCellStyle == null) return null;

        CellStyle targetCellStyle = existedOriginToClone.get(preCellStyle);
        if (targetCellStyle == null) {
            targetCellStyle = wb.createCellStyle();
            targetCellStyle.cloneStyleFrom(preCellStyle);
            existedOriginToClone.put(preCellStyle, targetCellStyle);
        }

        return targetCellStyle;
    }

    private CellStyle getPreCellStyle() {
        if (preSheet != null && isAbsY && keepCellFormat) {
            if (existedOriginToClone == null) {
                existedOriginToClone = new HashMap<>();
            }
            CellStyle preCellStyle;
            if (preCell == null) {
                preCellStyle = preSheet.getColumnStyle(curCell.getColumnIndex());
            } else {
                preCellStyle = preCell.getCellStyle();
            }

            CellStyle targetCellStyle = existedOriginToClone.get(preCellStyle);
            if(targetCellStyle==null) {
                targetCellStyle = wb.createCellStyle();
                targetCellStyle.cloneStyleFrom(preCellStyle);
                existedOriginToClone.put(preCellStyle, targetCellStyle);
            }

            return targetCellStyle;

        }
        return null;
    }

    public void addCellValue(boolean booleanValue) {
        addCell();
        curCell.setCellValue(booleanValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(Date dateValue, String pattern) {
        addCell();
        curCell.setCellValue(dateValue);
        curCell.setCellStyle(getDateCellStyle(pattern));
    }

    public void addCellValue(double doubleValue) {
        addCell();
        curCell.setCellValue(doubleValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(String stringValue) {
        addCell();
        String value = isTruncateExceedingCharacters && stringValue != null && stringValue.length() > CELL_CHARACTERS_LIMIT
                ? stringValue.substring(0, CELL_CHARACTERS_LIMIT)
                        : stringValue;
        curCell.setCellValue(value);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellNullValue() {
        addCell();
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void setColAutoSize(int colNum) {
        if (!isTrackAllColumns) {
            trackAllColumnsForAutoSizing();
        }
        sheet.autoSizeColumn(startX + colNum, true);
    }

    public void trackAllColumnsForAutoSizing() {
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        isTrackAllColumns = true;
    }

    public void setRowAccessWindowSize(int rowAccessWindowSize) {
        this.rowAccessWindowSize = rowAccessWindowSize;
    }

    public void writeExcel(OutputStream outputStream) throws Exception {
        try {
            wb.write(outputStream);
        } finally {
            try {
            wb.close();
            } finally {
                try {
                    if (preWb != null) {
                preWb.close();
            }
        } finally {
                    // close at the beginning to avoid additional try-finally block
                    if (existedOriginToClone != null) {
                existedOriginToClone = null;
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
        }
    }

    public void writeExcel(String fileName, boolean createDir) throws Exception {
        if (createDir) {
            File file = new File(fileName);
            File pFile = file.getParentFile();
            if (pFile != null && !pFile.exists()) {
                pFile.mkdirs();
            }
        }
        if (appendWorkbook && appendSheet && !streamingAppend && recalculateFormula) {
            evaluateFormulaCell();
        }
        try (FileOutputStream fileOutput = new FileOutputStream(fileName); 
             POIFSFileSystem fs = new POIFSFileSystem()) {
            if (password == null) {
                wb.write(fileOutput);
            } else {
                Encryptor encryptor = new EncryptionInfo(EncryptionMode.agile).getEncryptor();
                encryptor.confirmPassword(password);
                OutputStream encryptedDataStream = encryptor.getDataStream(fs);
                wb.write(encryptedDataStream);
                encryptedDataStream.close(); // this is mandatory to do that at that point
                fs.writeFilesystem(fileOutput);
            }
        } finally {
            try {
                wb.close();
            } finally {
                if (existedOriginToClone != null) {
                existedOriginToClone = null;
            }
                if (preWb != null) {
                preWb.close();
            }
        }
    }
    }

    private void evaluateFormulaCell() {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            sheet = wb.getSheetAt(sheetNum);
            for (Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellType() == CellType.FORMULA) {
                        evaluator.evaluateFormulaCell(c);
                    }
                }
            }
        }
    }

    public void flushRowInMemory() throws Exception {
        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFSheet) sheet).flushRows();
        }
    }

    public void setPasswordProtection(String password) {
        this.password = password;
    }

    public void setTruncateExceedingCharacters(boolean isTruncateExceedingCharacters) {
        this.isTruncateExceedingCharacters = isTruncateExceedingCharacters;
    }

    public static void main(String[] args) throws Exception {
        org.talend.ExcelTool tool = new org.talend.ExcelTool();
        tool.setTruncateExceedingCharacters(false);
        tool.setSheet("Sheet3");
        tool.setAppend(true, true, true);
        tool.setRecalculateFormula(false);
        tool.setXY(false, 0, 0, true);

        String file = "/Users/wangwei/Downloads/streamappend.xlsx";
        tool.prepareXlsxFile(file);

        tool.setFont("COURIER");

        tool.addRow();

        tool.addCellValue(String.valueOf("王伟"));
        tool.addCellValue(new Date(), "yyyy-MM-dd");

        tool.setColAutoSize(0);
        tool.setColAutoSize(1);

        tool.writeExcel(file, false);
    }

}
