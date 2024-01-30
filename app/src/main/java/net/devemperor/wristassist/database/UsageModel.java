package net.devemperor.wristassist.database;

public class UsageModel {
    private final String modelName;
    private final long tokens;
    private final double cost;

    public UsageModel(String modelName, long tokens, double cost) {
        this.modelName = modelName;
        this.tokens = tokens;
        this.cost = cost;
    }

    public String getModelName() {
        return modelName;
    }

    public long getTokens() {
        return tokens;
    }

    public double getCost() {
        return cost;
    }
}
