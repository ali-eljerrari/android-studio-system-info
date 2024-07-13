package com.coding.dynasty.systeminfo;

public class SystemInfo {
    private final String title;
    private final String details;

    public SystemInfo(String title, StringBuilder details) {
        this.title = title;
        this.details = String.valueOf(details);
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }
}
