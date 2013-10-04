package com.icc.acc;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Account {

    private String username;
    private String password;
    private AccountType accountType;

    public Account(final String username, final String pwd, final AccountType accType) {
        this.username = username;
        this.password = pwd;
        this.accountType = accType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccountType getAccountType() {
        return accountType;
    }
}