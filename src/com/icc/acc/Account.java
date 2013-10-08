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
    private Operator operator;

    /**
     *
     * @param mobileNumber Mobile Number of the user, used to login to the Operator's service
     * @param accountName ICC specific account name, to identify different account's within ICC
     * @param pwd Password of the user's operator account
     * @param operator The user's Telecoms Operator
     */
    public Account(final String mobileNumber, final String accountName, final String pwd, final Operator operator) {
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

    public Operator getOperator() {
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