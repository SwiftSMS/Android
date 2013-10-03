package com.icc.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

public class Meteor {

	public String login(final String username, final String password) {
		final HttpUriRequest request = new HttpPost("http://www.mymeteor.ie");
		final HttpClient client = new DefaultHttpClient();
		final StringBuilder result = new StringBuilder();

		try {
			final HttpResponse response = client.execute(request);
			final Header[] headers = response.getAllHeaders();
			for (final Header header : headers) {
				result.append(header.getName());
				result.append(" = ");
				result.append(header.getValue());
			}

			final HttpEntity entity = response.getEntity();
			final BufferedReader is = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line;
			while ((line = is.readLine()) != null) {
				result.append(line);
			}
		} catch (final ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString();
	}
}