package net.devemperor.chatgpt.util;

public class Util {

    public static double getFiatPrice(long price) {
        return price * 0.002 / 1000;
    }
}
