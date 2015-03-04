package com.swift.io.db;

import static com.swift.io.db.DbManager.COLUMN_ACCOUNT_NAME;
import static com.swift.io.db.DbManager.COLUMN_ID;
import static com.swift.io.db.DbManager.COLUMN_MOB_NUMBER;
import static com.swift.io.db.DbManager.COLUMN_NETWORK;
import static com.swift.io.db.DbManager.COLUMN_PASSWORD;
import static com.swift.io.db.DbManager.COLUMN_TIMESTAMP;
import static com.swift.io.db.DbManager.TABLE_ACCOUNTS;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.swift.model.Account;
import com.swift.model.Network;

/**
 * Access object for the accounts table in the database This is a Singleton Factory class
 * 
 * @author Rob Powell
 * @version 1.0
 */
public class AccountDataSource implements IAccountDatabase {

	private static AccountDataSource instance = null;
	private final SQLiteDatabase database;
	private final DbManager dbManager;

	/**
	 * 
	 * @param context
	 *            Current Context object of the application
	 * @return current/new instance of the Database Access Object
	 */
	public static IAccountDatabase getInstance(final Context context) {
		if (instance == null) {
			instance = new AccountDataSource(context);
		}

		return instance;
	}

	private AccountDataSource(final Context context) {
		this.dbManager = new DbManager(context);
		this.database = this.dbManager.getWritableDatabase();
	}

	/**
	 * Support for adding new account's to ICC
	 * 
	 * @param account
	 *            Account object to add to the database
	 * @return true if insert is successful
	 */
	@Override
	public int addAccount(final Account account) {
		return this.addAccountToDb(account);
	}

	private int addAccountToDb(final Account account) {
		final ContentValues dbValues = new ContentValues();
		final long time = System.currentTimeMillis();

		dbValues.put(COLUMN_ACCOUNT_NAME, account.getAccountName());
		dbValues.put(COLUMN_MOB_NUMBER, account.getMobileNumber());
		dbValues.put(COLUMN_NETWORK, account.getOperator().name());
		dbValues.put(COLUMN_PASSWORD, account.getPassword());
		dbValues.put(COLUMN_TIMESTAMP, time);

		this.database.insert(TABLE_ACCOUNTS, null, dbValues);

		final String where = COLUMN_TIMESTAMP + " = " + time;
		final Cursor cursor = this.database.query(TABLE_ACCOUNTS, new String[] { COLUMN_ID }, where, null, null, null, null);
		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	/**
	 * Support for removing/deleting an Account from ICC
	 * 
	 * @param account
	 *            Account object to delete from the database
	 * @return true if the account is successfully deleted
	 */
	@Override
	public boolean removeAccount(final Account account) {
		final String where = COLUMN_ID + " = ?";
		final String[] whereArgs = new String[] { Integer.toString(account.getId()) };
		final int deletedRows = this.database.delete(TABLE_ACCOUNTS, where, whereArgs);

		return deletedRows == 1;
	}

	/**
	 * 
	 * @return List of all Account's from the database
	 */
	@Override
	public List<Account> getAllAccounts() {

		final List<Account> accountList = new ArrayList<Account>();

		final Cursor cursor = this.database.query(DbManager.TABLE_ACCOUNTS, null, null, null, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			accountList.add(this.cursorToAccount(cursor));
			cursor.moveToNext();
		}

		return accountList;
	}

	@Override
	public boolean isEmpty() {
		final Cursor cursor = this.database
				.query(TABLE_ACCOUNTS, new String[] { COLUMN_ID }, null, null, null, null, null, "1");
		return cursor.getCount() == 0;
	}

	@Override
	public Account getAccountById(final int id) {
		final String where = DbManager.COLUMN_ID + " = " + id;
		final Cursor cursor = this.database.query(DbManager.TABLE_ACCOUNTS, null, where, null, null, null, null);

		cursor.moveToFirst();
		return this.cursorToAccount(cursor);
	}

	private Account cursorToAccount(final Cursor cursor) {
		final String acName = cursor.getString(1);
		final String acNumber = cursor.getString(2);
		final String acPassword = cursor.getString(3);
		final Network acNetwork = Network.valueOf(cursor.getString(4));
		final Account account = new Account(acName, acNumber, acPassword, acNetwork);

		account.setId(cursor.getInt(0));
		account.setTimeStamp(cursor.getLong(5));
		return account;
	}
}