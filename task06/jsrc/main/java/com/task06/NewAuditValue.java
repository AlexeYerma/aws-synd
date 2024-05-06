package com.task06;

public class NewAuditValue extends AuditItem {
    private Pair newValue;

    public NewAuditValue() {
    }

    public NewAuditValue(String id, String itemKey, String modificationTime, Pair newValue) {
        super(id, itemKey, modificationTime);
        this.newValue = newValue;
    }

    public Pair getNewValue() {
        return newValue;
    }

    public void setNewValue(Pair newValue) {
        this.newValue = newValue;
    }
}
