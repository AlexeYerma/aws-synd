package com.task06;

public class AuditItem {
    private String id;
    private String itemKey;
    private String modificationTime;

    public AuditItem() {
    }

    public AuditItem(String id, String itemKey, String modificationTime) {
        this.id = id;
        this.itemKey = itemKey;
        this.modificationTime = modificationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(String modificationTime) {
        this.modificationTime = modificationTime;
    }
}
