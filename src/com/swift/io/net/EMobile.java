package com.swift.io.net;

import com.swift.model.Account;
import com.swift.tasks.Status;

import java.util.List;

/**
 * Created by Rob Powell on 17/01/14.
 */
public class EMobile extends Operator {

    public EMobile(final Account account) {
        super(account);
    }

    @Override
    boolean doLogin() {
        return false;
    }

    @Override
    int doGetRemainingSMS() {
        return 0;
    }

    @Override
    Status doSend(List<String> list, String message) {
        return null;
    }

    @Override
    int doGetCharacterLimit() {
        return 0;
    }
}
