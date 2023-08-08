package net.devemperor.wristassist.items;

public class MainItem {
    private final String text;
    private final int icon;

    public MainItem(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getIcon() {
        return icon;
    }
}
