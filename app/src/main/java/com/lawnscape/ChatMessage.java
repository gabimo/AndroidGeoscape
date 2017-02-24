package com.lawnscape;

/**
 * Created by Mellis on 2/23/2017.
 */

public class ChatMessage {
    private String msgId, textMsg, sentByUid, date;
    private boolean isRead;

    public ChatMessage() {
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTextMsg() {
        return textMsg;
    }

    public void setTextMsg(String textMsg) {
        this.textMsg = textMsg;
    }

    public String getSentByUid() {
        return sentByUid;
    }

    public void setSentByUid(String sentByUid) {
        this.sentByUid = sentByUid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}