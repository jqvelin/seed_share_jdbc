package com.seedshare.model;

import java.math.BigDecimal;

public class Gardener {
    private int id;
    private String username;
    private BigDecimal rating;

    public Gardener() {
    }

    public Gardener(int id, String username, BigDecimal rating) {
        this.id = id;
        this.username = username;
        this.rating = rating;
    }

    public Gardener(String username, BigDecimal rating) {
        this(0, username, rating);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return String.format("Садовод{id=%d, username='%s', rating=%s}", id, username, rating);
    }
}
