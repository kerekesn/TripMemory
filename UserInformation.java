package com.example.kerekesnora.tripmemory;

/**
 * Created by Kerekes Nora on 2018.03.12..
 */

public class UserInformation {
        String name;
        String email;

    public UserInformation(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
