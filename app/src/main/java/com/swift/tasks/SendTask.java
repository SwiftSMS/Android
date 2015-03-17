package com.swift.tasks;

import static com.swift.InternalString.COLON_SPACE;
import static com.swift.InternalString.EMPTY_STRING;
import static com.swift.InternalString.FAILED_TO_ENABLE_WRITE_PERMISSION;
import static com.swift.InternalString.LOG_TAG;
import static com.swift.InternalString.PREFS_KEY;
import static com.swift.InternalString.SMSTO;
import static com.swift.InternalString.SMS_BODY;
import static com.swift.InternalString.SMS_PROVIDER_FAILURE;
import static com.swift.InternalString.SMS_PROVIDER_MESSAGE_ADDRESS;
import static com.swift.InternalString.SMS_PROVIDER_MESSAGE_BODY;
import static com.swift.InternalString.SMS_PROVIDER_MESSAGE_STATUS;
import static com.swift.InternalString.SMS_PROVIDER_MESSAGE_STATUS_DELIVERED;
import static com.swift.InternalString.SMS_PROVIDER_SENTBOX_URI;
import static com.swift.InternalString.WRITE_SMS_ENABLED;
import static com.swift.tasks.Status.FAILED;
import static com.swift.tasks.Status.SUCCESS;

import java.lang.reflect.Method;
import java.util.List;

import org.lucasr.twowayview.TwoWayView;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.swift.R;
import com.swift.io.net.Operator;
import com.swift.tasks.results.OperationResult;
import com.swift.ui.view.ComposeActivity;
import com.swift.ui.view.util.RecentContactsAdapter;
import com.swift.utils.ContactUtils;

/**
 * This class is an {@link AsyncTask} responsible for sending a web text using the provided operator.
 */
public class SendTask extends AsyncTask<String, Integer, OperationResult> {

	private static int FAILURE_NOTIFICATION = 127;
	private static int SUCCESS_NOTIFICATION = 1;

	private final ComposeActivity activity;
	private final EditText messageEditText;
	private final EditText recipientsEditText;
	private final ProgressBar progressBar;
	private final ImageButton sendButton;
	private final RecentContactsAdapter recentAdapter;

	private String recipients;
	private String message;

	private final Operator operator;
	private final SharedPreferences preferences;
	private final NotificationManager notificationService;

	/**
	 * Create a new instance of the sending Task.
	 * 
	 * @param activity
	 *            The compose activity sending the message. Used to get the UI elements.
	 * @param operator
	 *            The network operator used to send the message.
	 */
	public SendTask(final ComposeActivity activity, final Operator operator) {
		this.activity = activity;
		this.operator = operator;
		this.messageEditText = (EditText) activity.findViewById(R.id.text_compose_message);
		this.sendButton = (ImageButton) activity.findViewById(R.id.button_compose_send);
		this.progressBar = (ProgressBar) activity.findViewById(R.id.progressbar_compose);
		this.recipientsEditText = (EditText) activity.findViewById(R.id.text_compose_recipients);
		this.preferences = activity.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		this.notificationService = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		final TwoWayView recentList = (TwoWayView) activity.findViewById(R.id.list_compose_recent);
		this.recentAdapter = (RecentContactsAdapter) recentList.getAdapter();
	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setVisibility(View.VISIBLE);
		this.sendButton.setEnabled(false);
		this.operator.preSend(this.activity);
	}

	@Override
	protected OperationResult doInBackground(final String... params) {
		this.message = this.messageEditText.getText().toString();
		this.recipients = ContactUtils.trimSeparators(this.recipientsEditText.getText().toString());
		final List<String> recipList = ContactUtils.getContactsAsList(this.recipients);
		return this.operator.send(recipList, this.message);
	}

	@Override
	protected void onPostExecute(final OperationResult result) {
		this.progressBar.setVisibility(View.GONE);
		this.sendButton.setEnabled(true);
		this.activity.addNotification(result);

		if (result.getStatus() == SUCCESS) {
			this.activity.retrieveRemainingSmsCount();
			this.recipientsEditText.setText(EMPTY_STRING);
			this.recipientsEditText.requestFocus();
			this.messageEditText.setText(EMPTY_STRING);
			this.insertMessageInSmsDb();
		}
		this.sendNotification(result.getStatus());
		this.recentAdapter.refresh();
	}

