package com.swift.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

/**
 * How to use this logger.
 * <code>
 * <pre>
 * logger.log("Vodafone#handleVerificationCode - TOKEN = " + token);
 * logger.log("Vodafone#handleVerificationCode - HTML = " + sendHtml);
 * logger.showSendDialog();
 * </pre>
 * </code>
 * @param context
 */
public class Logger {

	private static final Semaphore lock = new Semaphore(1);

	private final Context context;
	private final Handler handler;

	private final File logFile;

	/**
	 * How to use this logger.
	 * <code>
	 * <pre>
	 * logger.log("Vodafone#handleVerificationCode - TOKEN = " + token);
	 * logger.log("Vodafone#handleVerificationCode - HTML = " + sendHtml);
	 * logger.showSendDialog();
	 * </pre>
	 * </code>
	 * @param context
	 */
	public Logger(final Context context) {
		this.context = context;
		this.handler = new Handler(context.getMainLooper());

		final File filesDir = this.context.getExternalFilesDir(null);
		this.logFile = new File(filesDir, "swiftsms.log");
	}

	public void log(final String msg) {
		lock.acquireUninterruptibly();
		try {
			final BufferedWriter logger = new BufferedWriter(new FileWriter(this.logFile, true));

			logger.append(msg);
			logger.newLine();
			logger.flush();
			logger.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.release();
		}
	}

	public void showSendDialog() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				final Builder builder = new AlertDialog.Builder(Logger.this.context);
				builder.setTitle("Log file written");
				builder.setMessage("Log file is located at " + Logger.this.logFile.getAbsolutePath());
				builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final Intent intent = new Intent(Intent.ACTION_SENDTO);
						intent.setData(Uri.parse("mailto:"));
						intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "dunedeveloping@gmail.com" });
						intent.putExtra(Intent.EXTRA_SUBJECT, "SwiftSMS Logs");
						intent.putExtra(Intent.EXTRA_TEXT, "Find attached my log file.");
						final Uri uri = Uri.parse("file://" + Logger.this.logFile);
						intent.putExtra(Intent.EXTRA_STREAM, uri);

						Logger.this.context.startActivity(Intent.createChooser(intent, "Send Email"));
					}
				});
				builder.setNegativeButton("Cancel", null);
				builder.show();
			}
		});
	}
}