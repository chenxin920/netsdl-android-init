package com.netsdl.android.init.view;

import java.util.HashMap;
import java.util.Map;

import com.netsdl.android.common.db.DbMaster;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.db.PosTable;
import com.netsdl.android.common.db.SkuMaster;
import com.netsdl.android.common.db.StoreMaster;
import com.netsdl.android.common.view.dialog.Dialogable;
import com.netsdl.android.init.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public class InitActivity extends Activity {

	public Map<Integer, Dialogable> mapDialogable;

	public Map<Integer, Object[]> mapSkuMaster;

	public Map<Integer, Object[]> mapStoreMaster;

	public Map<Integer, Object[]> mapPaymentMaster;

	public DbMaster dbMaster = null;

	public SkuMaster skuMaster = null;

	public StoreMaster storeMaster = null;

	public PaymentMaster paymentMaster = null;

	public PosTable posTable = null;

	public Init init = null;

	public InitActivity() {
		
		mapDialogable = new HashMap<Integer, Dialogable>();

		mapStoreMaster = new HashMap<Integer, Object[]>();
		mapSkuMaster = new HashMap<Integer, Object[]>();
		mapPaymentMaster = new HashMap<Integer, Object[]>();

		dbMaster = new DbMaster(this);
		skuMaster = new SkuMaster(this);
		storeMaster = new StoreMaster(this);
		paymentMaster = new PaymentMaster(this);
		posTable = new PosTable(this);

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.init);
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