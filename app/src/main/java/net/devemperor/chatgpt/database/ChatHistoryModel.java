package net.devemperor.chatgpt.database;

import net.devemperor.chatgpt.items.ChatItem;

import java.util.List;

public class ChatHistoryModel {
    private long id;
    private final String title;
    private final List<ChatItem> chatItems;

    public ChatHistoryModel(long id, String title, List<ChatItem> chatItems) {
        this.id = id;
        this.title = title;
        this.chatItems = chatItems;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public List<ChatItem> getChatItems() {
        return chatItems;
    }

}
