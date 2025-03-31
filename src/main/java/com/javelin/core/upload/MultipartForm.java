package com.javelin.core.upload;

import java.util.List;

/**
 * Represents a parsed multipart/form-data request.
 * Allows access to regular form fields and uploaded files.
 */
public interface MultipartForm {
    String getField(String name);
    UploadedFile getFile(String name);
    List<UploadedFile> getFiles(String name);
}
