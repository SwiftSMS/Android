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