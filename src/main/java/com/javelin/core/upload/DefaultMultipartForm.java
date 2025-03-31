package com.javelin.core.upload;

import java.util.*;

/**
 * Default implementation of MultipartForm backed by in-memory maps.
 */
public class DefaultMultipartForm implements MultipartForm {
    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, List<UploadedFile>> files = new HashMap<>();

    public void addField(String name, String value) {
        fields.put(name, value);
    }

    public void addFile(String name, UploadedFile file) {
        files.computeIfAbsent(name, k -> new ArrayList<>()).add(file);
    }

    @Override
    public String getField(String name) {
        return fields.get(name);
    }

    @Override
    public UploadedFile getFile(String name) {
        List<UploadedFile> list = files.get(name);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public List<UploadedFile> getFiles(String name) {
        return files.getOrDefault(name, List.of());
    }
}
