package net.devemperor.chatgpt.items;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ChatItem {
    private final ChatMessage chatMessage;
    private final long totalCost;

    public ChatItem(ChatMessage chatMessage, long totalCost) {
        this.chatMessage = chatMessage;
        this.totalCost = totalCost;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public long getTotalCost() {
        return totalCost;
    }

}
