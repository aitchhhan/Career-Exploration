package career.exploration.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;

public enum AllowedFileType {
    JPG("jpg", List.of("image/jpeg")),
    JPEG("jpeg", List.of("image/jpeg")),
    PNG("png", List.of("image/png")),
    GIF("gif", List.of("image/gif")),
    WEBP("webp", List.of("image/webp")),

    PDF("pdf", List.of("application/pdf")),
    DOCX("docx", List.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    XLSX("xlsx", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    PPTX("pptx", List.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    TXT("txt", List.of("text/plain")),

    ZIP("zip", List.of(
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream",
            "x-zip-compressed",
            "X-ZIP-COMPRESSED",
            "application/x-compressed",
            "multipart/x-zip"
    )),
    RAR("rar", List.of("application/vnd.rar")),
    SEVEN_Z("7z", List.of("application/x-7z-compressed"));

    private final String extension;
    private final List<String> mimeTypes;

    AllowedFileType(String extension, List<String> mimeTypes) {
        this.extension = extension;
        this.mimeTypes = mimeTypes;
    }

    public String getExtension() {
        return extension;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @JsonCreator
    public static AllowedFileType from(String input) {
        if (input == null) return null;

        return Arrays.stream(values())
                .filter(type ->
                        type.name().equalsIgnoreCase(input) ||
                                type.extension.equalsIgnoreCase(input) ||
                                type.mimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(input)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("허용되지 않은 파일 타입: " + input));
    }

    @JsonValue
    public String toValue() {
        return name();
    }

    public static boolean isAllowedMimeType(String mime) {
        return Arrays.stream(values())
                .anyMatch(type -> type.mimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(mime)));
    }

    public static boolean isAllowedExtension(String ext) {
        return Arrays.stream(values())
                .anyMatch(type -> type.extension.equalsIgnoreCase(ext));
    }

    public static boolean isAllowed(String ext, String mime) {
        return Arrays.stream(values())
                .anyMatch(type ->
                        type.extension.equalsIgnoreCase(ext) &&
                                type.mimeTypes.stream().anyMatch(m -> m.equalsIgnoreCase(mime)));
    }
}