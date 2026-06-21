package com.seedshare.model;

import java.math.BigDecimal;

public class Variety {
    private int id;
    private int plantId;
    private String name;
    private String growingConditions;
    private BigDecimal rating;

    public Variety() {
    }

    public Variety(int id, int plantId, String name, String growingConditions, BigDecimal rating) {
        this.id = id;
        this.plantId = plantId;
        this.name = name;
        this.growingConditions = growingConditions;
        this.rating = rating;
    }

    public Variety(int plantId, String name, String growingConditions, BigDecimal rating) {
        this(0, plantId, name, growingConditions, rating);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrowingConditions() {
        return growingConditions;
    }

    public void setGrowingConditions(String growingConditions) {
        this.growingConditions = growingConditions;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return String.format("Сорт{id=%d, plantId=%d, name='%s', rating=%s}", id, plantId, name, rating);
    }
}
