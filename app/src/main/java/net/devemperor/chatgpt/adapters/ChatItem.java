package net.devemperor.chatgpt.adapters;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ChatItem {
    private ChatMessage chatMessage;
    private long totalCost;

    public ChatItem(ChatMessage chatMessage, long totalCost) {
        this.chatMessage = chatMessage;
        this.totalCost = totalCost;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }
}
