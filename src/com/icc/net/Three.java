package com.icc.net;

import com.icc.acc.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Three extends Operator {

    public Three(final Account account){
        super(account);
    }

    public String login() {
        final ConnectionManager loginManager = new ConnectionManager("https://webtexts.three.ie/webtext/users/login");
        loginManager.addPostHeader("UserTelephoneNo", this.getAccount().getMobileNumber());
        loginManager.addPostHeader("UserPin", this.getAccount().getPassword());

        return loginManager.doConnection();
    }

    @Override
    public String send(String recipient, String message) {
        return null;
    }

    private String readStream(final InputStream is) throws IOException {
        final StringBuilder result = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
