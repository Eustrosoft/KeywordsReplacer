package org.eustrosoft.keywords.replacers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eustrosoft.keywords.config.KeywordsConfigUtil;
import org.eustrosoft.keywords.config.KeywordsReplaceConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.eustrosoft.keywords.config.KeywordType.TABLE_ROW;
import static org.eustrosoft.keywords.config.KeywordsConfigUtil.validateKeywordsObjectsCountInSameRow;

public class ExcelKeywordsReplacer implements KeywordsReplacer {
    public static final String SUPPORTED_EXTENSIONS = "xls,xlsx";

    @Override
    public String getSupportedFormats() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public void replaceKeywordsInFile(KeywordsReplaceConfig keywordsReplaceConfig, OutputStream out) throws Exception {
        try (InputStream is = new FileInputStream(keywordsReplaceConfig.getTemplatePath())) {
            try (XSSFWorkbook workbook = new XSSFWorkbook(is)) {
                XSSFSheet sheet = workbook.getSheetAt(0);
                findAndReplaceInHeader(sheet.getHeader(), keywordsReplaceConfig);
                findAndReplaceInFooter(sheet.getFooter(), keywordsReplaceConfig);
                findAndReplaceInRows(sheet, keywordsReplaceConfig);
                workbook.write(out);
            }
        }
    }

    private void findAndReplaceInHeader(Header header, KeywordsReplaceConfig keywordsReplaceConfig) {
        findAndReplaceInHeaderFooter(header, keywordsReplaceConfig);
    }

    private void findAndReplaceInFooter(Footer footer, KeywordsReplaceConfig keywordsReplaceConfig) {
        findAndReplaceInHeaderFooter(footer, keywordsReplaceConfig);
    }

    private void findAndReplaceInHeaderFooter(HeaderFooter headerFooter, KeywordsReplaceConfig keywordsReplaceConfig) {
        String replacedText = findAndReplaceKeysInText(headerFooter.getLeft(), keywordsReplaceConfig);
        if (replacedText != null) {
            headerFooter.setLeft(replacedText);
        }

        replacedText = findAndReplaceKeysInText(headerFooter.getCenter(), keywordsReplaceConfig);
        if (replacedText != null) {
            headerFooter.setCenter(replacedText);
        }

        replacedText = findAndReplaceKeysInText(headerFooter.getRight(), keywordsReplaceConfig);
        if (replacedText != null) {
            headerFooter.setRight(replacedText);
        }
    }

    private void findAndReplaceInRows(XSSFSheet sheet, KeywordsReplaceConfig keywordsReplaceConfig) {
        Map<Integer, String> rowIndexesOfRowTypeKeywords = new LinkedHashMap<>();
        for (Row row : sheet) {
            for (Cell cell : row) {
                String text = cell.getStringCellValue();
                while (cell.getStringCellValue().contains("${")) {
                    int keywordStartIndex = text.indexOf("${");
                    String substring = text.substring(keywordStartIndex + 2);
                    int keywordEndIndex = substring.indexOf("}");
                    String key = substring.substring(0, keywordEndIndex);
                    if (TABLE_ROW.getValue().equalsIgnoreCase(KeywordsConfigUtil.getKeyInfo(keywordsReplaceConfig, key).getType())) {
                        rowIndexesOfRowTypeKeywords.merge(
                                row.getRowNum(),
                                key,
                                (oldVal, newVal) -> oldVal + "," + newVal);
                        break;
                    }
                    text = text.replace("${" + key + "}", KeywordsConfigUtil.getJoinedReplaceValuesByKey(keywordsReplaceConfig, key));
                    cell.setCellValue(text);
                }
            }
        }
        int indexShift = 0;
        if (!rowIndexesOfRowTypeKeywords.isEmpty()) {
            for (Map.Entry<Integer, String> rowToCopy : rowIndexesOfRowTypeKeywords.entrySet()) {
                int currentRowIndex = rowToCopy.getKey() + indexShift;
                Map<String, List<String>> keyToObjects = new HashMap<>();
                for (String key : rowToCopy.getValue().split(",")) {
                    keyToObjects.put(
                            key,
                            KeywordsConfigUtil.getReplaceValuesByKey(keywordsReplaceConfig, key)
                    );
                }
                int objectsCount = validateKeywordsObjectsCountInSameRow(keyToObjects);

                if (currentRowIndex < sheet.getLastRowNum()) {
                    sheet.shiftRows(currentRowIndex + 1, sheet.getLastRowNum(), objectsCount - 1);
                } else {
                    sheet.createRow(currentRowIndex + 1);
                }

                for (int i = 0; i < objectsCount; i++) {
                    if (i == objectsCount - 1) {
                        findAndReplaceKeysInRow(sheet.getRow(currentRowIndex), keyToObjects, i);
                    } else {
                        int sourceRowIndex = currentRowIndex;
                        int copiedRowIndex = ++currentRowIndex;
                        sheet.copyRows(sourceRowIndex, sourceRowIndex, copiedRowIndex, new CellCopyPolicy());
                        findAndReplaceKeysInRow(sheet.getRow(sourceRowIndex), keyToObjects, i);
                    }
                }

                indexShift += objectsCount - 1;
            }
        }
    }

    private void findAndReplaceKeysInRow(Row row,
                                         Map<String, List<String>> keyToObjects,
                                         int iteration) {
        for (Cell cell : row) {
            String replacedText = findAndReplaceKeysInText(cell.getStringCellValue(), keyToObjects, iteration);
            if (replacedText != null) {
                cell.setCellValue(replacedText);
            }
        }
    }
}
