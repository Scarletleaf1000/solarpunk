package me.scarletleaf1000.sunworks.block.custom.cable;

public enum CableTier {
    BASIC("basic", "simple_power_cable", 60);

    private final String name;
    private final String textureName;
    private final int maxTransfer;

    CableTier(String name, String textureName, int maxTransfer) {
        this.name = name;
        this.textureName = textureName;
        this.maxTransfer = maxTransfer;
    }

    public String getName() {
        return name;
    }

    public String getTextureName() {
        return textureName;
    }

    public int getMaxTransfer() {
        return maxTransfer;
    }
}
