package com.mycompany.inventorysystem;

import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.util.Date;

@JsonPropertyOrder({"date", "oldStock", "newStock"})
public class StockChange {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String date;
    private int oldStock;
    private int newStock;

    @JsonCreator
    public StockChange(
        @JsonProperty("date") String date,
        @JsonProperty("oldStock") int oldStock,
        @JsonProperty("newStock") int newStock) {
        
        this.date = date;
        this.oldStock = oldStock;
        this.newStock = newStock;
    }

    public String getDate() {
        return date;
    }

    public int getOldStock() {
         return oldStock;
    }

    public int getNewStock() {
        return newStock;
    }

    @Override
    public String toString() {
        return "StockChange{" +
            "date=" + date +
            ", oldStock=" + oldStock +
            ", newStock=" + newStock +
            '}';
    }

    public int CalculateChange() {
        return oldStock - newStock;
    }
    
//        public  String dateToString() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        return date.format(formatter);
//    }

    // Convert String to LocalDate
    public  LocalDate stringToDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }
}
