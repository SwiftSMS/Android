package com.swift.io.db;

import java.util.List;

import com.swift.model.Account;

/**
 * Interface used to talk to the Account's Database.
 * 
 * @author Rob Powell
 */
public interface IAccountDatabase {

	/**
	 * Add a new account to the application.
	 * 
	 * @param account
	 *            Account object to add to the Database.
	 * @return Success returns the new row id, failed returns -1.
	 */
	public int addAccount(final Account account);

	/**
	 * Remove an account from the application.
	 * 
	 * @param account
	 *            Account object to remove from database, deleted by ID.
	 * @return True on Account deletion.
	 */
	public boolean removeAccount(final Account account);

	/**
	 * Get a list of all accounts currently in the application.
	 * 
	 * @return All Account's from the Database.
	 */
	public List<Account> getAllAccounts();

	/**
	 * Check is there is any accounts registered in the database.
	 * 
	 * @return true is there is no accounts in the database, otherwise false.
	 */
	public boolean isEmpty();

	/**
	 * Get a stored account based on it's id.
	 * 
	 * @param id
	 *            ID of the Account object you need from the database.
	 * @return The Account of the specified ID.
	 */
	public Account getAccountById(final int id);
}
