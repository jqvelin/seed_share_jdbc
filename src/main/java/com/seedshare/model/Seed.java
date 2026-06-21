package com.seedshare.model;

public class Seed {
    private int id;
    private int varietyId;
    private int gardenerId;
    private Integer harvestYear;
    private String lineage;
    private int packetsCount;

    public Seed() {
    }

    public Seed(int id, int varietyId, int gardenerId, Integer harvestYear, String lineage, int packetsCount) {
        this.id = id;
        this.varietyId = varietyId;
        this.gardenerId = gardenerId;
        this.harvestYear = harvestYear;
        this.lineage = lineage;
        this.packetsCount = packetsCount;
    }

    public Seed(int varietyId, int gardenerId, Integer harvestYear, String lineage, int packetsCount) {
        this(0, varietyId, gardenerId, harvestYear, lineage, packetsCount);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVarietyId() {
        return varietyId;
    }

    public void setVarietyId(int varietyId) {
        this.varietyId = varietyId;
    }

    public int getGardenerId() {
        return gardenerId;
    }

    public void setGardenerId(int gardenerId) {
        this.gardenerId = gardenerId;
    }

    public Integer getHarvestYear() {
        return harvestYear;
    }

    public void setHarvestYear(Integer harvestYear) {
        this.harvestYear = harvestYear;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public int getPacketsCount() {
        return packetsCount;
    }

    public void setPacketsCount(int packetsCount) {
        this.packetsCount = packetsCount;
    }

    @Override
    public String toString() {
        return String.format("Семена{id=%d, varietyId=%d, gardenerId=%d, packets=%d}", id, varietyId, gardenerId, packetsCount);
    }
}
