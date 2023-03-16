package net.devemperor.chatgpt.database;

import net.devemperor.chatgpt.adapters.ChatItem;

import java.util.List;

public class ChatHistoryModel {
    private int id;
    private String title;
    private List<ChatItem> chatItems;

    public ChatHistoryModel(int id, String title, List<ChatItem> chatItems) {
        this.id = id;
        this.title = title;
        this.chatItems = chatItems;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChatItem> getChatItems() {
        return chatItems;
    }

    public void setChatItems(List<ChatItem> chatItems) {
        this.chatItems = chatItems;
    }
}
