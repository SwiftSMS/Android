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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

	private HttpURLConnection connection;
	private String webpageUrl;
	private final Map<String, String> requestHeaders = new LinkedHashMap<String, String>();
	private final boolean doOutput;
	private final String method;

	/**
	 * Create a new ConnectionManager with the settings provided to open a connection to the URL provided using the
	 * {@link HttpURLConnection} APIs. <br />
	 * 
	 * @param webpageUrl
	 *            The URL of the webpage to connect to.
	 * @param requestMethod
	 *            The request method to use for the connection.
	 * @param doOutput
	 *            Whether output to the server should be done or not.
	 */
	public ConnectionManager(final String webpageUrl, final String requestMethod, final boolean doOutput) {
		this.webpageUrl = webpageUrl;
		this.method = requestMethod;
		this.doOutput = doOutput;
		this.initalize();
	}

	/**
	 * Create a new ConnectionManager with default settings to open a connection to the URL provided using the
	 * {@link HttpURLConnection} APIs. <br />
	 * By default uses a POST method and requires output to be made.
	 * 
	 * @param webpageUrl
	 *            The URL of the webpage to connect to.
	 */
	public ConnectionManager(final String webpageUrl) {
		this(webpageUrl, "POST", true);
	}

	private void initalize() {
		try {
			final URL url = new URL(this.webpageUrl);
			this.connection = (HttpURLConnection) url.openConnection();
			// this.connection.setChunkedStreamingMode(0);
			this.connection.setRequestMethod(this.method);
			this.connection.setDoOutput(this.doOutput);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPostHeader(final String key, final String value) {
		this.requestHeaders.put(key, value);
	}

	public String connect() {
		String responseHtml = this.doConnection();
		while (this.getResponseStatus() == HttpURLConnection.HTTP_MOVED_TEMP) {
			this.webpageUrl = this.connection.getHeaderField("Location");
			this.initalize();
			responseHtml = this.doConnection();
		}
		return responseHtml;
	}

	private String doConnection() {
		final StringBuilder result = new StringBuilder();
		try {
			if (this.connection.getDoOutput()) {
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
			}

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