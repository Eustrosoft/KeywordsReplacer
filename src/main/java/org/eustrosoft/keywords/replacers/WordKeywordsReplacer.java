package org.eustrosoft.keywords.replacers;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.eustrosoft.keywords.config.KeywordsConfigUtil;
import org.eustrosoft.keywords.config.KeywordsReplaceConfig;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.eustrosoft.keywords.config.KeywordType.TABLE_ROW;
import static org.eustrosoft.keywords.config.KeywordsConfigUtil.validateKeywordsObjectsCountInSameRow;

public class WordKeywordsReplacer implements KeywordsReplacer {
    public static final String SUPPORTED_EXTENSIONS = "doc,docx";

    @Override
    public String getSupportedFormats() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public void replaceKeywordsInFile(KeywordsReplaceConfig keywordsReplaceConfig, OutputStream out) throws Exception {
        try (InputStream is = new FileInputStream(keywordsReplaceConfig.getTemplatePath())) {
            try (XWPFDocument document = new XWPFDocument(is)) {
                findAndReplaceInHeaders(document, keywordsReplaceConfig);
                findAndReplaceInParagraphs(document, keywordsReplaceConfig);
                findAndReplaceInTables(document, keywordsReplaceConfig);
                findAndReplaceInFooters(document, keywordsReplaceConfig);
                document.write(out);
            }
        }
    }

    private void findAndReplaceInTables(XWPFDocument document, KeywordsReplaceConfig keywordsReplaceConfig) {
        document.getTables().forEach(t -> findAndReplaceInTable(t, keywordsReplaceConfig));
    }

    private void findAndReplaceInTable(XWPFTable table, KeywordsReplaceConfig keywordsReplaceConfig) {
        Map<Integer, String> rowIndexesOfRowTypeKeywords = new LinkedHashMap<>();

        List<XWPFTableRow> rows = table.getRows();
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    String text = paragraph.getText();
                    while (text.contains("${")) {
                        int keywordStartIndex = text.indexOf("${");
                        String substring = text.substring(keywordStartIndex + 2);
                        int keywordEndIndex = substring.indexOf("}");
                        String key = substring.substring(0, keywordEndIndex);
                        if (TABLE_ROW.getValue().equalsIgnoreCase(KeywordsConfigUtil.getKeyInfo(keywordsReplaceConfig, key).getType())) {
                            rowIndexesOfRowTypeKeywords.merge(
                                    rows.indexOf(row),
                                    key,
                                    (oldVal, newVal) -> oldVal + "," + newVal);
                            break;
                        }
                        text = text.replace("${" + key + "}", KeywordsConfigUtil.getJoinedReplaceValuesByKey(keywordsReplaceConfig, key));
                        paragraphSetText(paragraph, text);
                    }
                }
            }
        }

        if (!rowIndexesOfRowTypeKeywords.isEmpty()) {
            int indexShift = 0;
            for (Map.Entry<Integer, String> rowToCopy : rowIndexesOfRowTypeKeywords.entrySet()) {
                int currentRowIndex = rowToCopy.getKey() + indexShift;
                XWPFTableRow row = table.getRow(currentRowIndex);
                XWPFTableRow copiedRow = new XWPFTableRow((CTRow) row.getCtRow().copy(), table);
                Map<String, List<String>> keyToObjects = new HashMap<>();
                for (String key : rowToCopy.getValue().split(",")) {
                    keyToObjects.put(
                            key,
                            KeywordsConfigUtil.getReplaceValuesByKey(keywordsReplaceConfig, key)
                    );
                }
                int objectsCount = validateKeywordsObjectsCountInSameRow(keyToObjects);
                for (int i = 0; i < objectsCount; i++) {
                    XWPFTableRow rowWithKeys = new XWPFTableRow((CTRow) copiedRow.getCtRow().copy(), table);
                    findAndReplaceKeysInTableRow(rowWithKeys, keyToObjects, i);
                    table.addRow(rowWithKeys, ++currentRowIndex);
                }
                table.removeRow(rowToCopy.getKey() + indexShift);
                indexShift += objectsCount - 1;
            }
        }
    }

    private void findAndReplaceInParagraphs(XWPFDocument document, KeywordsReplaceConfig keywordsReplaceConfig) {
        document.getParagraphs().forEach(paragraph -> findAndReplaceKeysInParagraph(paragraph, keywordsReplaceConfig));
    }

    private void findAndReplaceInHeaders(XWPFDocument document, KeywordsReplaceConfig keywordsReplaceConfig) {
        document.getHeaderList().forEach(header -> {
                    header.getParagraphs().forEach(paragraph -> findAndReplaceKeysInParagraph(paragraph,
                            keywordsReplaceConfig));
                    header.getTables().forEach(t -> findAndReplaceInTable(t, keywordsReplaceConfig));
                }
        );
    }

    private void findAndReplaceInFooters(XWPFDocument document, KeywordsReplaceConfig keywordsReplaceConfig) {
        document.getFooterList().forEach(footer -> {
                    footer.getParagraphs().forEach(paragraph -> findAndReplaceKeysInParagraph(paragraph,
                            keywordsReplaceConfig));
                    footer.getTables().forEach(t -> findAndReplaceInTable(t, keywordsReplaceConfig));
                }
        );
    }

    private void findAndReplaceKeysInParagraph(XWPFParagraph paragraph, KeywordsReplaceConfig
            keywordsReplaceConfig) {
        String replacedText = findAndReplaceKeysInText(paragraph.getText(), keywordsReplaceConfig);
        if (replacedText != null) {
            paragraphSetText(paragraph, replacedText);
        }
    }

    private void findAndReplaceKeysInTableRow(XWPFTableRow row,
                                              Map<String, List<String>> keyToObjects,
                                              int iteration) {
        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                String replacedText = findAndReplaceKeysInText(paragraph.getText(), keyToObjects, iteration);
                if (replacedText != null) {
                    paragraphSetText(paragraph, replacedText);
                }
            }
        }
    }

    private void paragraphSetText(XWPFParagraph paragraph, String text) {
        for (int i = paragraph.getRuns().size() - 1; i > 0; i--) {
            paragraph.removeRun(i);
        }
        paragraph.getRuns().get(0).setText(text, 0);
    }
}
