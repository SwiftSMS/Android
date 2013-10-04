package com.icc.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Meteor {
	
	/*
	 * Steps to send
	 * 	1. Get session cookie
	 * 		- https://www.mymeteor.ie/go/mymeteor-login-manager
	 * 		  POST
	 * 			username=phone
	 * 			userpass=pin
	 * 			login=
	 * 			returnTo=/
	 * 		- Session cookie should be in the returned headers
	 * 		  Example
	 * 			CFID=35626768
	 * 			CFTOKEN=13595949
	 * 	2. Add recipients
	 * 		- https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=35626768&CFTOKEN=13595949&func=addEnteredMsisdns
	 * 		  POST (i think)
	 * 			ajaxRequest=addEnteredMSISDNs
	 * 			remove=-
	 * 			add=NUMBER
	 * 		- Form to replicate the adding
	 * 			<form action="https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=35626768&CFTOKEN=13595949&func=addEnteredMsisdns" method="POST">
	 *				<input name="ajaxRequest" value="addEnteredMSISDNs" />
	 *				<input name="remove" value="-" />
	 *				<input name="add" value="0871234567" />
	 *				<input type="submit" />
	 *			</form>
	 * 	3. Send message
	 * 		- https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=35626768&CFTOKEN=13595949&func=sendSMS
	 * 		  POST (i think)
	 * 			ajaxRequest=sendSMS
	 * 			messageText=MESSAGE
	 * 		- Form to replicate the adding
	 *			<form action="https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&func=sendSMS&CFID=35626768&CFTOKEN=13595949" method="POST">
	 *				<input name="ajaxRequest" value="sendSMS" />
	 *				<input name="messageText" value="My Text Message :-)" />
	 *				<input type="submit" />
	 *			</form> 
	 */

	public String login(final String username, final String password) {
		final StringBuilder result = new StringBuilder();
		try {
			final URL url = new URL("https://www.mymeteor.ie/go/mymeteor-login-manager");
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			final Writer writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write("username=" + username);
			writer.write("&userpass=" + password);
			writer.write("&login=");
			writer.write("&returnTo=/");
			writer.close();

			result.append(connection.getResponseCode());
			result.append(connection.getHeaderFields().toString());
			// result.append(this.readStream(connection.getErrorStream()));
			result.append(this.readStream(connection.getInputStream()));

		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString();
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