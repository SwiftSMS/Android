package org.swiftsms;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private final Context context;
    private final List<Message> messages;
    private final LayoutInflater inflater;

    public MessageAdapter(final Context context, final List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(final int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Message message = getItem(position);
        final View layout = getView(convertView, message.type);
        final TextView text = (TextView) layout.findViewById(R.id.message_body);
        final TextView date = (TextView) layout.findViewById(R.id.message_date);

        text.setText(message.message);
        date.setText(DateFormat.format("dd MMM", message.date));

        return layout;
    }

    private View getView(final View convertView, final int type) {
        View layout = convertView;

        if (layout == null) {
            layout = inflater.inflate(R.layout.message_list_item, null);
        }

        if (type == 1) {
            layout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        } else {
            layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        return layout;
    }

}