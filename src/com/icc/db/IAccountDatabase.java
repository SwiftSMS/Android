package com.icc.db;

import com.icc.model.Account;

import java.util.List;

/**
 * Interface used to talk to the Account's Database
 *
 * @author Rob Powell
 */
public interface IAccountDatabase {

    /**
     *
     * @param account
     *                  Account object to add to the Database
     * @return
     *         Success returns the new row id, failed returns -1
     */
    public int addAccount(final Account account);

    /**
     *
     * @param account
     *               Account object to remove from database, deleted by ID
     * @return
     *        True on Account deletion
     */
    public boolean removeAccount(final Account account);

    /**
     *
     * @return
     *          All Account's from the Database
     */
    public List<Account> getAllAccounts();

    /**
     *
     * @param id
     *          ID of the Account object you need from the database
     * @return
     *          The Account of the specified ID
     */
    public Account getAccountById(final int id);
}
