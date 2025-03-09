package com.example.metadata.model;

import java.util.Map;

public class MetadataModel {
    private Map<String, String> metadata;

    public MetadataModel(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
