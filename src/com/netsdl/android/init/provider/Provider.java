package com.netsdl.android.init.provider;

import java.lang.reflect.Field;

import com.netsdl.android.common.Constant;
import com.netsdl.android.common.db.DatabaseHandler;
import com.netsdl.android.common.db.DatabaseHelper;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.db.SkuMaster;
import com.netsdl.android.common.db.StoreMaster;
import com.netsdl.android.init.data.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class Provider extends ContentProvider {

	private static final Class<?>[] clazzes = new Class<?>[] {
			StoreMaster.class, PaymentMaster.class, SkuMaster.class };
	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		for (Class<?> clazz : clazzes) {
			try {
				URI_MATCHER.addURI(Constant.PROVIDER_AUTHORITY, (String) clazz
						.getField(Constant.TABLE_NAME).get(clazz), clazz
						.hashCode());
			} catch (IllegalArgumentException e) {
			} catch (SecurityException e) {
			} catch (IllegalAccessException e) {
			} catch (NoSuchFieldException e) {
			}
		}
	}

	Data data;

	@Override
	public boolean onCreate() {
		data = Data.getInstance(getContext());
		Log.d("Provider", "onCreate");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String type = getType(uri);
		if (type == null)
			return null;
		Field[] fields = data.getClass().getFields();
		for (Field field : fields) {
			if (type.equals(field.getType().getName())) {
				try {
					DatabaseHandler databaseHandler = (DatabaseHandler) field
							.get(data);
					SQLiteDatabase db = databaseHandler.getReadableDatabase();
					String[] strs = new String[] { null, null, null, null };
					if (sortOrder != null) {
						strs = sortOrder.split(Constant.SEMICOLON);
						for (int i = 0; i < strs.length; i++) {
							strs[i] = strs[i].trim();
							if (strs[i].length() == 0)
								strs[i] = null;
						}
					}

					Cursor cursor = db.query(databaseHandler.getTableName(),
							databaseHandler.getColumns(), selection,
							selectionArgs, strs[0], strs[1], strs[2], strs[3]);

					Log.d("cursor Count", cursor.getCount() + "");
					return cursor;
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}

		}
		return null;

	}

	@Override
	public String getType(Uri uri) {
		int match = URI_MATCHER.match(uri);
		for (Class<?> clazz : clazzes) {
			if (clazz.hashCode() == match) {
				return clazz.getName();
			}
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		String type = getType(uri);
		if (type == null)
			return null;
		Field[] fields = data.getClass().getFields();
		for (Field field : fields) {
			if (type.equals(field.getType().getName())) {
				try {
					DatabaseHandler databaseHandler = (DatabaseHandler) field
							.get(data);
					SQLiteDatabase db = databaseHandler.getWritableDatabase();
					db.replace(databaseHandler.getTableName(), null, values);
					return uri;
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}

		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
