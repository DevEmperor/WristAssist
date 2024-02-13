package net.devemperor.wristassist.database;

public class ImageModel {
    int id;
    String prompt;
    String revisedPrompt;
    String model;
    String quality;
    String size;
    String style;
    long created;
    String url;

    public ImageModel(int id, String prompt, String revisedPrompt, String model, String quality, String size, String style, long created, String url) {
        this.id = id;
        this.prompt = prompt;
        this.revisedPrompt = revisedPrompt;
        this.model = model;
        this.quality = quality;
        this.size = size;
        this.style = style;
        this.created = created;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    public String getModel() {
        return model;
    }

    public String getQuality() {
        return quality;
    }

    public String getSize() {
        return size;
    }

    public String getStyle() {
        return style;
    }

    public long getCreated() {
        return created;
    }

    public String getUrl() {
        return url;
    }
}
