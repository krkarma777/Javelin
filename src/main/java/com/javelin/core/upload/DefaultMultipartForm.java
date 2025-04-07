package com.javelin.core.upload;

import java.util.*;

/**
 * Default implementation of the {@link MultipartForm} interface using in-memory maps.
 * <p>
 * This class stores parsed multipart form fields and file uploads,
 * allowing handlers to easily retrieve submitted data.
 * <p>
 * Fields are stored as {@code String -> String}, while uploaded files are stored as
 * {@code String -> List<UploadedFile>} to support multiple files with the same field name.
 */
public class DefaultMultipartForm implements MultipartForm {

    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, List<UploadedFile>> files = new HashMap<>();

    /**
     * Adds a regular form field to the multipart form.
     *
     * @param name  the field name
     * @param value the field value
     */
    public void addField(String name, String value) {
        fields.put(name, value);
    }

    /**
     * Adds an uploaded file to the multipart form.
     * Supports multiple files with the same field name.
     *
     * @param name the field name (input name in form)
     * @param file the uploaded file object
     */
    public void addFile(String name, UploadedFile file) {
        files.computeIfAbsent(name, k -> new ArrayList<>()).add(file);
    }

    /**
     * Retrieves the value of a form field by name.
     *
     * @param name the name of the field
     * @return the field value, or {@code null} if not found
     */
    @Override
    public String getField(String name) {
        return fields.get(name);
    }

    /**
     * Retrieves the first uploaded file for the given field name.
     *
     * @param name the field name
     * @return the first uploaded file, or {@code null} if none exist
     */
    @Override
    public UploadedFile getFile(String name) {
        List<UploadedFile> list = files.get(name);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * Retrieves all uploaded files for the given field name.
     *
     * @param name the field name
     * @return a list of uploaded files (empty if none exist)
     */
    @Override
    public List<UploadedFile> getFiles(String name) {
        return files.getOrDefault(name, List.of());
    }
}
