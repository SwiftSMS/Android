package com.icc.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

	private final ImageView imageView;

	public DownloadImageTask(final ImageView imageView) {
		this.imageView = imageView;
	}

	@Override
	protected Bitmap doInBackground(final String... params) {
		try {
			final InputStream is = new URL(params[0]).openStream();
			return BitmapFactory.decodeStream(is);
		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(final Bitmap result) {
		this.imageView.setImageBitmap(result);
	}

}
