package com.chatserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

/**
 * {@link StorageService} implementation that stores files on the local filesystem.
 *
 * <p>Files are saved to the directory configured by {@code app.storage.upload-dir}
 * (defaults to {@code uploads/} relative to the working directory). The returned URL
 * follows the pattern {@code /uploads/<uuid>-<originalFilename>} and is served by
 * Spring's static resource handler (configured in {@link com.chatserver.config.StorageConfig}).
 */
@Slf4j
@Service
public class FileSystemStorageService implements StorageService {

    private final Path uploadDir;

    public FileSystemStorageService(@Value("${app.storage.upload-dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Storage directory initialised at: {}", uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Saves the file with a UUID prefix to avoid collisions and returns the URL path.
     */
    @Override
    public String store(MultipartFile file) {
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        if (original.contains("..")) {
            throw new IllegalArgumentException(
                    "Filename contains invalid path sequence: " + original);
        }

        String storedName = UUID.randomUUID() + "-" + original;
        Path   target     = uploadDir.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file '{}' ({} bytes)", storedName, file.getSize());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + original, e);
        }

        return "/uploads/" + storedName;
    }

    /**
     * Loads a file from the upload directory as a Spring {@link Resource}.
     */
    @Override
    public Resource load(String filename) {
        try {
            Path     file     = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Could not read file: " + filename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
}