	/**
	 * Take the data from this {@link SendTask} and insert into the Android SMS provider.
	 */
	private void insertMessageInSmsDb() {
		this.kitKatEnableSMSWritePermission();

		for (final String recipient : ContactUtils.getContactsAsList(this.recipients)) {
			try {
				final ContentValues values = new ContentValues();
				values.put(SMS_PROVIDER_MESSAGE_ADDRESS, recipient);
				values.put(SMS_PROVIDER_MESSAGE_BODY, this.message);
				values.put(SMS_PROVIDER_MESSAGE_STATUS, SMS_PROVIDER_MESSAGE_STATUS_DELIVERED);
				final ContentResolver resolver = this.activity.getContentResolver();
				final Uri smsUri = Uri.parse(SMS_PROVIDER_SENTBOX_URI);
				resolver.insert(smsUri, values);
			} catch (final Exception e) {
				Log.e(LOG_TAG, SMS_PROVIDER_FAILURE, e);
			}
		}
	}

	/**
	 * <b>Muhahahahahahahahahahah</b>
	 * <p>
	 * Hack to enable WRITE_SMS permission on post KitKat devices.
	 * </p>
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void kitKatEnableSMSWritePermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			final boolean isWriteEnabled = this.preferences.getBoolean(WRITE_SMS_ENABLED, false);
			if (!isWriteEnabled) {
				try {
					final AppOpsManager mAppOps = (AppOpsManager) this.activity.getSystemService(Context.APP_OPS_SERVICE);
					final int WRITE_SMS = 15;

					final PackageManager mPm = this.activity.getPackageManager();
					final PackageInfo mPackageInfo = mPm.getPackageInfo(this.activity.getPackageName(), PackageManager.GET_ACTIVITIES);

					final Method method = mAppOps.getClass().getMethod("setMode", int.class, int.class, String.class, int.class);
					method.invoke(mAppOps, WRITE_SMS, mPackageInfo.applicationInfo.uid, mPackageInfo.packageName, AppOpsManager.MODE_ALLOWED);

					final Editor editor = this.preferences.edit();
					editor.putBoolean(WRITE_SMS_ENABLED, true);
					editor.apply();
				} catch (final Exception e) {
					Log.e(LOG_TAG, FAILED_TO_ENABLE_WRITE_PERMISSION, e);
				}
			}
		}
	}

	private void sendNotification(final com.swift.tasks.Status status) {
		if (!this.recipientsEditText.isShown()) {
			if (status == SUCCESS) {
				final Notification notif = this.buildSuccessNotification();
				this.notificationService.notify(SUCCESS_NOTIFICATION, notif);
				this.notificationService.cancel(SUCCESS_NOTIFICATION);
			} else if (status == FAILED) {
				final Notification notif = this.buildFailureNotification();
				this.notificationService.notify(++FAILURE_NOTIFICATION, notif);
			}
		}
	}

	private Notification buildSuccessNotification() {
		final Builder builder = new Notification.Builder(this.activity);
		builder.setTicker(this.getStringRes(R.string.message_sent));
		builder.setSmallIcon(R.drawable.ic_launcher_grey);
		return this.build(builder);
	}

	/**
	 * Build up an Android notification for this {@link SendTask} to inform the user of a message that failed to send.
	 * 
	 * @return A message send failure notification.
	 */
	private Notification buildFailureNotification() {
		final Builder builder = new Notification.Builder(this.activity);
		final String message = this.getStringRes(R.string.to) + COLON_SPACE + this.recipients;
		builder.setContentTitle(this.getStringRes(R.string.message_failed_to_send));
		builder.setSmallIcon(R.drawable.ic_launcher_grey);
		builder.setVibrate(new long[] { 10, 200 });
		builder.setContentIntent(this.buildFailureIntent());
		builder.setAutoCancel(true);
		builder.setContentText(message);
		this.setNotificationStyle(builder, message);
		return this.build(builder);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setNotificationStyle(final Builder builder, final String message) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			final BigTextStyle style = new Notification.BigTextStyle();
			style.setSummaryText(message);
			style.bigText(this.message);

			builder.setStyle(style);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	private Notification build(final Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return builder.build();
		} else {
			return builder.getNotification();
		}
	}

	private PendingIntent buildFailureIntent() {
		final Intent intent = new Intent(this.activity, ComposeActivity.class);
		intent.setAction(Intent.ACTION_SEND);
		intent.setData(Uri.parse(SMSTO + this.recipients));
		intent.putExtra(SMS_BODY, this.message);
		return PendingIntent.getActivity(this.activity, FAILURE_NOTIFICATION, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	/**
	 * Get the string value from strings.xml using activity resources.
	 * 
	 * @param id
	 *            The id of the resource to get.
	 * @return The string value of the resource.
	 */
	private String getStringRes(final int id) {
		return this.activity.getResources().getString(id);
	}
}