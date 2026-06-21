package com.seedshare.model;

public class PlantIssue {
    private int plantId;
    private int issueId;
    private String treatment;

    public PlantIssue() {
    }

    public PlantIssue(int plantId, int issueId, String treatment) {
        this.plantId = plantId;
        this.issueId = issueId;
        this.treatment = treatment;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    @Override
    public String toString() {
        return String.format("Связь{plantId=%d, issueId=%d}", plantId, issueId);
    }
}
