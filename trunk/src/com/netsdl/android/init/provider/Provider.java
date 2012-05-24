package com.netsdl.android.init.provider;

import java.lang.reflect.Field;

import com.netsdl.android.common.Constant;
import com.netsdl.android.common.db.DatabaseHelper;
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

	private static final Class<?>[] clazzes = new Class<?>[] { StoreMaster.class };
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
		if(type==null)
			return null;
		Field[] fields = data.getClass().getFields();
		for (Field field : fields) {
			if(type.equals(field.getType().getName())){
				try {
					Log.d("aa", field.get(data).toString());
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
			
		}

		SQLiteDatabase db = data.storeMaster.getReadableDatabase();

		Cursor cursor = db.query(data.storeMaster.getTableName(),
				data.storeMaster.getColumns(),
				DatabaseHelper.getWhereClause(data.storeMaster.getKeys()),
				selectionArgs, null, null, null, null);

		return cursor;
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
		// TODO Auto-generated method stub
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
