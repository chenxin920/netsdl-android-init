package com.netsdl.android.init.view;

import java.util.HashMap;
import java.util.Map;

import com.netsdl.android.common.view.dialog.Dialogable;
import com.netsdl.android.init.data.Data;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public class InitActivity extends Activity {

	public Map<Integer, Dialogable> mapDialogable;

	Data data = null;
	
	public Init init = null;

	public InitActivity() {
		mapDialogable = new HashMap<Integer, Dialogable>();
		data = Data.getInstance(this);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init = new Init(this);
		init.init();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialogable dialog = mapDialogable.get(id);
		if (dialog == null)
			return super.onCreateDialog(id);
		return dialog.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (mapDialogable.get(id) == null) {
			super.onPrepareDialog(id, dialog);
		} else {
			((Dialogable) mapDialogable.get(id)).onPrepareDialog(id, dialog);
		}
	}


}