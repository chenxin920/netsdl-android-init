package com.netsdl.android.init.dialog.progress.payment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.netsdl.android.common.Constant;
import com.netsdl.android.common.Util;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;

public class PaymentProgressThread extends AbstractProgressThread {
	private PaymentMaster paymentMaster;
	private String url;
	BufferedReader in;
	String line;

	public PaymentProgressThread(Handler handler) {
		super(handler);
	}

	public void before() throws Exception {
		paymentMaster.clear();
		if (url == null) {
			setState(STATE_CANCEL);
			return;
		}
		
		in = Util.getBufferedReaderFromURI(url,Constant.UTF_8);
		line = in.readLine();

	}

	public void proc() throws Exception {
		if (line == null) {
			setState(STATE_DONE);
			return;
		}

		if (line.trim().length() == 0)
			return;

		//paymentMaster.delete(line);
		paymentMaster.insert(line);

		line = in.readLine();

	}

	public void after() throws Exception {
		if (in != null)
			in.close();
	}

	public void setPaymentMaster(PaymentMaster paymentMaster) {
		this.paymentMaster = paymentMaster;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
