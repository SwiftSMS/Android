package org.swiftsms.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.swiftsms.R;
import org.swiftsms.models.Conversation;

import java.util.List;

import static org.swiftsms.sms.SmsReceiver.SMS_READ;
import static org.swiftsms.sms.SmsReceiver.SMS_UNREAD;

public class ConversationAdapter extends BaseAdapter {

    private Context context;
    private List<Conversation> conversations;

    public ConversationAdapter(final Context context, final List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public Conversation getItem(final int position) {
        return conversations.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View layout = getView(convertView);
        final Conversation conversation = getItem(position);
        final TextView title = (TextView) layout.findViewById(R.id.contact_name);
        final TextView snippet = (TextView) layout.findViewById(R.id.message_snippet);

        title.setText(conversation.number);
        snippet.setText(conversation.body);

        if (conversation.read == SMS_UNREAD) {
            title.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            title.setTypeface(Typeface.DEFAULT);
        }

//        final ImageView image = (ImageView) layout.findViewById(R.id.contact_thumb);
//        image.setImageBitmap(meta.mThumb);

        return layout;
    }

    private View getView(final View convertView) {
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.conversation_list_item, null);
        } else {
            return convertView;
        }
    }

}