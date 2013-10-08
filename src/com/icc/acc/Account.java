package com.icc.acc;

/**
 * Account Object used for the ICC user's account details
 * @author Rob Powell
 * @version 1.0
 */
public class Account {

    private int id;
    private String accountName;
    private String password;
    private String mobileNumber;
    private Network operator;

    public Account(final String mobileNumber, final String accountName, final String pwd, final Network operator) {
        this.accountName = accountName;
        this.mobileNumber = mobileNumber;
        this.password = pwd;
        this.operator = operator;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPassword() {
        return password;
    }

    public Network getOperator() {
        return operator;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}