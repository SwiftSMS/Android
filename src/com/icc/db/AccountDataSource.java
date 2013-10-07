package com.icc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.icc.acc.Account;
import com.icc.acc.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * Access object for the accounts table in the database
 * This is a Singleton Factory class
 * @author Rob Powell
 * @version 1.0
 */
public class AccountDataSource implements IAccountDatabase {

    private static AccountDataSource instance = null;
    private SQLiteDatabase database;
    private DbManager dbManager;

    /**
     *
     * @param context Current Context object of the application
     * @return current/new instance of the Database Access Object
     */
    public static IAccountDatabase getInstance(final Context context) {
        if(instance == null)
            instance = new AccountDataSource(context);

        return instance;
    }

    private AccountDataSource(final Context context) {
        dbManager = new DbManager(context);
        database = dbManager.getWritableDatabase();
    }

    /**
     * Support for adding new account's to ICC
     *
     * @param account Account object to add to the database
     * @return true if insert is successful
     */
    @Override
    public boolean addAccount(final Account account) {
        return addAccountToDb(account);
    }

    private boolean addAccountToDb(Account account) {

        ContentValues dbValues = new ContentValues();

        dbValues.put(DbManager.COLUMN_ACCOUNT_NAME, account.getAccountName());
        dbValues.put(DbManager.COLUMN_MOB_NUMBER, account.getMobileNumber());
        dbValues.put(DbManager.COLUMN_NETWORK, account.getOperator().name());
        dbValues.put(DbManager.COLUMN_PASSWORD, account.getPassword());
        dbValues.put(DbManager.COLUMN_TIMESTAMP, System.currentTimeMillis());

        final long idOfNewRow = database.insert(DbManager.TABLE_ACCOUNTS, null, dbValues);

        if(idOfNewRow < 0)
            return false;

        return true;
    }

    /**
     * Support for removing/deleting an Account from ICC
     *
     * @param account Account object to delete from the database
     * @return true if the account is successfully deleted
     */
    @Override
    public boolean removeAccount(final Account account) {
        return false;
    }

    /**
     *
     * @return List of all Account's from the database
     */
    @Override
    public List<Account> getAllAccounts() {

        List<Account> accountList = new ArrayList<Account>();

        final Cursor cursor = database.query(DbManager.TABLE_ACCOUNTS, null, null, null, null, null, null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            accountList.add(cursorToAccount(cursor));
            cursor.moveToNext();
        }

        return accountList;
    }

    private Account cursorToAccount(final Cursor cursor) {

        final Account account = new Account(cursor.getString(1),cursor.getString(2),cursor.getString(3), Operator.valueOf(cursor.getString(4)));
        account.setId(cursor.getInt(0));

        return account;
    }
}
