package com.javelin.core.upload;

/**
 * Represents a single uploaded file in a {@code multipart/form-data} request.
 * <p>
 * Each instance holds the original filename, content type, and raw byte data of the file.
 * This class is typically used in {@link MultipartForm} implementations to encapsulate uploaded files.
 */
public class UploadedFile {

    /** The original filename as submitted by the client (e.g., "image.png") */
    public final String filename;

    /** The MIME type of the uploaded file (e.g., "image/png", "application/pdf") */
    public final String contentType;

    /** The raw file content as a byte array */
    public final byte[] data;

    /**
     * Creates a new {@code UploadedFile} instance.
     *
     * @param filename    the original name of the uploaded file
     * @param contentType the MIME type of the file
     * @param data        the file content as bytes
     */
    public UploadedFile(String filename, String contentType, byte[] data) {
        this.filename = filename;
        this.contentType = contentType;
        this.data = data;
    }
}
