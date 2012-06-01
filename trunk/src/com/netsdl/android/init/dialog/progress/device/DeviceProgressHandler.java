package com.netsdl.android.init.dialog.progress.device;

import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;
import android.os.Message;

public class DeviceProgressHandler extends Handler {
	DeviceProgressDialog deviceProgressDialog;
	DeviceProgressThread deviceProgressThread;
	
	public DeviceProgressThread getDeviceProgressThread() {
		return deviceProgressThread;
	}

	public void setDeviceProgressThread(
			DeviceProgressThread deviceProgressThread) {
		this.deviceProgressThread = deviceProgressThread;
	}

	public DeviceProgressHandler(
			DeviceProgressDialog deviceProgressDialog) {
		this.deviceProgressDialog = deviceProgressDialog;
	}

	@Override
	public void handleMessage(Message msg) {

		int progress = msg.arg1;
		deviceProgressDialog.progressDialog.setProgress(progress);

		if (progress >= deviceProgressDialog.progressDialog.getMax()) {
			deviceProgressDialog.activity.dismissDialog(deviceProgressDialog.hashCode());
			deviceProgressThread.setState(AbstractProgressThread.STATE_DONE);
		}

		super.handleMessage(msg);
	}

}
