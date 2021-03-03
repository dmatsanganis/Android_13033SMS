package com.dmatsanganis.hardaliapp.Database;

public class DatabaseModel {

    public String id, reason, data;

    public DatabaseModel(String id, String reason, String data) {
        this.id = id;
        this.reason = reason;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
