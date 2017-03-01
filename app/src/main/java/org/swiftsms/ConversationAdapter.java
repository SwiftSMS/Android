package org.swiftsms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        final Conversation file = getItem(position);
        final TextView title = (TextView) layout.findViewById(R.id.contact_name);
        final TextView snippet = (TextView) layout.findViewById(R.id.message_snippet);

        title.setText(file.number);
        snippet.setText(file.body);

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