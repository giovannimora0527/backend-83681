package com.uniminuto.clinica.models;

public class MiRespuestaRS {

    private int status;
    private String message;
    private Object data;

    public MiRespuestaRS() {
    }

    public MiRespuestaRS(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}