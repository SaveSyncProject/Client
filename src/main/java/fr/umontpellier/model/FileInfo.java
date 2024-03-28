package fr.umontpellier.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileInfo {
    private final String name;
    private final String downloadUrl;

    @JsonCreator
    public FileInfo(@JsonProperty("name") String name,
                    @JsonProperty("downloadUrl") String downloadUrl) {
        this.name = name;
        this.downloadUrl = downloadUrl;
    }

    // Getters
    public String getFileName() {
        return name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
