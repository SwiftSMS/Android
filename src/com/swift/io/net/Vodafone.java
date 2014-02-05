package com.swift.io.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.Failure;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Successful;
import com.swift.tasks.results.WarningResult;
import com.swift.utils.HTMLParser;

public class Vodafone extends Operator {

	private static final int MAX_MSG_RECIPIENTS = 5;

	private static final String CHARS_URL = "https://www.vodafone.ie/javascript/section.myv.webtext.js";
	private static final String TOKEN_URL = "https://www.vodafone.ie/myv/messaging/webtext/index.jsp";
	private static final String CAPTCHA_URL = "https://www.vodafone.ie/myv/messaging/webtext/Challenge.shtml";

	private static final String HTML_TOKEN_PRETEXT = "\"";
	private static final String HTML_TOKEN_POSTTEXT = "org.apache.struts.taglib.html.TOKEN\" value=\"";

	private static final String VERIFY_POST_PIN = "confirmPin";
	private static final String VERIFICATION_URL = "https://www.vodafone.ie/myv/messaging/webtext/send.shtml";

	private static final String SMS_JSON_USED = "used";
	private static final String SMS_JSON_TOTAL = "total";
	private static final String SMS_URL = "https://www.vodafone.ie/myv/dashboard/webtextdetails.shtml";

	private static final String SEND_SUCCESS_STRING = "Message sent!";
	private static final String SEND_POST_CAPTCHA = "jcaptcha_response";
	private static final String SEND_POST_MESSAGE = "message";
	private static final String SEND_POST_X = "x";
	private static final String SEND_POST_Y = "y";
	private static final String SEND_POST_TOKEN = "org.apache.struts.taglib.html.TOKEN";
	private static final String SEND_URL = "https://www.vodafone.ie/myv/messaging/webtext/Process.shtml";

	private static final String LOGIN_SUCCESS_STRING = "Sign out";
	private static final String LOGIN_POST_PASSWORD = "password";
	private static final String LOGIN_POST_USERNAME = "username";
	private static final String LOGIN_URL = "https://www.vodafone.ie/myv/services/login/Login.shtml";

	private Handler handler;
	private Context context;
	private LayoutInflater inflater;

	private ImageView imageView;
	private ProgressBar progessBar;
	private EditText answerEditText;
	private EditText verificationEditText;

