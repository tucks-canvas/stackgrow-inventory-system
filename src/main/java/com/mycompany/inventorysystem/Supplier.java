/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventorysystem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 *
 * @author rayjd
 */
public class Supplier {
    private String name;
    private String address;
    private String email;
    private String number;

    // Constructors (you can have multiple constructors based on your requirements)
     @JsonCreator
     public Supplier(
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("number") String number) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.number = number;
    }

    // Getter methods

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setNumber(String number){
        this.number = number;
    }
}
