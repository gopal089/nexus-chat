package com.chatserver.dto;

import lombok.*;

/**
 * Response body returned after a successful file upload.
 * Also broadcast as a WebSocket message to alert chat participants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /** Public URL to download the file. */
    private String  url;

    /** Original filename as uploaded by the client. */
    private String  fileName;

    /** MIME type (e.g. {@code image/png}, {@code application/pdf}). */
    private String  fileType;

    /** File size in bytes. */
    private long    size;
}
