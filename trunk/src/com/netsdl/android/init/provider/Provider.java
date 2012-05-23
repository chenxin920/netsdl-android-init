package com.netsdl.android.init.provider;

import com.netsdl.android.common.db.DatabaseHelper;
import com.netsdl.android.init.data.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class Provider extends ContentProvider {
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
		Log.d("Provider", uri.toString());

		SQLiteDatabase db = data.storeMaster.getReadableDatabase();
		Cursor cursor = db.query(data.storeMaster.getTableName(),
				data.storeMaster.getColumns(),
				DatabaseHelper.getWhereClause(data.storeMaster.getKeys()),
				selectionArgs, null, null, null, null);
		
		
		Log.d("Provider", cursor.getCount() + "");

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		Log.d("getType", uri.toString());
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
