package com.task11.dto;

public class TableDTO {
    private int id;
    private int number;
    private int places;
    private Boolean isVip;
    private int minOrder;

    //Default constructor
    public TableDTO() {
    }

    //All-args constructor
    public TableDTO(int id, int number, int places, Boolean isVip, int minOrder){
        this.id = id;
        this.number = number;
        this.places = places;
        this.isVip = isVip;
        this.minOrder = minOrder;
    }

    //Getters
    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public int getPlaces() {
        return places;
    }

    public Boolean getIsVip() {
        return isVip;
    }

    public int getMinOrder() {
        return minOrder;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public void setIsVip(Boolean isVip) {
        this.isVip = isVip;
    }

    public void setMinOrder(int minOrder) {
        this.minOrder = minOrder;
    }

    @Override
    public String toString() {
        return "CreateTableRequest{" +
                "id=" + id +
                ", number=" + number +
                ", places=" + places +
                ", isVip=" + isVip +
                ", minOrder=" + minOrder +
                '}';
    }
}
