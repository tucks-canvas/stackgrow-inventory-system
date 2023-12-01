/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventorysystem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author remya
 */
public class User {
    
    private String username;
    
    private String password;
    
    private int id;
    
    private String type;
    
    @JsonCreator
    public User(@JsonProperty("id ")int id, @JsonProperty("type")String type, @JsonProperty("u")String u , @JsonProperty("p")String p){
        this.username = u;
        this.password = p;
        this.id = id;
        this.type = type;
          
    }
    
   public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
