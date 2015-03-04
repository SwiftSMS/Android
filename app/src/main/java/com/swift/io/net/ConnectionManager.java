package com.swift.io.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConnectionManager {

	private final boolean doOutput;
	private final String method;

	private HttpURLConnection connection;
	private String webpageUrl;
	private Map<String, String> requestHeaders;
	private StringBuilder requestOutput;

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
			this.requestHeaders = new LinkedHashMap<String, String>();
			this.requestOutput = new StringBuilder();
			this.connection = (HttpURLConnection) url.openConnection();
			// this.connection.setChunkedStreamingMode(0);
			this.connection.setRequestMethod(this.method);
			this.connection.setDoOutput(this.doOutput);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method adds a HTTP POST variable key-pair to this {@link URLConnection}.
	 * 
	 * @param key
	 *            The key used for this POST header.
	 * @param value
	 *            The value to use for this POST header.
	 */
	public void addPostHeader(final String key, final String value) {
		this.requestHeaders.put(key, value);
	}

	/**
	 * This method is used to add text to a request. The data entered here is written in the request body.
	 * 
	 * @param text
	 *            The text to write.
	 */
	public void addRequestBody(final String text) {
		this.requestOutput.append(text);
	}

	/**
	 * This method will set a request header on the {@link URLConnection}.
	 * 
	 * @param field
	 *            The key used for this request header.
	 * @param value
	 *            The request header value to set.
	 */
	public void setRequestHeader(final String field, final String value) {
		this.connection.setRequestProperty(field, value);
	}

	public String connect() {
		String responseHtml = this.doConnection();
		while (this.getResponseStatus() == HttpURLConnection.HTTP_MOVED_TEMP || this.getResponseStatus() == HttpURLConnection.HTTP_MOVED_PERM) {
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
				if (this.requestHeaders.size() > 0) {
					this.writeHeaders(writer);
				}
				if (this.requestOutput.length() > 0) {
					writer.write(this.requestOutput.toString());
				}
				writer.close();
			}
			result.append(this.readStream(this.connection.getInputStream()));
		} catch (final IOException e) {
			throw new NoInternetAccessException();
		}
		return result.toString();
	}

	/**
	 * This method is responsible for writing HTTP POST headers to a {@link URLConnection}'s output stream.
	 * 
	 * @param writer
	 *            A writer wrapped around a {@link URLConnection}'s output stream.
	 * @throws IOException
	 */
	private void writeHeaders(final Writer writer) throws IOException {
		boolean firstHeader = true;
		for (final String key : this.requestHeaders.keySet()) {
			if (!firstHeader) {
				writer.write("&");
			}
			writer.write(key + "=" + this.requestHeaders.get(key));
			firstHeader = false;
		}
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