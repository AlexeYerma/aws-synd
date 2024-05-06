package com.task06;

public class UpdatedAuditValue extends  AuditItem {
    private String updatedAttribute;
    private Object newValue;
    private Object oldValue;

    public UpdatedAuditValue(String updatedAttribute, Object newValue, Object oldValue) {
        this.updatedAttribute = updatedAttribute;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public UpdatedAuditValue(String id, String itemKey, String modificationTime, String updatedAttribute, Object newValue, Object oldValue) {
        super(id, itemKey, modificationTime);
        this.updatedAttribute = updatedAttribute;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public UpdatedAuditValue() {
    }

    public String getUpdatedAttribute() {
        return updatedAttribute;
    }

    public void setUpdatedAttribute(String updatedAttribute) {
        this.updatedAttribute = updatedAttribute;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }
}
