package com.example.techtalk.model;

public class ContactsTwo {
    public String name, phone, role;

    public ContactsTwo() {
    }

    public ContactsTwo(String name, String phone, String role) {
        this.name = name;
        this.phone = phone;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
