package org.eustrosoft.keywords.replacers;

import org.eustrosoft.keywords.config.KeywordsConfigUtil;
import org.eustrosoft.keywords.config.KeywordsReplaceConfig;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface KeywordsReplacer {

    void replaceKeywordsInFile(KeywordsReplaceConfig replaceConfig, OutputStream stream) throws Exception;

    String getSupportedFormats();

    default String findAndReplaceKeysInText(String text, KeywordsReplaceConfig keywordsReplaceConfig) {
        boolean textReplaced = false;
        while (text.contains("${")) {
            int keywordStartIndex = text.indexOf("${");
            String substring = text.substring(keywordStartIndex + 2);
            int keywordEndIndex = substring.indexOf("}");
            String key = substring.substring(0, keywordEndIndex);
            text = text.replace("${" + key + "}", KeywordsConfigUtil.getJoinedReplaceValuesByKey(keywordsReplaceConfig, key));
            textReplaced = true;
        }
        if (textReplaced) {
            return text;
        } else {
            return null;
        }
    }

    default String findAndReplaceKeysInText(String text,
                                            Map<String, List<String>> keyToObjects,
                                            int iteration) {
        boolean textReplaced = false;
        while (text.contains("${")) {
            int keywordStartIndex = text.indexOf("${");
            String substring = text.substring(keywordStartIndex + 2);
            int keywordEndIndex = substring.indexOf("}");
            String key = substring.substring(0, keywordEndIndex);
            text = text.replace("${" + key + "}", keyToObjects.get(key).get(iteration));
            textReplaced = true;
        }
        if (textReplaced) {
            return text;
        } else {
            return null;
        }
    }
}
