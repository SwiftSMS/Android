package org.swiftsms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.swiftsms.io.db.AccountDataSource;
import org.swiftsms.io.db.IAccountDatabase;
import org.swiftsms.io.net.Operator;
import org.swiftsms.io.net.OperatorFactory;
import org.swiftsms.models.Account;
import org.swiftsms.models.Network;
import org.swiftsms.tasks.results.Status;

import static org.swiftsms.InternalString.LOG_TAG;
import static org.swiftsms.InternalString.OPERATOR;
import static org.swiftsms.tasks.results.Status.SUCCESS;

public class AddAccountActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Network mNetwork;
    private IAccountDatabase mAccountDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        // Set up the login form.
        CookieSyncManager.createInstance(this);
        mNetwork = Network.values()[getIntent().getIntExtra(OPERATOR, -1)];
        mAccountDatabase = AccountDataSource.getInstance(this);
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(mEmailView.getText(), mPasswordView.getText());
            mAuthTask.execute((Void) null);
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        public UserLoginTask(final Editable username, final Editable password) {
            mUsername = String.valueOf(username);
            mPassword = String.valueOf(password);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final Account account = new Account(mUsername, "test", mPassword, mNetwork);
            final Operator operator = OperatorFactory.getOperator(account);

            if (operator.login().getStatus() == SUCCESS) {
                final int successfullyAddedId = mAccountDatabase.addAccount(account);
                Log.i(LOG_TAG, "Account added, ID is: " + successfullyAddedId);
                return true;
            }

            Log.i(LOG_TAG, "Failed to add new account, login status is " + operator.login().getStatus());
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_login_failed));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
