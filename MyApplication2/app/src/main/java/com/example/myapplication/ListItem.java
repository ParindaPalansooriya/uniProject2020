package com.example.myapplication;

public class ListItem {
    String text,type,link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    boolean isUser;

    public ListItem(String text, String type,boolean isUser, String link) {
        this.text = text;
        this.type = type;
        this.link = link;
        this.isUser = isUser;
    }

    public ListItem(String text, String type, boolean isUser) {
        this.text = text;
        this.type = type;
        this.isUser = isUser;
        this.link = "NO";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
