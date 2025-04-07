package com.javelin.core.upload;

import java.util.List;

/**
 * Represents a parsed {@code multipart/form-data} HTTP request.
 * <p>
 * This interface provides methods to access both regular form fields and uploaded files
 * submitted in a multipart request (typically from an HTML form with {@code enctype="multipart/form-data"}).
 */
public interface MultipartForm {

    /**
     * Retrieves the value of a regular form field by name.
     *
     * @param name the field name
     * @return the field value, or {@code null} if not present
     */
    String getField(String name);

    /**
     * Retrieves the first uploaded file for the given field name.
     * <p>
     * This is useful when a form input is expected to contain a single file.
     *
     * @param name the name of the file input field
     * @return the uploaded file, or {@code null} if no file was submitted
     */
    UploadedFile getFile(String name);

    /**
     * Retrieves all uploaded files for the given field name.
     * <p>
     * This is useful when the input allows multiple file uploads (e.g. {@code <input type="file" multiple>}).
     *
     * @param name the name of the file input field
     * @return a list of uploaded files, or an empty list if none were submitted
     */
    List<UploadedFile> getFiles(String name);
}
