package com.vijaykumawat.Leyaa;

import java.util.Date;

public class Bill_Transaction_Data {
    String itemName;
    Double itemPrice;
    Date timestamp;
    String payer, contributor;

    public Bill_Transaction_Data(String itemName, Double itemPrice, Date timestamp, String payer, String contributor) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.timestamp = timestamp;
        this.payer = payer;
        this.contributor = contributor;
    }

    public Bill_Transaction_Data(){}

    public String getItemName() {
        return itemName;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getPayer() {
        return payer;
    }

    public String getContributor() {
        return contributor;
    }
}
