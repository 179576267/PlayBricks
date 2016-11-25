package com.wangzhenfei.cocos2dgame.tool;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 异步下载网络图片，并且显示在imageview上。   
 * need表示是否需要模糊效果
 * @author wzf
 *
 */
public abstract class AsyTaskForLoadNetPicture extends
		AsyncTask<String, integer, Bitmap> {

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[1024];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}
	private Context mContext;

	public AsyTaskForLoadNetPicture() {
		super();
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		if(params[0]==null){
			return null;
		}
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new URL(params[0]).openStream(),
					2 * 1024);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, 2 * 1024);
			copy(in, out);
			out.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bitmap;
		} catch (IOException e) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		onResult(result);
	}

	public abstract void onResult(Bitmap bitmap);

}
