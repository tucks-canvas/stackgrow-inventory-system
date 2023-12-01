/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventorysystem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



/**
 *
 * @author remya
 */
public class Product {
    private int id;
    private String name;
    private String type;
    private String description;
    private String weight;
    private String brand;
    private int stock;
    private float retailCost;
    private float wholesaleCost;
    private String supplier;
    private List<StockChange> stockChangeHistory;
    

    // Constructors (you can have multiple constructors based on your requirements)
    @JsonCreator
    public Product(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("description") String description,
            @JsonProperty("weight") String weight,
            @JsonProperty("brand") String brand,
            @JsonProperty("stock") int stock,
            @JsonProperty("retailCost") float retailCost,
            @JsonProperty("wholesaleCost") float wholesaleCost,
            @JsonProperty("supplier") String supplier) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.weight = weight;
        this.brand = brand;
        this.stock = stock;
        this.retailCost = retailCost;
        this.wholesaleCost = wholesaleCost;
        this.supplier = supplier;
        stockChangeHistory = new ArrayList<>();
    }

    Product() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    // Getter methods

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getWeight() {
        return weight;
    }

    public String getBrand() {
        return brand;
    }

    public int getStock() {
        return stock;
    }

    public float getRetailCost() {
        return retailCost;
    }

    public float getWholesaleCost() {
        return wholesaleCost;
    }

    public String getSupplier() {
        return supplier;
    }
    public List<StockChange> getStockChangeHistory() {
        return stockChangeHistory;
    }
    public int calculateDailySale(){
        int sale= 0;
        LocalDate today = LocalDate.now();
        for (StockChange stockChange : stockChangeHistory) {
            // Check if the date of the stock change matches today's date
            System.out.println(stockChange.getDate());
            System.out.println(today);
            LocalDate stockChangeDate = stockChange.stringToDate();
            if (stockChangeDate.equals(today)) {
                System.out.println("hi");
                // Check if the old stock is greater than the new stock
                if (stockChange.getOldStock() > stockChange.getNewStock()) {
                    // Calculate the difference and add to the sale
                        sale += stockChange.getOldStock() - stockChange.getNewStock();
                }
            }
        }
        return sale;
    }
    public int calculateWeeklySales() {
        int sale = 0;

                
        LocalDate today = LocalDate.now();
        LocalDate startDateOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
        LocalDate endDateOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));


        // Iterate through stockChangeHistory
        for (StockChange stockChange : stockChangeHistory) {
            // Check if the date of the stock change is within the current week
            LocalDate stockChangeDate = stockChange.stringToDate();
        if (stockChangeDate.isEqual(startDateOfWeek) || (stockChangeDate.isAfter(startDateOfWeek) && stockChangeDate.isBefore(endDateOfWeek))) {
                // Check if the old stock is greater than the new stock
                System.out.println(stockChangeDate.isEqual(startDateOfWeek) || (stockChangeDate.isAfter(startDateOfWeek) && stockChangeDate.isBefore(endDateOfWeek)));
                if (stockChange.getOldStock() > stockChange.getNewStock()) {
                    // Calculate the difference and add to the sale
                    sale += stockChange.getOldStock() - stockChange.getNewStock();
                }
            }
        }

        return sale;
    }

     // Setter methods
    public void setSupplier(String Supplier) {
        this.supplier = Supplier;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

   
    public void setStock(int stock) {
        int oldStock = this.stock;
        this.stock = stock;

        // Record the stock change
        StockChange stockChange = new StockChange( dateToString(LocalDate.now()), oldStock, stock);
        stockChangeHistory.add(stockChange);
    }
    public  String dateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    

    public void setRetailCost(float retailCost) {
        this.retailCost = retailCost;
    }

    public void setWholesaleCost(float wholesaleCost) {
        this.wholesaleCost = wholesaleCost;
    }
    
    

}