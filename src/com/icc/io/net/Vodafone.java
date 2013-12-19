package com.icc.io.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.icc.R;
import com.icc.model.Account;

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

	private final Semaphore lock = new Semaphore(1);
	private EditText answerEditText;
	private ImageView imageView;
	private Handler handler;

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
		this.lock.acquireUninterruptibly();
		this.handler = new Handler(context.getMainLooper());

		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.captcha_dialog, null);
		this.answerEditText = (EditText) layout.findViewById(R.id.text_captcha_dialog);
		this.imageView = (ImageView) layout.findViewById(R.id.image_captcha_dialog);

		final Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.captcha);
		dialog.setView(layout);
		dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				dialog.dismiss();
				Vodafone.this.lock.release();
			}
		});
		dialog.show();
	}

	@Override
	boolean doSend(final List<String> recipients, final String message) {
		final String token = this.getToken();
		this.downloadCaptcha();
		this.lock.acquireUninterruptibly(); // wait here for the user to solve the captcha

		final ConnectionManager manager = new ConnectionManager(SEND_URL);
		manager.addPostHeader(SEND_POST_TOKEN, token);
		manager.addPostHeader(SEND_POST_MESSAGE, Uri.encode(message));
		for (int i = 0; i < recipients.size(); i++) {
			manager.addPostHeader(Uri.encode("recipients[" + i + "]"), Uri.encode(recipients.get(i)));
		}
		manager.addPostHeader(SEND_POST_CAPTCHA, this.answerEditText.getText().toString());
		final String html = manager.connect();

		this.lock.release();
		return html.contains(SEND_SUCCESS_STRING);
	}

	private void downloadCaptcha() {
		try {
			final InputStream is = new URL(CAPTCHA_URL).openStream();
			final Bitmap image = BitmapFactory.decodeStream(is);

			this.handler.post(new Runnable() {
				@Override
				public void run() {
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