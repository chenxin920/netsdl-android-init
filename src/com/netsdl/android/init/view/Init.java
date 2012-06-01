package com.netsdl.android.init.view;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
//import org.ksoap2.SoapEnvelope;
//import org.ksoap2.serialization.SoapObject;
//import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.HttpTransportSE;

import com.netsdl.android.common.Constant;
import com.netsdl.android.common.Util;
import com.netsdl.android.common.db.DatabaseHelper;
import com.netsdl.android.common.db.DbMaster;
import com.netsdl.android.common.db.DeviceMaster;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.db.SkuMaster;
import com.netsdl.android.common.db.StoreMaster;
import com.netsdl.android.common.dialog.progress.AbstractProgressThread;
import com.netsdl.android.common.dialog.progress.CommonProgressDialog;
import com.netsdl.android.init.R;
import com.netsdl.android.init.dialog.progress.commodity.CommodityProgressDialog;
import com.netsdl.android.init.dialog.progress.commodity.CommodityProgressHandler;
import com.netsdl.android.init.dialog.progress.commodity.CommodityProgressThread;
import com.netsdl.android.init.dialog.progress.device.DeviceProgressDialog;
import com.netsdl.android.init.dialog.progress.device.DeviceProgressHandler;
import com.netsdl.android.init.dialog.progress.device.DeviceProgressThread;
import com.netsdl.android.init.dialog.progress.payment.PaymentProgressDialog;
import com.netsdl.android.init.dialog.progress.payment.PaymentProgressHandler;
import com.netsdl.android.init.dialog.progress.payment.PaymentProgressThread;
import com.netsdl.android.init.dialog.progress.store.StoreProgressDialog;
import com.netsdl.android.init.dialog.progress.store.StoreProgressHandler;
import com.netsdl.android.init.dialog.progress.store.StoreProgressThread;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Init {
	public static final String KEY_SKU = "sku";
	public static final String STORE = "store";
	public static final String PAYMENT = "payment";
	public static final String DEVICE = "device";

	public static final int LAYOUT_INIT = R.layout.init;
	InitActivity parent;
	public Map<String, String> initInfo;
	public Map<String, String> infoSku;
	public Map<String, String> infoStore;
	public Map<String, String> infoPayment;
	public Map<String, String> infoDevice;

	public Init(InitActivity parent) {
		this.parent = parent;
		initInfo = new HashMap<String, String>();
		infoSku = new HashMap<String, String>();
		infoStore = new HashMap<String, String>();
		infoPayment = new HashMap<String, String>();
		infoDevice = new HashMap<String, String>();

	}

	public void init() {
		parent.setContentView(LAYOUT_INIT);
		setVersion();
		initButtonNext();
		initButtonCheckDB();
		initButtonUpdateCommodity();
		initButtonUpdateStore();
		initButtonUpdatePayment();
		initButtonUpdateDevice();

		((Button) parent.findViewById(R.id.buttonNext)).setEnabled(true);
	}

	private void setVersion() {
		boolean canNext = setSkuVersion();
		canNext &= setStoreVersion();
		canNext &= setPaymentVersion();
		canNext &= setDeviceVersion();
		((Button) parent.findViewById(R.id.buttonNext)).setEnabled(canNext);
	}

	private boolean setSkuVersion() {
		return setVersion(SkuMaster.class, R.id.CommodityDataLocalVersion);
	}

	private boolean setStoreVersion() {
		return setVersion(StoreMaster.class, R.id.StoreDataLocalVersion);
	}

	private boolean setPaymentVersion() {
		return setVersion(PaymentMaster.class, R.id.PaymentDataLocalVersion);
	}

	private boolean setDeviceVersion() {
		return setVersion(DeviceMaster.class, R.id.DeviceDataLocalVersion);
	}

	private boolean setVersion(Class<?> clazz, int rid) {
		try {
			Object[] objs;
			objs = parent.data.dbMaster
					.getSingleColumn(new Object[] { (String) clazz.getField(
							Constant.TABLE_NAME).get(clazz) });
			String version = (String) DatabaseHelper.getColumnValue(objs,
					DbMaster.COLUMN_VERSION, DbMaster.COLUMNS);
			if (version != null) {
				((TextView) parent.findViewById(rid)).setText(version);
				return true;
			} else {
				return false;
			}
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		}
		return false;
	}

	private void initButtonNext() {
		((Button) parent.findViewById(R.id.buttonNext))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						// Intent intent = new Intent();
						// intent.setClassName("com.netsdl.android.main.view",
						// "com.netsdl.android.main.view.MainActivity");
						// parent.startActivity(intent);

						// Intent i = new
						// Intent("com.netsdl.android.intent.ACTION_VIEW");
						// i.addCategory(Intent.CATEGORY_DEFAULT);
						// parent.startActivity(i);

						// Intent intent = new Intent();
						// intent.setClassName( "com.netsdl.android.main.view" ,
						// "com.netsdl.android.main.view.MainActivity" );
						// parent.startActivity(intent);

						Log.d("button1", "onClick");
						// SoapObject rpc = new SoapObject(
						// "http://print.web.netsdl.com/", "test");
						// rpc.addProperty("str", "aaab");
						// SoapSerializationEnvelope envelope = new
						// SoapSerializationEnvelope(
						// SoapEnvelope.VER10);
						//
						// envelope.bodyOut = rpc;
						// envelope.dotNet = true;
						// envelope.setOutputSoapObject(rpc);
						//
						// HttpTransportSE ht = new HttpTransportSE(
						// "http://10.0.2.2:8080/NetSDL_WebPrint/wsdl/Util.wsdl");
						// ht.debug = true;
						// try {
						//
						// ht.call("http://print.web.netsdl.com/test",
						// envelope);
						//
						// SoapObject result = (SoapObject) envelope
						// .getResponse();
						//
						// System.out.println("result " + result);
						//
						// Toast.makeText(parent, result.toString(),
						// Toast.LENGTH_LONG).show();
						//
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						//
					}

				});
	}

	private void initButtonCheckDB() {

		((Button) parent.findViewById(R.id.buttonCheckDB))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						try {
							// String strLocalMacAddress =
							// Util.getLocalMacAddress(parent);
							// Log.d("LocalMacAddress",strLocalMacAddress);

							String strLocalDeviceId = Util
									.getLocalDeviceId(parent);
							Log.d("LocalDeviceId", strLocalDeviceId);

							initInfo = Util.getInitInfo();

							infoSku = Util.getInfo(initInfo.get(Init.KEY_SKU));

							infoStore = Util.getInfo(initInfo.get(Init.STORE));

							infoPayment = Util.getInfo(initInfo
									.get(Init.PAYMENT));

							infoDevice = Util.getInfo(initInfo.get(Init.DEVICE));

							((TextView) parent
									.findViewById(R.id.CommodityDataHostVersion))
									.setText(infoSku.get(Constant.VERSION));
							((Button) parent
									.findViewById(R.id.buttonUpdateCommodity))
									.setEnabled(true);

							((TextView) parent
									.findViewById(R.id.StoreDataHostVersion))
									.setText(infoStore.get(Constant.VERSION));
							((Button) parent
									.findViewById(R.id.buttonUpdateStore))
									.setEnabled(true);

							((TextView) parent
									.findViewById(R.id.PaymentDataHostVersion))
									.setText(infoPayment.get(Constant.VERSION));
							((Button) parent
									.findViewById(R.id.buttonUpdatePayment))
									.setEnabled(true);

							((TextView) parent
									.findViewById(R.id.DeviceDataHostVersion))
									.setText(infoDevice.get(Constant.VERSION));
							((Button) parent
									.findViewById(R.id.buttonUpdateDevice))
									.setEnabled(true);

						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (URISyntaxException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				});
	}

	private void initButtonUpdateCommodity() {
		((Button) parent.findViewById(R.id.buttonUpdateCommodity))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final CommodityProgressDialog commodityProgressDialog = new CommodityProgressDialog(
								parent);
						CommodityProgressHandler commodityProgressHandler = new CommodityProgressHandler(
								commodityProgressDialog);
						final CommodityProgressThread commodityProgressThread = new CommodityProgressThread(
								commodityProgressHandler);
						commodityProgressThread
								.setSkuMaster(parent.data.skuMaster);
						commodityProgressThread.setUrl(infoSku
								.get(Constant.URL));
						commodityProgressHandler
								.setCommodityProgressThread(commodityProgressThread);
						commodityProgressDialog
								.setProgressThread(commodityProgressThread);

						int rows;
						try {
							rows = Integer.parseInt(infoSku.get(Constant.ROWS));
						} catch (NumberFormatException nfe) {
							rows = 100;
						}
						commodityProgressDialog.setProgressMax(rows);

						commodityProgressDialog
								.setOnDismissListener(new OnDismissListener() {
									public void onDismiss(DialogInterface dialog) {
										try {
											if (commodityProgressThread.mState == AbstractProgressThread.STATE_DONE) {

												parent.data.dbMaster
														.deleteByKey(new String[] { infoSku
																.get(Constant.VERSION) });

												parent.data.dbMaster.insert(new String[] {
														SkuMaster.TABLE_NAME,
														infoSku.get(Constant.VERSION) });

												setVersion();
//												setSkuVersion();
//												if (setStoreVersion()
//														&& setPaymentVersion()
//														&& setDeviceVersion()) {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(true);
//												} else {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(false);
//												}

											}

										} catch (IllegalArgumentException e) {
										} catch (SecurityException e) {
										} catch (IllegalAccessException e) {
										} catch (NoSuchFieldException e) {
										}

									}
								});

						parent.mapDialogable.put(
								commodityProgressDialog.hashCode(),
								commodityProgressDialog);
						parent.showDialog(commodityProgressDialog.hashCode());

						Log.d("Init", "parent.showDialog");

					}
				});
	}

	private void initButtonUpdateStore() {
		((Button) parent.findViewById(R.id.buttonUpdateStore))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final StoreProgressDialog storeProgressDialog = new StoreProgressDialog(
								parent);
						StoreProgressHandler storeProgressHandler = new StoreProgressHandler(
								storeProgressDialog);
						final StoreProgressThread storeProgressThread = new StoreProgressThread(
								storeProgressHandler);
						storeProgressThread
								.setStoreMaster(parent.data.storeMaster);
						storeProgressThread.setUrl(infoStore.get(Constant.URL));
						storeProgressHandler
								.setStoreProgressThread(storeProgressThread);
						storeProgressDialog
								.setProgressThread(storeProgressThread);

						int rows;
						try {
							rows = Integer.parseInt(infoStore
									.get(Constant.ROWS));
						} catch (NumberFormatException nfe) {
							rows = 100;
						}
						storeProgressDialog.setProgressMax(rows);

						storeProgressDialog
								.setOnDismissListener(new OnDismissListener() {
									public void onDismiss(DialogInterface dialog) {
										try {
											if (storeProgressThread.mState == AbstractProgressThread.STATE_DONE) {
												parent.data.dbMaster
														.deleteByKey(new String[] { infoStore
																.get(Constant.VERSION) });
												parent.data.dbMaster.insert(new String[] {
														StoreMaster.TABLE_NAME,
														infoStore
																.get(Constant.VERSION) });

												setVersion();
//												setStoreVersion();
//												if (setSkuVersion()
//														&& setPaymentVersion()) {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(true);
//												} else {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(false);
//												}
											}

										} catch (IllegalArgumentException e) {
										} catch (SecurityException e) {
										} catch (IllegalAccessException e) {
										} catch (NoSuchFieldException e) {
										}

									}
								});

						parent.mapDialogable.put(
								storeProgressDialog.hashCode(),
								storeProgressDialog);
						parent.showDialog(storeProgressDialog.hashCode());

						Log.d("Init", "parent.showDialog");

					}
				});
	}

	private void initButtonUpdatePayment() {
		((Button) parent.findViewById(R.id.buttonUpdatePayment))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final PaymentProgressDialog paymentProgressDialog = new PaymentProgressDialog(
								parent);
						PaymentProgressHandler paymentProgressHandler = new PaymentProgressHandler(
								paymentProgressDialog);
						final PaymentProgressThread paymentProgressThread = new PaymentProgressThread(
								paymentProgressHandler);
						paymentProgressThread
								.setPaymentMaster(parent.data.paymentMaster);
						paymentProgressThread.setUrl(infoPayment
								.get(Constant.URL));
						paymentProgressHandler
								.setPaymentProgressThread(paymentProgressThread);
						paymentProgressDialog
								.setProgressThread(paymentProgressThread);

						int rows;
						try {
							rows = Integer.parseInt(infoPayment
									.get(Constant.ROWS));
						} catch (NumberFormatException nfe) {
							rows = 100;
						}
						paymentProgressDialog.setProgressMax(rows);

						paymentProgressDialog
								.setOnDismissListener(new OnDismissListener() {
									public void onDismiss(DialogInterface dialog) {
										try {
											if (paymentProgressThread.mState == AbstractProgressThread.STATE_DONE) {
												parent.data.dbMaster
														.deleteByKey(new String[] { infoPayment
																.get(Constant.VERSION) });

												parent.data.dbMaster.insert(new String[] {
														PaymentMaster.TABLE_NAME,
														infoPayment
																.get(Constant.VERSION) });

												setVersion();
//												setPaymentVersion();
//												if (setSkuVersion()
//														&& setStoreVersion()) {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(true);
//												} else {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(false);
//												}
											}

										} catch (IllegalArgumentException e) {
										} catch (SecurityException e) {
										} catch (IllegalAccessException e) {
										} catch (NoSuchFieldException e) {
										}

									}
								});

						parent.mapDialogable.put(
								paymentProgressDialog.hashCode(),
								paymentProgressDialog);
						parent.showDialog(paymentProgressDialog.hashCode());

						Log.d("Init", "parent.showDialog");

					}
				});
	}

	private void initButtonUpdateDevice() {
		((Button) parent.findViewById(R.id.buttonUpdateDevice))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final DeviceProgressDialog deviceProgressDialog = new DeviceProgressDialog(
								parent);
						DeviceProgressHandler deviceProgressHandler = new DeviceProgressHandler(
								deviceProgressDialog);
						final DeviceProgressThread deviceProgressThread = new DeviceProgressThread(
								deviceProgressHandler);
						deviceProgressThread
								.setDeviceMaster(parent.data.deviceMaster);
						deviceProgressThread.setUrl(infoDevice
								.get(Constant.URL));
						deviceProgressHandler
								.setDeviceProgressThread(deviceProgressThread);
						deviceProgressDialog
								.setProgressThread(deviceProgressThread);

						int rows;
						try {
							rows = Integer.parseInt(infoDevice
									.get(Constant.ROWS));
						} catch (NumberFormatException nfe) {
							rows = 100;
						}
						deviceProgressDialog.setProgressMax(rows);

						deviceProgressDialog
								.setOnDismissListener(new OnDismissListener() {
									public void onDismiss(DialogInterface dialog) {
										try {
											if (deviceProgressThread.mState == AbstractProgressThread.STATE_DONE) {
												parent.data.dbMaster
														.deleteByKey(new String[] { infoDevice
																.get(Constant.VERSION) });

												parent.data.dbMaster.insert(new String[] {
														DeviceMaster.TABLE_NAME,
														infoDevice
																.get(Constant.VERSION) });

												setVersion();
//												setDeviceVersion();
//												if (setSkuVersion()
//														&& setStoreVersion()) {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(true);
//												} else {
//													((Button) parent
//															.findViewById(R.id.buttonNext))
//															.setEnabled(false);
//												}
											}

										} catch (IllegalArgumentException e) {
										} catch (SecurityException e) {
										} catch (IllegalAccessException e) {
										} catch (NoSuchFieldException e) {
										}

									}
								});

						parent.mapDialogable.put(
								deviceProgressDialog.hashCode(),
								deviceProgressDialog);
						parent.showDialog(deviceProgressDialog.hashCode());

						Log.d("Init", "parent.showDialog");

					}
				});
	}

}