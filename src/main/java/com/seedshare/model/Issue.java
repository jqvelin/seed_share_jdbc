package com.seedshare.model;

public class Issue {
    private int id;
    private boolean pest;
    private String name;
    private String description;

    public Issue() {
    }

    public Issue(int id, boolean pest, String name, String description) {
        this.id = id;
        this.pest = pest;
        this.name = name;
        this.description = description;
    }

    public Issue(boolean pest, String name, String description) {
        this(0, pest, name, description);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPest() {
        return pest;
    }

    public void setPest(boolean pest) {
        this.pest = pest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeName() {
        return pest ? "Вредитель" : "Болезнь";
    }

    @Override
    public String toString() {
        return String.format("Проблема{id=%d, type='%s', name='%s'}", id, getTypeName(), name);
    }
}
