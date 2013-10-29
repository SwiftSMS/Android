package com.icc.tasks;

import static com.icc.InternalString.COLON_SPACE;
import static com.icc.InternalString.SMSTO;
import static com.icc.InternalString.SMS_BODY;
import static com.icc.InternalString.SMS_PROVIDER_MESSAGE_ADDRESS;
import static com.icc.InternalString.SMS_PROVIDER_MESSAGE_BODY;
import static com.icc.InternalString.SMS_PROVIDER_SENTBOX_URI;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.icc.R;
import com.icc.net.Operator;
import com.icc.utils.MultipleContactUtilities;
import com.icc.view.ComposeActivity;

/**
 * This class is an {@link AsyncTask} responsible for sending a web text using the provided operator.
 */
public class SendTask extends AsyncTask<String, Integer, Boolean> {

	public static int FAILURE_NOTIFICATION = 127;

	private final EditText messageEditText;
	private final ProgressBar progressBar;
	private final EditText recipientsEditText;
	private final Context activity;
	private final Operator operator;

	private String recipients;
	private String message;

	/**
	 * Create a new instance of the sending Task.
	 * 
	 * @param activity
	 *            The compose activity sending the message. Used to get the UI elements.
	 * @param operator
	 *            The network operator used to send the message.
	 */
	public SendTask(final Activity activity, final Operator operator) {
		this.activity = activity;
		this.operator = operator;
		this.messageEditText = (EditText) activity.findViewById(R.id.text_compose_message);
		this.progressBar = (ProgressBar) activity.findViewById(R.id.progressbar_compose);
		this.recipientsEditText = (EditText) activity.findViewById(R.id.text_compose_recipients);
	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected Boolean doInBackground(final String... params) {
		this.message = this.messageEditText.getText().toString();
		this.recipients = MultipleContactUtilities.trimSeparators(this.recipientsEditText.getText().toString());
		if (this.operator.login()) {
			return this.operator.send(MultipleContactUtilities.getEnteredContactsAsList(this.recipients), this.message);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean result) {
		this.progressBar.setVisibility(View.GONE);
		if (result) {
			Toast.makeText(this.activity, this.getStringRes(R.string.message_sent), Toast.LENGTH_LONG).show();
			this.insertMessageInSmsDb();
		} else {
			Toast.makeText(this.activity, this.getStringRes(R.string.message_failed_to_send), Toast.LENGTH_LONG).show();

			final Notification notif = this.buildFailureNotification();
			final NotificationManager service = (NotificationManager) this.activity
					.getSystemService(Context.NOTIFICATION_SERVICE);
			service.notify(++FAILURE_NOTIFICATION, notif);
		}
	}

	/**
	 * Take the data from this {@link SendTask} and insert into the Android SMS provider.
	 */
	private void insertMessageInSmsDb() {
		for (final String recipient : MultipleContactUtilities.getEnteredContactsAsList(this.recipients)) {
			final ContentValues values = new ContentValues();
			values.put(SMS_PROVIDER_MESSAGE_ADDRESS, recipient);
			values.put(SMS_PROVIDER_MESSAGE_BODY, this.message);
			this.activity.getContentResolver().insert(Uri.parse(SMS_PROVIDER_SENTBOX_URI), values);
		}
	}

	/**
	 * Build up an Android notification for this {@link SendTask} to inform the user of a message that failed to send.
	 * 
	 * @return A message send failure notification.
	 */
	private Notification buildFailureNotification() {
		final Builder builder = new Notification.Builder(this.activity);
		builder.setContentTitle(this.getStringRes(R.string.message_failed_to_send));
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setVibrate(new long[] { 10, 200 });
		builder.setContentIntent(this.buildFailureIntent());
		builder.setAutoCancel(true);
		final String message = this.getStringRes(R.string.to) + COLON_SPACE + this.recipients;
		builder.setContentText(message);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			this.setNotificationStyle(builder, message);
			return this.buildJB(builder);
		} else {
			return this.build(builder);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setNotificationStyle(final Builder builder, final String message) {
		final BigTextStyle style = new Notification.BigTextStyle();
		style.setSummaryText(message);
		style.bigText(this.message);

		builder.setStyle(style);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Notification buildJB(final Notification.Builder builder) {
		return builder.build();
	}

	@SuppressWarnings("deprecation")
	private Notification build(final Notification.Builder builder) {
		return builder.getNotification();
	}

	private PendingIntent buildFailureIntent() {
		final Intent intent = new Intent(this.activity, ComposeActivity.class);
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