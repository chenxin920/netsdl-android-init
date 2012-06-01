package com.netsdl.android.init.dialog.progress.device;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.netsdl.android.common.db.DeviceMaster;
import com.netsdl.android.common.dialog.progress.AbstractProgressThread;

import android.os.Handler;

public class DeviceProgressThread extends AbstractProgressThread {
	private DeviceMaster deviceMaster;
	private String url;
	BufferedReader in;
	String line;

	public DeviceProgressThread(Handler handler) {
		super(handler);
	}

	public void before() throws Exception {
		deviceMaster.clear();
		if (url == null) {
			setState(STATE_CANCEL);
			return;
		}
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		request.setURI(new URI(url));
		HttpResponse response = client.execute(request);
		in = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent(), "UTF-8"));

		line = in.readLine();

	}

	public void proc() throws Exception {
		if (line == null) {
			setState(STATE_DONE);
			return;
		}

		if (line.trim().length() == 0)
			return;

		//deviceMaster.delete(line);
		deviceMaster.insert(line);

		line = in.readLine();

	}

	public void after() throws Exception {
		if (in != null)
			in.close();
	}

	public void setDeviceMaster(DeviceMaster deviceMaster) {
		this.deviceMaster = deviceMaster;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
