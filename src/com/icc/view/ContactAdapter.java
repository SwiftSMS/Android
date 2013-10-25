package com.icc.view;

import static com.icc.InternalString.CONTACT_SEPARATOR;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.icc.R;
import com.icc.model.Contact;

public class ContactAdapter extends BaseAdapter implements Filterable {

	private final LayoutInflater layoutInflater;
	private ArrayList<Contact> items;
	private final Context context;

	public ContactAdapter(final Context context) {
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = new ArrayList<Contact>();
	}

	@Override
	public int getCount() {
		return this.items.size();
	}

	@Override
	public Object getItem(final int position) {
		return this.items.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = this.layoutInflater.inflate(R.layout.contact_suggest_dropdown, null);
		}

		final TextView nameTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_name);
		final ImageView photoImageView = (ImageView) view.findViewById(R.id.image_contact_suggestion_photo);
		final TextView numberTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_number);
		final TextView numberTypeTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_label);
		nameTextView.setText(this.items.get(position).getName());
		photoImageView.setImageURI(this.items.get(position).getPhoto());
		numberTextView.setText(this.items.get(position).getNumber());
		numberTypeTextView.setText(this.items.get(position).getNumberType());

		return view;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				if (results.values != null) {
					ContactAdapter.this.items = (ArrayList<Contact>) results.values;
				} else {
					ContactAdapter.this.items = new ArrayList<Contact>();
				}
				if (results.count > 0) {
					ContactAdapter.this.notifyDataSetChanged();
				} else {
					ContactAdapter.this.notifyDataSetInvalidated();
				}
			}

			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				final String[] projection = new String[] { Contacts.DISPLAY_NAME, Contacts.PHOTO_THUMBNAIL_URI, Phone.NUMBER,
						Phone.TYPE, Phone.LABEL };
				final String selection = Data.MIMETYPE + " = ?";
				final String[] selectionArgs = new String[] { Phone.CONTENT_ITEM_TYPE };
				final String sortOrder = Contacts.DISPLAY_NAME + " ASC";

				String searchText = constraint.toString();
				if (searchText.contains(CONTACT_SEPARATOR)) {
					searchText = searchText.substring(searchText.lastIndexOf(CONTACT_SEPARATOR) + 1);
				}
				final Uri contentUri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(searchText));

				final ContentResolver resolver = ContactAdapter.this.context.getContentResolver();
				final Cursor c = resolver.query(contentUri, projection, selection, selectionArgs, sortOrder);

				final ArrayList<Contact> values = new ArrayList<Contact>();
				while (c.moveToNext()) {
					final String cName = c.getString(0);
					final String cPhoto = c.getString(1);
					final String cNumber = c.getString(2);
					final int cLabelType = c.getInt(3);
					final String cCustomLabel = c.getString(4);

					final Resources res = ContactAdapter.this.context.getResources();
					final String cNumberType = Phone.getTypeLabel(res, cLabelType, cCustomLabel).toString();

					values.add(new Contact(cName, cPhoto, cNumber, cNumberType));
				}

				final FilterResults result = new FilterResults();
				result.values = values;
				result.count = values.size();
				return result;
			}
		};
	}
}