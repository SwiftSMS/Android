package org.swiftsms;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;

/**
 * Created by sean on 04/03/17.
 */
public class SendButtonTextWatcher implements TextWatcher {

    private final ImageButton button;

    public SendButtonTextWatcher(ImageButton button) {
        this.button = button;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(final Editable s) {
        final boolean isEmpty = s.length() > 0;
        button.setClickable(isEmpty);
        button.setEnabled(isEmpty);
    }
}
