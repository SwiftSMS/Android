package com.icc.net;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.icc.InternalString;
import com.icc.R;
import com.icc.model.Account;
import com.icc.tasks.DownloadImageTask;

public class Vodafone extends Operator {

	private final Semaphore lock = new Semaphore(1);;
	private EditText answerEditText;

	public Vodafone(final Account account) {
		super(account);
	}

	/**
	 * <pre>
	 * Steps to send
	 * 	1. Get session cookie
	 * 		- https://www.vodafone.ie/myv/services/login/Login.shtml
	 * 		  POST
	 * 			username=phone
	 * 			password=pin
	 * 		- Session cookie will be in the returned headers
	 * 		- Form to replicate logging in
	 * 			form/vodafone_login.html
	 * 	2. Send message
	 * 		- https://www.vodafone.ie/myv/messaging/webtext/Process.shtml
	 * 		  POST
	 * 			org.apache.struts.taglib.html.TOKEN=xxxx
	 * 			message=My Message
	 * 			recipients[0]=NUMBER
	 * 			recipients[1]=NUMBER
	 * 			recipients[2]=NUMBER
	 * 			recipients[3]=NUMBER
	 * 			recipients[4]=NUMBER
	 * 			jcaptcha_response=CAPTCHA
	 * 		- Form to replicate the adding
	 * 			form/vodafone_send.html
	 * </pre>
	 */

	@Override
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager("https://www.vodafone.ie/myv/services/login/Login.shtml");
		loginManager.addPostHeader("username", this.getAccount().getMobileNumber());
		loginManager.addPostHeader("password", this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains("302");
	}

	@Override
	public void preSend(final Context context) {
		this.lock.acquireUninterruptibly();

		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.captcha_dialog, null);
		this.answerEditText = (EditText) layout.findViewById(R.id.text_captcha_dialog);
		final ImageView imageView = (ImageView) layout.findViewById(R.id.image_captcha_dialog);
		new DownloadImageTask(imageView).execute("https://www.vodafone.ie/myv/messaging/webtext/Challenge.shtml");

		final Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Captcha");
		dialog.setView(layout);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
		Log.d(InternalString.LOG_TAG, "doSend - wait for captcha dialog");
		this.lock.acquireUninterruptibly();
		Log.d(InternalString.LOG_TAG, "doSend - captcha dialog done: " + this.answerEditText.getText().toString());
		Log.d(InternalString.LOG_TAG, "doSend - get TOKEN");
		final String token = this.getToken();
		Log.d(InternalString.LOG_TAG, "doSend - TOKEN generated: " + token);
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/Process.shtml");
		manager.addPostHeader("org.apache.struts.taglib.html.TOKEN", token);
		Log.d(InternalString.LOG_TAG, "doSend - message: " + Uri.encode(message));
		manager.addPostHeader("message", Uri.encode(message));
		for (int i = 0; i < recipients.size(); i++) {
			manager.addPostHeader(Uri.encode("recipients[" + i + "]"), Uri.encode(recipients.get(i)));
		}
		manager.addPostHeader("jcaptcha_response", this.answerEditText.getText().toString());
		Log.d(InternalString.LOG_TAG, "doSend - try send SMS");
		final String html = manager.doConnection();
		Log.d(InternalString.LOG_TAG, "doSend - SMS sent:");
		Log.d(InternalString.LOG_TAG, html);

		Log.d(InternalString.LOG_TAG, "doSend - send Response Status: " + manager.getResponseStatus());

		this.lock.release();
		return html.contains("Message sent!");
	}

	private String getToken() {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/index.jsp",
				"GET", false);
		final String html = manager.doConnection();

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
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/dashboard/webtextdetails.shtml",
				"GET", false);
		final String smsHtml = manager.doConnection();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final int totalSms = smsJson.getInt("total");
			final int usedSms = smsJson.getInt("used");
			return totalSms - usedSms;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/javascript/section.myv.webtext.js",
				"GET", false);
		final String html = manager.doConnection();

		final String charsText = "var char_limit = ";
		final int startPos = html.indexOf(charsText) + charsText.length();
		final int endPos = html.indexOf(";", startPos);
		if (startPos > charsText.length()) {
			final String characterCount = html.substring(startPos, endPos);
			return Integer.valueOf(characterCount);
		} else {
			return -1;
		}
	}
}