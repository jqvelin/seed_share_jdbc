package com.seedshare.model;

public class Plant {
    private int id;
    private String name;

    public Plant() {
    }

    public Plant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Plant(String name) {
        this(0, name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Растение{id=%d, name='%s'}", id, name);
    }
}
