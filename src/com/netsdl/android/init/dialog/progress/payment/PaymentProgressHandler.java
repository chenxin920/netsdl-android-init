package com.netsdl.android.init.dialog.progress.payment;

import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;
import android.os.Message;

public class PaymentProgressHandler extends Handler {
	PaymentProgressDialog paymentProgressDialog;
	PaymentProgressThread paymentProgressThread;
	
	public PaymentProgressThread getPaymentProgressThread() {
		return paymentProgressThread;
	}

	public void setPaymentProgressThread(
			PaymentProgressThread paymentProgressThread) {
		this.paymentProgressThread = paymentProgressThread;
	}

	public PaymentProgressHandler(
			PaymentProgressDialog paymentProgressDialog) {
		this.paymentProgressDialog = paymentProgressDialog;
	}

	@Override
	public void handleMessage(Message msg) {

		int progress = msg.arg1;
		paymentProgressDialog.progressDialog.setProgress(progress);

		if (progress >= paymentProgressDialog.progressDialog.getMax()) {
			paymentProgressDialog.activity.dismissDialog(paymentProgressDialog.hashCode());
			paymentProgressThread.setState(AbstractProgressThread.STATE_DONE);
		}

		super.handleMessage(msg);
	}

}
