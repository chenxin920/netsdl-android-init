package com.netsdl.android.init.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import com.netsdl.android.init.ConfigProperties;
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
		initButtonSaleDate();
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
						AlertDialog.Builder builder = new Builder(parent);
						builder.setMessage("确认日结吗？");
						builder.setTitle("提示");
						builder.setPositiveButton("确认", new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								endDay();
							}
						});
						builder.setNegativeButton("取消", new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
						builder.create().show();
					}
				});
	}

	// 日结功能
	private void endDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		String timestamp = sdf.format(now.getTime());

		String localDeviceId = Util.getLocalDeviceId(parent);
		String filepath = parent.getFilesDir().getPath().toString();
		String filename = localDeviceId + "." + timestamp;

		PrintStream output = null;
		FileInputStream input = null;

		try {
			output = new PrintStream(filepath + File.separatorChar + filename,
					Constant.UTF_8);
			// 取未日结的数据
			Object[][] objss = parent.data.posTable.getMultiColumn(
					new String[] { "0" },
					new String[] { PosTable.COLUMN_END_DAY }, null, null,
					new String[] { PosTable.COLUMN_CREATE_DATE }, null, true);
			if (objss == null || objss.length == 0) {
				// 没数据营业日期也增加
				addSaleDate();
				Toast.makeText(parent, R.string.endDayMessage1, Toast.LENGTH_SHORT)
				.show();
				return;
			}
			for (int i = 0; i < objss.length; i++) {
				for (int j = 0; j < objss[i].length; j++) {
					//日结标记不导出
					if(j==DatabaseHelper.getColumnIndex(PosTable.COLUMN_END_DAY, PosTable.COLUMNS))
						continue;
					output.print(objss[i][j]);
					if (j != objss[i].length - 1) {
						output.print(',');
					}
				}
				output.println();
				output.flush();
			}
			output.close();
			output = null;
			Object[] objs = parent.data.deviceMaster.getSingleColumn(
					new String[] { "9", Util.getLocalDeviceId(parent) },
					new String[] { DeviceMaster.COLUMN_INIT_ID,
							DeviceMaster.COLUMN_DEVICE_ID });
			if (objs != null) {
				String ftpUrl = DatabaseHelper.getColumnValue(objs,
						DeviceMaster.COLUMN_FIELD_13, DeviceMaster.COLUMNS)
						.toString();
				String ftpPath = DatabaseHelper.getColumnValue(objs,
						DeviceMaster.COLUMN_FIELD_14, DeviceMaster.COLUMNS)
						.toString();
				String ftpUser = DatabaseHelper.getColumnValue(objs,
						DeviceMaster.COLUMN_FIELD_15, DeviceMaster.COLUMNS)
						.toString();
				String ftpPassword = DatabaseHelper.getColumnValue(objs,
						DeviceMaster.COLUMN_FIELD_16, DeviceMaster.COLUMNS)
						.toString();
				// 上传数据
				Util.ftpUpload(filepath, filename, ftpUrl, ftpPath, ftpUser,
						ftpPassword);

				// 更新日结标记
				Map<String, Object> mapData = new HashMap<String, Object>();
				mapData.put(PosTable.COLUMN_END_DAY, 1);
				parent.data.posTable.update(mapData, new String[] { "0" },
						new String[] { PosTable.COLUMN_END_DAY });
				// 备份数据
				bakup(filepath, filename);
				// 营业日期加1
				addSaleDate();
				//删除生成的文件
				new File(filepath + File.separatorChar + filename).delete();
				//删除销售数据
				//parent.data.posTable.deleteByKey(new String[] { PosTable.COLUMN_END_DAY },new String[] { "1" });
				Toast.makeText(parent, R.string.endDayMessage2, Toast.LENGTH_SHORT)
				.show();
			}

		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	//备份数据，并删除多余备份
	private void bakup(String filepath, String filename) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		Object[] deviceMasterObjs = DatabaseHelper.getSingleColumn(
				parent.getContentResolver(),
				new Object[] { "9", Util.getLocalDeviceId(parent) },
				DeviceMaster.class);
		String bakpath = (String) DatabaseHelper.getColumnValue(
				deviceMasterObjs, DeviceMaster.COLUMN_FIELD_02,
				DeviceMaster.COLUMNS);
		String bakday = (String) DatabaseHelper.getColumnValue(
				deviceMasterObjs, DeviceMaster.COLUMN_FIELD_03,
				DeviceMaster.COLUMNS);
		//无备份路径不备份文件，备份路径无法建立也不备份文件
		if(bakpath==null||bakpath.trim().length()==0)
			return;
		bakpath = bakpath.trim();
		File bakDir = new File(bakpath);
		try {
			if(!bakDir.exists())
				bakDir.mkdirs();
		} catch (Exception e) {
			return;
		}
		int copyFile = Util.CopyFile(filepath + File.separatorChar + filename,bakpath+ File.separatorChar + filename);
		//备份文件保留，默认30天
		if(bakday==null||bakday.trim().length()==0)
			bakday = "30";
		Integer bd = null;
		try {
			bd = new Integer(bakday);
		} catch (Exception e) {
			bd = new Integer(30);
		}
		File[] bakFiles = bakDir.listFiles();
		if(bakFiles!=null&&bakFiles.length>bd)
		{
			List<File> fl = new ArrayList();
			for(File f:bakFiles)
				fl.add(f);
			//排序
			Collections.sort(fl, new Comparator<File>() {

				public int compare(File o1, File o2) {
					return o1.getName().compareToIgnoreCase(o2.getName())*-1;
				}
			});
			//按日期和数量比较，大于bd的删除
			Map<String,String> fm = new HashMap();
			for(File f:fl)
			{
				String sn = f.getName().substring(f.getName().lastIndexOf(".")+1);
				String d = sn.substring(0,8);
				if(fm.get(d)!=null)
					continue;
				else
				{
					if(fm.size()>bd)
						f.delete();
					else
						fm.put(d, d);
				}
			}
		}
	}

	//营业日期增加
	private void addSaleDate() throws IllegalAccessException,
			NoSuchFieldException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Object[] deviceMasterObjs = DatabaseHelper.getSingleColumn(
				parent.getContentResolver(),
				new Object[] { "1", Util.getLocalDeviceId(parent) },
				DeviceMaster.class);
		String strDocumentDate = (String) DatabaseHelper.getColumnValue(
				deviceMasterObjs, DeviceMaster.COLUMN_FIELD_04,
				DeviceMaster.COLUMNS);
		Date date = null;
		if (strDocumentDate == null)
			date = new Date();
		else
			date = sdf.parse(strDocumentDate);
		date = new Date(date.getTime() + 24 * 60 * 60 * 1000);
		Map<String, Object> sd = new HashMap();
		sd.put(DeviceMaster.COLUMN_FIELD_04, sdf.format(date));
		parent.data.deviceMaster
		.update(sd,
				new String[] {
						"1",
						Util.getLocalDeviceId(parent) },
				new String[] {
						DeviceMaster.COLUMN_INIT_ID,
						DeviceMaster.COLUMN_DEVICE_ID });
	}

	private void initButtonViewConfig() {
		((Button) parent.findViewById(R.id.buttonViewConfig))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						popupConfigWindow();
					}

				});
	}

	private void initButtonSaleDate() {
		((Button) parent.findViewById(R.id.buttonSaleDate))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd");
						try {
							// 查询营业日期
							Object[] deviceMasterObjs = DatabaseHelper.getSingleColumn(
									parent.getContentResolver(),
									new Object[] { "1",
											Util.getLocalDeviceId(parent) },
									DeviceMaster.class);
							String strDocumentDate = (String) DatabaseHelper
									.getColumnValue(deviceMasterObjs,
											DeviceMaster.COLUMN_FIELD_04,
											DeviceMaster.COLUMNS);
							Date date = null;
							if (strDocumentDate == null||strDocumentDate.trim().length()==0)
								date = new Date();
							else
								date = sdf.parse(strDocumentDate);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);

							DatePickerDialog dd = new DatePickerDialog(parent,
									new OnDateSetListener() {

										public void onDateSet(DatePicker view,
												int year, int monthOfYear,
												int dayOfMonth) {
											Calendar calendar = Calendar
													.getInstance();
											calendar.set(Calendar.YEAR, year);
											calendar.set(Calendar.MONTH,
													monthOfYear);
											calendar.set(Calendar.DAY_OF_MONTH,
													dayOfMonth);
											String strDocumentDate = sdf
													.format(calendar.getTime());

											// 保存营业日期
											try {
												Map<String, Object> sd = new HashMap();
												sd.put(DeviceMaster.COLUMN_FIELD_04,
														strDocumentDate);
												parent.data.deviceMaster
														.update(sd,
																new String[] {
																		"1",
																		Util.getLocalDeviceId(parent) },
																new String[] {
																		DeviceMaster.COLUMN_INIT_ID,
																		DeviceMaster.COLUMN_DEVICE_ID });
											} catch (IllegalArgumentException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											} catch (SecurityException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											} catch (IllegalAccessException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											} catch (NoSuchFieldException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
									}, calendar.get(Calendar.YEAR), calendar
											.get(Calendar.MONTH), calendar
											.get(Calendar.DAY_OF_MONTH));
							dd.setTitle(R.string.saleDate);
							dd.show();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchFieldException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
	}

	//更新检查
	private void initButtonCheckDB() {

		((Button) parent.findViewById(R.id.buttonCheckDB))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						checkUpdate2();
					}
				});
	}

	private void checkUpdate2() {
		try {
			 clearUpdateInfo();
			// String strLocalMacAddress =
			// Util.getLocalMacAddress(parent);
			// Log.d("LocalMacAddress",strLocalMacAddress);

			String strLocalDeviceId = Util
					.getLocalDeviceId(parent);
			// Log.d("LocalDeviceId", strLocalDeviceId);

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
	
	//清空版本信息
	private void clearUpdateInfo()
	{
		initInfo.clear();
		infoSku.clear();
		infoStore.clear();
		infoPayment.clear();
		infoDevice.clear();
		((TextView) parent
				.findViewById(R.id.CommodityDataLocalVersion))
				.setText("");
		((TextView) parent
				.findViewById(R.id.CommodityDataHostVersion))
				.setText("");
		((Button) parent
				.findViewById(R.id.buttonUpdateCommodity))
				.setEnabled(false);

		((TextView) parent
				.findViewById(R.id.StoreDataLocalVersion))
				.setText("");
		((TextView) parent
				.findViewById(R.id.StoreDataHostVersion))
				.setText("");
		((Button) parent
				.findViewById(R.id.buttonUpdateStore))
				.setEnabled(false);

		((TextView) parent
				.findViewById(R.id.PaymentDataLocalVersion))
				.setText("");
		((TextView) parent
				.findViewById(R.id.PaymentDataHostVersion))
				.setText("");
		((Button) parent
				.findViewById(R.id.buttonUpdatePayment))
				.setEnabled(false);

		((TextView) parent
				.findViewById(R.id.DeviceDataLocalVersion))
				.setText("");
		((TextView) parent
				.findViewById(R.id.DeviceDataHostVersion))
				.setText("");
		((Button) parent
				.findViewById(R.id.buttonUpdateDevice))
				.setEnabled(false);
	}
	
	private void checkUpdate() {
		 clearUpdateInfo();
		//通过Config配置得到店铺数据的更新位置URL
		String url = ConfigProperties.getProperties("pos.config.url");
		if(url==null||url.trim().length()==0)
		{
			Toast.makeText(parent, R.string.configUrlNoFind, Toast.LENGTH_SHORT)
			.show();
			return;
		}
		//URL下固定路径，得到device_update.txt、payment_update.txt、sku_update.txt、store_update.txt配置信息
		String configUrl = getUpdateConfigUrl(url);
		//配置文件中包含更新地址、版本和记录数
		try {
			chekcSkuUpdate(configUrl+"sku_update.txt");
			infoStore = Util.getInfo(configUrl+"store_update.txt");
			infoPayment = Util.getInfo(configUrl+"payment_update.txt");
			infoDevice = Util.getInfo(configUrl+"device_update.txt");
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(infoSku.get(Constant.VERSION)==null)
		{
			((TextView) parent
					.findViewById(R.id.CommodityDataHostVersion))
					.setText(R.string.updateError);
			((Button) parent
					.findViewById(R.id.buttonUpdateCommodity))
					.setEnabled(false);
		}
		else
		{
			((TextView) parent
					.findViewById(R.id.CommodityDataHostVersion))
					.setText(infoSku.get(Constant.VERSION));
			((Button) parent
					.findViewById(R.id.buttonUpdateCommodity))
					.setEnabled(true);
		}
		
		if(infoStore.get(Constant.VERSION)==null)
		{
			((TextView) parent
					.findViewById(R.id.StoreDataHostVersion))
					.setText(R.string.updateError);
			((Button) parent
					.findViewById(R.id.buttonUpdateStore))
					.setEnabled(false);
		}
		else
		{
			((TextView) parent
					.findViewById(R.id.StoreDataHostVersion))
					.setText(infoStore.get(Constant.VERSION));
			((Button) parent
					.findViewById(R.id.buttonUpdateStore))
					.setEnabled(true);
		}
		
		if(infoPayment.get(Constant.VERSION)==null)
		{
			((TextView) parent
					.findViewById(R.id.PaymentDataHostVersion))
					.setText(R.string.updateError);
			((Button) parent
					.findViewById(R.id.buttonUpdatePayment))
					.setEnabled(false);
		}
		else
		{
			((TextView) parent
					.findViewById(R.id.PaymentDataHostVersion))
					.setText(infoPayment.get(Constant.VERSION));
			((Button) parent
					.findViewById(R.id.buttonUpdatePayment))
					.setEnabled(true);
		}
		
		if(infoDevice.get(Constant.VERSION)==null)
		{
			((TextView) parent
					.findViewById(R.id.DeviceDataHostVersion))
					.setText(R.string.updateError);
			((Button) parent
					.findViewById(R.id.buttonUpdateDevice))
					.setEnabled(false);
		}
		else
		{
			((TextView) parent
					.findViewById(R.id.DeviceDataHostVersion))
					.setText(infoDevice.get(Constant.VERSION));
			((Button) parent
					.findViewById(R.id.buttonUpdateDevice))
					.setEnabled(true);
		}
		
	}
	
	//得到配置文件地址
	private String getUpdateConfigUrl(String url)
	{
		return "";
	}
	
	//商品更新检查
	private void chekcSkuUpdate(String skuUrl)
	{
		Map<String,String> skuMap = getSkuConfig(skuUrl);
		if(skuMap==null||skuMap.get("allSkuUrl")==null||skuMap.get("allSkuVersion")==null||skuMap.get("allSkuRows")==null)
		{
			return;
		}
		//如果服务器上没增量更新版本，则使用全量更新
		if(skuMap.get("addSkuUrl")==null||skuMap.get("addSkuVersion")==null||skuMap.get("addSkuRows")==null)
		{
			infoSku.put(Constant.URL,skuMap.get("allSkuUrl"));
			infoSku.put(Constant.VERSION,skuMap.get("allSkuVersion"));	
			infoSku.put(Constant.ROWS,skuMap.get("allSkuRows"));
			return;
		}
			
		String currentVersion =((TextView) parent.findViewById(R.id.CommodityDataLocalVersion)).getText().toString().trim();
		//如果本地版本同上次更新版本相同，则只更新增量版本
		if(currentVersion.equalsIgnoreCase(skuMap.get("allSkuVersion")))
		{
			infoSku.put(Constant.URL,skuMap.get("addSkuUrl"));
			infoSku.put(Constant.VERSION,skuMap.get("addSkuVersion"));				
			infoSku.put(Constant.ROWS,skuMap.get("addSkuRows"));
		}
		else
		{
			infoSku.put(Constant.URL,skuMap.get("allSkuUrl"));
			infoSku.put(Constant.VERSION,skuMap.get("addSkuVersion"));				
			infoSku.put(Constant.ROWS,skuMap.get("allSkuRows"));
		}
	}
	
	//通过配置文件得到SKU更新的配置
	private Map getSkuConfig(String skuUrl)
	{
		BufferedReader in = null;
		try {
			in = null;
			in = Util.getBufferedReaderFromURI(skuUrl);
			Map<String, String> map = new HashMap<String, String>();
			String line = in.readLine();
			if(line==null)
				return map;
			map.put("allSkuUrl", line);
			line = in.readLine();
			if(line==null)
				return map;
			map.put("allSkuVersion", line);
			line = in.readLine();
			if(line==null)
				return map;
			map.put("allSkuRows", line);
			line = in.readLine();
			if(line==null)
				return map;
			map.put("addSkuUrl", line);
			line = in.readLine();
			if(line==null)
				return map;
			map.put("addSkuVersion", line);
			line = in.readLine();
			if(line==null)
				return map;
			map.put("addSkuRows", line);
			return map;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
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
														.deleteByKey(null,new String[] { infoSku
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
														.deleteByKey(null,new String[] { infoStore
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
														.deleteByKey(null,new String[] { infoPayment
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
														.deleteByKey(null,new String[] { infoDevice
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
