package com.netsdl.android.init.dialog.progress.commodity;

import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;
import android.os.Message;

public class CommodityProgressHandler extends Handler {
	CommodityProgressDialog commodityProgressDialog;
	CommodityProgressThread commodityProgressThread;
	
	public CommodityProgressThread getCommodityProgressThread() {
		return commodityProgressThread;
	}

	public void setCommodityProgressThread(
			CommodityProgressThread commodityProgressThread) {
		this.commodityProgressThread = commodityProgressThread;
	}

	public CommodityProgressHandler(
			CommodityProgressDialog commodityProgressDialog) {
		this.commodityProgressDialog = commodityProgressDialog;
	}

	@Override
	public void handleMessage(Message msg) {

		int progress = msg.arg1;
		commodityProgressDialog.progressDialog.setProgress(progress);

		if (progress >= commodityProgressDialog.progressDialog.getMax()) {
			commodityProgressDialog.activity.dismissDialog(commodityProgressDialog.hashCode());
			commodityProgressThread.setState(AbstractProgressThread.STATE_DONE);
		}

		super.handleMessage(msg);
	}

}
