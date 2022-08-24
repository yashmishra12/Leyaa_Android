package com.vijaykumawat.Leyaa;

import java.sql.Timestamp;
import java.util.Date;

public class Bill_Transaction_Data {
    String itemName;
    Double itemPrice;
    Date timestamp;

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

    public Bill_Transaction_Data(String itemName, Double itemPrice, Date timestamp) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.timestamp = timestamp;
    }
}
