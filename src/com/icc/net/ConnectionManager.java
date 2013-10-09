package com.icc.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

	private HttpURLConnection connection;
	private final String webpageUrl;
	private final Map<String, String> requestHeaders = new HashMap<String, String>();

	public ConnectionManager(final String webpageUrl) {
		this.webpageUrl = webpageUrl;
		this.initalize();
	}

	private void initalize() {
		try {
			final URL url = new URL(this.webpageUrl);
			this.connection = (HttpURLConnection) url.openConnection();
			// this.connection.setChunkedStreamingMode(0);
			this.connection.setRequestMethod("POST");
			this.connection.setDoOutput(true);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPostHeader(final String key, final String value) {
		this.requestHeaders.put(key, value);
	}

	public String doConnection() {
		final StringBuilder result = new StringBuilder();
		try {
			final Writer writer = new BufferedWriter(new OutputStreamWriter(this.connection.getOutputStream()));
			boolean firstHeader = true;
			for (final String key : this.requestHeaders.keySet()) {
				if (!firstHeader) {
					writer.write("&");
				}
				writer.write(key + "=" + this.requestHeaders.get(key));
				firstHeader = false;
			}
			writer.close();

			result.append(this.readStream(this.connection.getInputStream()));
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toString();
	}

	public Map<String, List<String>> getResponseHeaders() {
		return this.connection.getHeaderFields();
	}

	public int getResponseStatus() {
		try {
			return this.connection.getResponseCode();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public String getResponseMessage() {
		try {
			return this.connection.getResponseMessage();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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