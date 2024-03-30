package org.eustrosoft.keywords.replacers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReplacersManager {
    private final Map<String, KeywordsReplacer> keywordsReplacerBySupportedFormat;

    public ReplacersManager(List<KeywordsReplacer> keywordsReplacer) {
        keywordsReplacerBySupportedFormat = keywordsReplacer.stream().collect(
                Collectors.toMap(KeywordsReplacer::getSupportedFormats, Function.identity())
        );
    }

    public KeywordsReplacer getReplacerByFileExtension(String fileExtension) {
        return keywordsReplacerBySupportedFormat.entrySet().stream()
                .filter(entry -> Arrays.asList(entry.getKey().split(",")).contains(fileExtension))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> {
                    throw new NoSuchElementException(String.format("%s file extension is not supported", fileExtension));
                });
    }
}
