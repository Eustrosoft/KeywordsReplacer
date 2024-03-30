package org.eustrosoft.keywords.config;

import java.util.List;
import java.util.Map;

public class KeywordsInfo {
    private String key;
    private List<Map<String, Object>> objects;
    private String delimiter;
    private String type;

    public KeywordsInfo() {
    }

    public KeywordsInfo(String key, List<Map<String, Object>> objects, String delimiter, String type) {
        this.key = key;
        this.objects = objects;
        this.delimiter = delimiter;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Map<String, Object>> getObjects() {
        return objects;
    }

    public void setObjects(List<Map<String, Object>> objects) {
        this.objects = objects;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
