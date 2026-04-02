package com.chatserver.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over the file storage back-end.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@link FileSystemStorageService} – local filesystem (default / dev)</li>
 *   <li>S3StorageService – AWS S3 (production; plug in by defining an alternative bean)</li>
 * </ul>
 */
public interface StorageService {

    /**
     * Stores the uploaded file and returns the public URL / path to access it.
     *
     * @param file multipart file from the HTTP request
     * @return public download path (e.g. {@code /uploads/uuid-filename.png})
     */
    String store(MultipartFile file);

    /**
     * Loads a stored file as a Spring {@link Resource} suitable for streaming.
     *
     * @param filename stored filename (without path prefix)
     * @return readable resource
     */
    Resource load(String filename);
}
