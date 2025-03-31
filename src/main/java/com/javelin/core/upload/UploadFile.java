package com.javelin.core.upload;

/**
 * Represents a single uploaded file part in a multipart/form-data request.
 */
public class UploadedFile {
    public final String filename;
    public final String contentType;
    public final byte[] data;

    public UploadedFile(String filename, String contentType, byte[] data) {
        this.filename = filename;
        this.contentType = contentType;
        this.data = data;
    }
}
