/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.camel.cxfrs.example.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement ( name = "person", namespace = "http://service.example.cxfrs.camel.redhat.com/api" )
@XmlType ( name = "person", namespace = "http://service.example.cxfrs.camel.redhat.com/api", 
    propOrder = { "id", "lastName", "firstName", "middleName", "age" } )
@XmlAccessorType ( XmlAccessType.FIELD )
public class Person implements Serializable {
    
    @JsonProperty(required=true)
    private String id;
    @JsonProperty(required=true)
    private String lastName;
    @JsonProperty(required=true)
    private String firstName;
    @JsonProperty(required=true)
    private String middleName;
    @JsonProperty(required=true)
    private Integer age;
    
    public Person() {
        
    }
    
    public Person(String id, String lastName, String firstName, String middleName, Integer age) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    @Override
    public String toString() {
        return "{\"id\":\"" + id + "\",\"lastName\":\"" + lastName + "\",\"firstName\":\"" + firstName + "\",\"middleName\":\"" + middleName + "\",\"age\":\"" + age + "\"}"; 
    }

}
