package com.netsdl.android.init.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import com.netsdl.android.common.Constant;
import com.netsdl.android.common.Util;
import com.netsdl.android.common.db.DatabaseHelper;
import com.netsdl.android.common.db.DbMaster;
import com.netsdl.android.common.db.DeviceMaster;
import com.netsdl.android.common.db.PaymentMaster;
import com.netsdl.android.common.db.PosTable;
import com.netsdl.android.common.db.SkuMaster;
import com.netsdl.android.common.db.StoreMaster;
import com.netsdl.android.common.dialog.progress.AbstractProgressThread;
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
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
		initButtonViewConfig();
		initButtonCheckDB();
		initButtonUpdateCommodity();
		initButtonUpdateStore();
		initButtonUpdatePayment();
		initButtonUpdateDevice();
		initUploadData();

		((Button) parent.findViewById(R.id.buttonViewConfig)).setEnabled(true);
	}

	private void setVersion() { 
		boolean canNext = setSkuVersion();
		canNext &= setStoreVersion();
		canNext &= setPaymentVersion();
		canNext &= setDeviceVersion();
		// ((Button)
		// parent.findViewById(R.id.buttonViewConfig)).setEnabled(canNext);
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

	private void initUploadData() {

		((Button) parent.findViewById(R.id.buttonUploadData))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Calendar now = Calendar.getInstance();
						now.setTimeInMillis(System.currentTimeMillis());
						String timestamp = sdf.format(now.getTime());

						String localDeviceId = Util.getLocalDeviceId(parent);
						String filepath = parent.getFilesDir().getPath()
								.toString();
						String filename = localDeviceId + "." + timestamp;

						PrintStream output = null;
						FileInputStream input = null;

						try {
							output = new PrintStream(filepath
									+ File.separatorChar + filename,
									Constant.UTF_8);
							//取未日结的数据
							Object[][] objss = parent.data.posTable
									.getMultiColumn(new String[]{"0"},
											new String[]{PosTable.COLUMN_END_DAY}, null, null,
											new String[]{PosTable.COLUMN_CREATE_DATE}, null, true);
							if (objss == null || objss.length == 0)
								return;
							for (int i = 0; i < objss.length; i++) {
								for (int j = 0; j < objss[i].length-1; j++) {
									output.print(objss[i][j]);
									if (j != objss[i].length - 2) {
										output.print(',');
									}
								}
								output.println();
								output.flush();
							}
							output.close();
							output = null;
							Object[] objs = parent.data.deviceMaster.getSingleColumn(
									new String[] { "9",
											Util.getLocalDeviceId(parent) },
									new String[] { DeviceMaster.COLUMN_INIT_ID,
											DeviceMaster.COLUMN_DEVICE_ID });
							if (objs != null) {
								String ftpUrl = DatabaseHelper.getColumnValue(
										objs, DeviceMaster.COLUMN_FIELD_13,
										DeviceMaster.COLUMNS).toString();
								String ftpPath = DatabaseHelper.getColumnValue(
										objs, DeviceMaster.COLUMN_FIELD_14,
										DeviceMaster.COLUMNS).toString();
								String ftpUser = DatabaseHelper.getColumnValue(
										objs, DeviceMaster.COLUMN_FIELD_15,
										DeviceMaster.COLUMNS).toString();
								String ftpPassword = DatabaseHelper
										.getColumnValue(objs,
												DeviceMaster.COLUMN_FIELD_16,
												DeviceMaster.COLUMNS)
										.toString();
								Util.ftpUpload(filepath, filename, ftpUrl,
										ftpPath, ftpUser, ftpPassword);

								//更新日结标记
								Map<String, Object> mapData = new HashMap<String, Object>();
								mapData.put(PosTable.COLUMN_END_DAY, 1);
								parent.data.posTable.update(mapData, new String[]{"0"}, new String[]{PosTable.COLUMN_END_DAY});
							}

						} catch (IllegalArgumentException e) {
						} catch (SecurityException e) {
						} catch (IllegalAccessException e) {
						} catch (NoSuchFieldException e) {
						} catch (FileNotFoundException e) {
						} catch (UnsupportedEncodingException e) {
						} finally {
							if (input != null) {
								try {
									input.close();
								} catch (IOException e) {
								}
								input = null;
							}
							if (output != null) {
								output.close();
								output = null;
							}

						}

					}
				});
	}

	private void initButtonViewConfig() {
		((Button) parent.findViewById(R.id.buttonViewConfig))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						popupConfigWindow();

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
							//Log.d("LocalDeviceId", strLocalDeviceId);

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
												// setSkuVersion();
												// if (setStoreVersion()
												// && setPaymentVersion()
												// && setDeviceVersion()) {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(true);
												// } else {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(false);
												// }

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
												// setStoreVersion();
												// if (setSkuVersion()
												// && setPaymentVersion()) {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(true);
												// } else {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(false);
												// }
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
												// setPaymentVersion();
												// if (setSkuVersion()
												// && setStoreVersion()) {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(true);
												// } else {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(false);
												// }
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
												// setDeviceVersion();
												// if (setSkuVersion()
												// && setStoreVersion()) {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(true);
												// } else {
												// ((Button) parent
												// .findViewById(R.id.buttonNext))
												// .setEnabled(false);
												// }
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

	private void popupConfigWindow() {
		LayoutInflater layoutInflater = (LayoutInflater) parent
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.config, null);

		((TextView) view.findViewById(R.id.deviceid)).setText(Util
				.getLocalDeviceId(parent));
		((TextView) view.findViewById(R.id.mac)).setText(Util
				.getLocalMacAddress(parent));
		((TextView) view.findViewById(R.id.ip)).setText(Util
				.getIpAddress(parent));

		// ((TextView) view.findViewById(R.id.ip)).setText(Util
		// .getIpAddress(parent));
		((TextView) view.findViewById(R.id.externalStorageDirectory))
				.setText(Util.ExternalStorageDirectory());

		final PopupWindow popupWindow = new PopupWindow(view, parent
				.getWindowManager().getDefaultDisplay().getWidth() / 2, parent
				.getWindowManager().getDefaultDisplay().getHeight() / 2);

		((Button) view.findViewById(R.id.close))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						popupWindow.dismiss();
					}
				});

		popupWindow.showAtLocation(parent.findViewById(R.id.init),
				Gravity.CENTER, 0, 0);
	}

}
