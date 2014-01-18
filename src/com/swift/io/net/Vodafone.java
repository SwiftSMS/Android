package com.swift.io.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.swift.tasks.Status;

public class Vodafone extends Operator {

	private static final String CHARS_URL = "https://www.vodafone.ie/javascript/section.myv.webtext.js";
	private static final String TOKEN_URL = "https://www.vodafone.ie/myv/messaging/webtext/index.jsp";
	private static final String CAPTCHA_URL = "https://www.vodafone.ie/myv/messaging/webtext/Challenge.shtml";

	private static final String SMS_JSON_USED = "used";
	private static final String SMS_JSON_TOTAL = "total";
	private static final String SMS_URL = "https://www.vodafone.ie/myv/dashboard/webtextdetails.shtml";

	private static final String SEND_SUCCESS_STRING = "Message sent!";
	private static final String SEND_POST_CAPTCHA = "jcaptcha_response";
	private static final String SEND_POST_MESSAGE = "message";
	private static final String SEND_POST_TOKEN = "org.apache.struts.taglib.html.TOKEN";
	private static final String SEND_URL = "https://www.vodafone.ie/myv/messaging/webtext/Process.shtml";

	private static final String LOGIN_SUCCESS_STRING = "Sign out";
	private static final String LOGIN_POST_PASSWORD = "password";
	private static final String LOGIN_POST_USERNAME = "username";
	private static final String LOGIN_URL = "https://www.vodafone.ie/myv/services/login/Login.shtml";

	private EditText answerEditText;
	private ImageView imageView;
	private Handler handler;
	private ProgressBar progessBar;
	private LayoutInflater inflater;
	private Context context;

	public Vodafone(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager(LOGIN_URL);
		loginManager.addPostHeader(LOGIN_POST_USERNAME, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(LOGIN_POST_PASSWORD, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		return loginHtml.contains(LOGIN_SUCCESS_STRING);
	}

	@Override
	public void preSend(final Context context) {
		this.context = context;
		this.handler = new Handler(context.getMainLooper());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	Status doSend(final List<String> recipients, final String message) {
		final String token = this.getToken();
		final String captcha = this.getCaptchaResponse();
		if (captcha.isEmpty()) {
			return Status.CANCELLED;
		}

		final ConnectionManager manager = new ConnectionManager(SEND_URL);
		manager.addPostHeader(SEND_POST_TOKEN, token);
		manager.addPostHeader(SEND_POST_MESSAGE, message);
		for (int i = 0; i < recipients.size(); i++) {
			final String key = Uri.encode("recipients[" + i + "]");
			final String value = Uri.encode(recipients.get(i));
			manager.addPostHeader(key, value);
		}
		manager.addPostHeader(SEND_POST_CAPTCHA, captcha);
		final boolean isSent = manager.connect().contains(SEND_SUCCESS_STRING);
		return isSent ? Status.SUCCESS : Status.FAILED;
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
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
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

		final String charsText = "org.apache.struts.taglib.html.TOKEN\" value=\"";
		final int startPos = html.indexOf(charsText) + charsText.length();
		final int endPos = html.indexOf("\"", startPos);
		if (startPos > charsText.length()) {
			return html.substring(startPos, endPos);
		}
		return "";
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
}