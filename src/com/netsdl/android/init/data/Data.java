package com.netsdl.android.init.data;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.netsdl.android.common.db.DbMaster;
import com.netsdl.android.common.db.DeviceMaster;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.db.PosTable;
import com.netsdl.android.common.db.SkuMaster;
import com.netsdl.android.common.db.StoreMaster;

public class Data {
	private static Data data;

	public Context context;

	public Map<Integer, Object[]> mapSkuMaster;

	public Map<Integer, Object[]> mapStoreMaster;

	public Map<Integer, Object[]> mapPaymentMaster;

	public DbMaster dbMaster = null;

	public SkuMaster skuMaster = null;

	public StoreMaster storeMaster = null;

	public PaymentMaster paymentMaster = null;

	public DeviceMaster deviceMaster = null;

	public PosTable posTable = null;

	private Data(Context context) {
		this.context = context;
		mapStoreMaster = new HashMap<Integer, Object[]>();
		mapSkuMaster = new HashMap<Integer, Object[]>();
		mapPaymentMaster = new HashMap<Integer, Object[]>();

		dbMaster = new DbMaster(context);
		skuMaster = new SkuMaster(context);
		storeMaster = new StoreMaster(context);
		paymentMaster = new PaymentMaster(context);
		deviceMaster = new DeviceMaster(context);
		posTable = new PosTable(context);
	}

	public static Data getInstance(Context context) {
		if (data == null) {
			data = new Data(context);
		}
		return data;
	}
}
