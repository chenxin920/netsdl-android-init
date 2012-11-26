package com.netsdl.android.init.dialog.progress.store;

import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;
import android.os.Message;

public class StoreProgressHandler extends Handler {
	StoreProgressDialog storeProgressDialog;
	StoreProgressThread storeProgressThread;
	
	public StoreProgressThread getStoreProgressThread() {
		return storeProgressThread;
	}

	public void setStoreProgressThread(
			StoreProgressThread storeProgressThread) {
		this.storeProgressThread = storeProgressThread;
	}

	public StoreProgressHandler(
			StoreProgressDialog storeProgressDialog) {
		this.storeProgressDialog = storeProgressDialog;
	}

	@Override
	public void handleMessage(Message msg) {

		int progress = msg.arg1;
		storeProgressDialog.progressDialog.setProgress(progress);

		if (progress >= storeProgressDialog.progressDialog.getMax()) {
			storeProgressDialog.activity.dismissDialog(storeProgressDialog.hashCode());
			storeProgressThread.setState(AbstractProgressThread.STATE_DONE);
		}

		super.handleMessage(msg);
	}

}
