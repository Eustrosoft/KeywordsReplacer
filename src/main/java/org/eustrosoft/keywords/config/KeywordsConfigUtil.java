package org.eustrosoft.keywords.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.eustrosoft.keywords.config.KeywordType.CHECKBOX;
import static org.eustrosoft.keywords.config.KeywordType.COMMON;
import static org.eustrosoft.keywords.config.KeywordType.TABLE_ROW;

public class KeywordsConfigUtil {

    public static KeywordsInfo getKeyInfo(KeywordsReplaceConfig config, String key) {
        String finalKey = key.split("\\.")[0];
        return config.getKeywords()
                .stream().filter(info -> info.getKey().equalsIgnoreCase(finalKey))
                .findFirst()
                .orElseThrow(() -> {
                    throw new NoSuchElementException(String.format("Keyword %s not found in config", key));
                });
    }

    public static String getJoinedReplaceValuesByKey(KeywordsReplaceConfig keywordsReplaceConfig, String key) {
        KeywordsInfo keyInfo = getKeyInfo(keywordsReplaceConfig, key);
        List<String> keyParts = new ArrayList<>(Arrays.asList(key.split("\\.")));
        keyParts.remove(0);
        if (keyParts.isEmpty()) {
            return String.join(keyInfo.getDelimiter(), getObjectsReplaceValues(keyInfo));
        } else {
            return String.join(keyInfo.getDelimiter(), getObjectsReplaceValuesByFields(keyInfo, keyParts));
        }
    }

    public static List<String> getReplaceValuesByKey(KeywordsReplaceConfig keywordsReplaceConfig, String key) {
        KeywordsInfo keyInfo = getKeyInfo(keywordsReplaceConfig, key);
        List<String> keyParts = new ArrayList<>(Arrays.asList(key.split("\\.")));
        keyParts.remove(0);
        if (keyParts.isEmpty()) {
            return getObjectsReplaceValues(keyInfo);
        } else {
            return getObjectsReplaceValuesByFields(keyInfo, keyParts);
        }
    }

    private static List<String> getObjectsReplaceValues(KeywordsInfo keywordInfo) {
        List<String> values = new ArrayList<>();
        if (COMMON.getValue().equalsIgnoreCase(keywordInfo.getType()) || TABLE_ROW.getValue().equalsIgnoreCase(keywordInfo.getType())) {
            values.addAll(getAllObjectsAllInfo(keywordInfo.getObjects()));
        }
        if (CHECKBOX.getValue().equalsIgnoreCase(keywordInfo.getType())) {
            values.addAll(
                    getAllObjectsAllInfo(keywordInfo.getObjects()).stream().map(v -> {
                        if (Boolean.parseBoolean(v)) {
                            return "\u2612";
                        } else {
                            return "\u2610";
                        }
                    })
                            .collect(Collectors.toList())
            );
        }
        return values;
    }

    private static List<String> getObjectsReplaceValuesByFields(KeywordsInfo keywordInfo,
                                                                List<String> keyParts) {
        List<String> values = new ArrayList<>();
        if (COMMON.getValue().equalsIgnoreCase(keywordInfo.getType()) || TABLE_ROW.getValue().equalsIgnoreCase(keywordInfo.getType())) {
            values.addAll(getAllObjectsFieldsInfo(keywordInfo.getObjects(), keyParts));
        }
        if (CHECKBOX.getValue().equalsIgnoreCase(keywordInfo.getType())) {
            values.addAll(
                    getAllObjectsFieldsInfo(keywordInfo.getObjects(), keyParts).stream().map(v -> {
                        if (Boolean.parseBoolean(v)) {
                            return "\u2612";
                        } else {
                            return "\u2610";
                        }
                    })
                            .collect(Collectors.toList())
            );
        }
        return values;
    }

    private static List<String> getAllObjectsFieldsInfo(List<Map<String, Object>> objects, List<String> fields) {
        return objects.stream().map(obj -> getObjectFieldsInfo(obj, fields)).collect(Collectors.toList());
    }

    private static List<String> getAllObjectsAllInfo(List<Map<String, Object>> objects) {
        return objects.stream().map(KeywordsConfigUtil::getObjectAllInfo).collect(Collectors.toList());
    }

    private static String getObjectFieldsInfo(Map<String, Object> objectInfo, List<String> fields) {
        return objectInfo.entrySet().stream().filter(entry -> fields.contains(entry.getKey())).map(entry -> String.valueOf(entry.getValue())).collect(Collectors.joining(
                " "));
    }

    private static String getObjectAllInfo(Map<String, Object> objectInfo) {
        return objectInfo.values().stream().map(String::valueOf).collect(Collectors.joining(" "));
    }

    public static int validateKeywordsObjectsCountInSameRow(Map<String, List<String>> keyToObjects) {
        final int[] count = {0};
        keyToObjects.values().forEach(
                objects -> {
                    if (count[0] != 0 && count[0] != objects.size()) {
                        throw new RuntimeException(
                                String.format("Different objects count of keywords (%s) in the same row",
                                        String.join(",", keyToObjects.keySet()))
                        );
                    } else {
                        count[0] = objects.size();
                    }
                }
        );
        return count[0];
    }
}
