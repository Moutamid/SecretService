package com.moutamid.secretservice.models;

public class MessageModel {
    String keyword, msg;

    public MessageModel(String keyword, String msg) {
        this.keyword = keyword;
        this.msg = msg;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