	public Vodafone(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		if (this.isLoggedIn()) {
			return true;
		}
		final ConnectionManager loginManager = new ConnectionManager(LOGIN_URL);
		loginManager.addPostHeader(LOGIN_POST_USERNAME, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(LOGIN_POST_PASSWORD, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		return loginHtml.contains(LOGIN_SUCCESS_STRING);
	}

	private boolean isLoggedIn() {
		final ConnectionManager preLogin = new ConnectionManager(TOKEN_URL, "GET", false);
		final String preHtml = preLogin.connect();
		if (preHtml.contains(LOGIN_SUCCESS_STRING)) {
			return true;
		}
		return false;
	}

	@Override
	public void preSend(final Context context) {
		this.context = context;
		this.handler = new Handler(context.getMainLooper());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	OperationResult doSend(final List<String> recipients, final String message) {
		OperationResult isSent = new Failure();
		final List<List<String>> splitRecipients = this.chopped(recipients, MAX_MSG_RECIPIENTS);
		for (final List<String> sendableRecipients : splitRecipients) {
			isSent = this.sendMessage(sendableRecipients, message);
		}
		return isSent;
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager(SMS_URL, "GET", false);
		final String smsHtml = manager.connect();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final int totalSms = smsJson.getInt(SMS_JSON_TOTAL);
			final int usedSms = smsJson.getInt(SMS_JSON_USED);
			return totalSms - usedSms;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager(CHARS_URL, "GET", false);
		final String html = manager.connect();

		final String charsText = "var char_limit = ";
		final int startPos = html.lastIndexOf(charsText) + charsText.length();
		final int endPos = html.indexOf(";", startPos);
		if (startPos > charsText.length()) {
			final String characterCount = html.substring(startPos, endPos);
			return Integer.valueOf(characterCount);
		} else {
			return -1;
		}
	}

	private OperationResult sendMessage(final List<String> recipients, final String message) {
		final ConnectionManager manager = this.createSendManager(recipients, message);

		final String captcha = this.getCaptchaResponse();
		if (captcha.isEmpty()) {
			return new WarningResult(R.string.message_cancelled);
		}
		manager.addPostHeader(SEND_POST_CAPTCHA, captcha);

		final String sendHtml = manager.connect();
		if (sendHtml.contains(HTML_TOKEN_POSTTEXT)) {
			return this.handleVerificationCode(sendHtml);
		}

		final boolean isSent = sendHtml.contains(SEND_SUCCESS_STRING);
		return isSent ? new Successful() : new Failure();
	}

	private ConnectionManager createSendManager(final List<String> recipients, final String message) {
		final String token = this.getToken();
		final int x = (int) (Math.random() * 60) + 1;
		final int y = (int) (Math.random() * 20) + 1;

		final ConnectionManager manager = new ConnectionManager(SEND_URL);
		manager.addPostHeader(SEND_POST_TOKEN, token);
		manager.addPostHeader(SEND_POST_X, Integer.toString(x));
		manager.addPostHeader(SEND_POST_Y, Integer.toString(y));
		manager.addPostHeader(SEND_POST_MESSAGE, message);
		for (int i = 0; i < recipients.size(); i++) {
			final String key = Uri.encode("recipients[" + i + "]");
			final String value = Uri.encode(recipients.get(i));
			manager.addPostHeader(key, value);
		}
		return manager;
	}

	/**
	 * This method is responsible for waiting for the user to complete the captcha.
	 * 
	 * @return The Captcha answer.
	 */
	private String getCaptchaResponse() {
		this.displayCaptchaDialog();
		this.downloadCaptcha();
		while (this.answerEditText.isShown()) {
			this.waitFor();
		}
		return this.answerEditText.getText().toString();
	}

	private void downloadCaptcha() {
		try {
			final InputStream is = new URL(CAPTCHA_URL).openStream();
			final Bitmap image = BitmapFactory.decodeStream(is);

			this.handler.post(new Runnable() {
				@Override
				public void run() {
					Vodafone.this.progessBar.setVisibility(View.GONE);
					Vodafone.this.imageView.setImageBitmap(image);
				}
			});
		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void displayCaptchaDialog() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				final View layout = Vodafone.this.inflater.inflate(R.layout.captcha_dialog, null);
				Vodafone.this.answerEditText = (EditText) layout.findViewById(R.id.text_captcha_dialog);
				Vodafone.this.imageView = (ImageView) layout.findViewById(R.id.image_captcha_dialog);
				Vodafone.this.progessBar = (ProgressBar) layout.findViewById(R.id.image_captcha_progress);

				final Builder builder = new AlertDialog.Builder(Vodafone.this.context);
				builder.setTitle(R.string.captcha);
				builder.setView(layout);
				builder.setPositiveButton(R.string.ok, null);

				final AlertDialog dialog = builder.create();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				dialog.show();
			}
		});
	}

	private String getToken() {
		final ConnectionManager manager = new ConnectionManager(TOKEN_URL, "GET", false);
		final String html = manager.connect();

		return HTMLParser.parseHtml(html, HTML_TOKEN_POSTTEXT, HTML_TOKEN_PRETEXT);
	}

	/**
	 * When the {@link #sendMessage(List, String)} is redirected to an
	 * 
	 * @param sendHtml
	 * @return
	 */
	private OperationResult handleVerificationCode(final String sendHtml) {
		final String token = HTMLParser.parseHtml(sendHtml, HTML_TOKEN_POSTTEXT, HTML_TOKEN_PRETEXT);
		final String code = this.getVerificationCode();

		final ConnectionManager manager = new ConnectionManager(VERIFICATION_URL);
		manager.addPostHeader(SEND_POST_TOKEN, token);
		manager.addPostHeader(VERIFY_POST_PIN, code);
		final String verifyHtml = manager.connect();

		final boolean isSent = verifyHtml.contains(SEND_SUCCESS_STRING);
		return isSent ? new Successful() : new Failure();
	}

	private String getVerificationCode() {
		this.displayVerificationDialog();
		while (this.verificationEditText == null || !this.verificationEditText.isShown()) { // wait until dialog appears
			this.waitFor();
		}
		while (this.verificationEditText.isShown()) { // wait until dialog is dismissed
			this.waitFor();
		}
		return this.verificationEditText.getText().toString();
	}

	private void displayVerificationDialog() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				final View layout = Vodafone.this.inflater.inflate(R.layout.verification_code_dialog, null);
				Vodafone.this.verificationEditText = (EditText) layout.findViewById(R.id.text_verification_code);

				final Builder builder = new AlertDialog.Builder(Vodafone.this.context);
				builder.setTitle(R.string.enter_verification_code);
				builder.setView(layout);
				builder.setPositiveButton(R.string.ok, null);

				final AlertDialog dialog = builder.create();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				dialog.show();
			}
		});
	}

	private void waitFor() {
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to break a {@link List} into multiple smaller lists.
	 * 
	 * @param list
	 *            The larger list to be broken down.
	 * @param length
	 *            The max length of a returned {@link List}.
	 * @return A {@link List} containing the smaller broken down lists.
	 */
	private <T> List<List<T>> chopped(final List<T> list, final int length) {
		final List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += length) {
			final List<T> sublist = list.subList(i, Math.min(N, i + length));
			parts.add(new ArrayList<T>(sublist));
		}
		return parts;
	}
}