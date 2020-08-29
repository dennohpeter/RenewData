package com.dennohpeter.renewdata;

public class MessageModel {
    private String date;
    private String message;
    private String sender;

    public MessageModel(String date, String message, String sender) {
        this.date = date;
        this.message = message;
        this.sender = sender;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
