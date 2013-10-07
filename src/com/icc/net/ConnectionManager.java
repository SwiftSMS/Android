package com.icc.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
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
			this.connection.setDoOutput(true);
			this.connection.setRequestMethod("POST");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addRequestHeader(final String key, final String value) {
		this.requestHeaders.put(key, value);
	}

	public String doConnection() {
		final StringBuilder result = new StringBuilder();
		try {
			final Writer writer = new OutputStreamWriter(this.connection.getOutputStream());
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
		// result.append(connection.getResponseCode());
		// result.append("\n");
		// for (final String key : connection.getHeaderFields().keySet()) {
		// result.append(key);
		// result.append("=");
		// result.append(connection.getHeaderFields().get(key));
		// result.append("\n");
		// }
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