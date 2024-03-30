package org.eustrosoft.keywords.config;

import java.util.List;

public class KeywordsReplaceConfig {
    private String filename;
    private String templatePath;
    private List<KeywordsInfo> keywords;

    public KeywordsReplaceConfig() {
    }

    public KeywordsReplaceConfig(String filename, String templatePath, List<KeywordsInfo> keywords) {
        this.filename = filename;
        this.templatePath = templatePath;
        this.keywords = keywords;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public List<KeywordsInfo> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeywordsInfo> keywords) {
        this.keywords = keywords;
    }
}
