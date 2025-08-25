package com.example.uvideo.exceptions;

import java.util.Date;

public class ApiError {
    private String error;
    private String message;
    private Date timestamp;
    private Integer status;
    private String path;

    public ApiError(String error, String message, Integer status, String path) {
        this.error = error;
        this.message = message;
        this.timestamp = new Date();
        this.status = status;
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}