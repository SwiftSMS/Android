package com.icc.db;

import com.icc.acc.Account;
import java.util.List;

/**
 * Interface used to talk to the Account's Database
 *
 * @author Rob Powell
 */
public interface IAccountDatabase {

    public boolean addAccount(final Account account);
    public boolean removeAccount(final Account account);
    public List<Account> getAllAccounts();
}
