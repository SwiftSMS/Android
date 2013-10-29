package com.icc.tasks;

import static com.icc.Notifications.FAILURE_NOTIFICATION;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.Notification.Style;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
			Toast.makeText(this.activity, "Message sent!", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this.activity, "Message failed to send!", Toast.LENGTH_LONG).show();

			final Notification notif = this.buildFailureNotification();
			final NotificationManager service = (NotificationManager) this.activity
					.getSystemService(Context.NOTIFICATION_SERVICE);
			service.notify(FAILURE_NOTIFICATION, notif);
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Notification buildFailureNotification() {
		final Builder builder = new Notification.Builder(this.activity);
		builder.setContentTitle("Message failed to send");
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(this.buildFailureIntent());
		final String message = "Recipient(s): " + this.recipients;
		builder.setContentText(message);
		builder.setStyle(this.buildFailureNotificationStyle(message));
		Notification notif = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notif = builder.build();
		} else {
			notif = builder.getNotification();
		}
		return notif;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Style buildFailureNotificationStyle(final String message) {
		final BigTextStyle style = new Notification.BigTextStyle();
		style.setSummaryText(message);
		style.bigText(this.message);
		return style;
	}

	private PendingIntent buildFailureIntent() {
		final Intent intent = new Intent(this.activity, ComposeActivity.class);
		intent.setData(Uri.parse("smsto:" + this.recipients));
		intent.putExtra("sms_body", this.message);
		return PendingIntent.getActivity(this.activity, FAILURE_NOTIFICATION, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
	}
}