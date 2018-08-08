package com.xds.p.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	private final String TAG = "DbHelper";

	public static final double sCardinalNumber = 1000000d;// 经纬度换算基数
	private static String DB_NAME = "eddata.db";// ----------------------数据库名
	private static String DB_TABLE_GPSDATA = "eddata";// ------------表名
	private static int DB_VERSION = 1;// -------------------------------------版本号

	private final static String DB_TABLE_CHANGE_TYPE = "change_type";// 临时数据表名
	private static String DB_TABLE_ORDER = "order_table";// -----------------排序表名

	public static interface GpsDb extends BaseColumns {
		String START_LATITUDE = "start_lattitude";// -----------------起点纬度--1
		String START_LONGITUDE = "start_longitude";// ------------起点经度--2
		String START_ANGLE = "start_angle";// -------------------------起点角度--3

		String END_LATITUDE = "end_lattitude";// ---------------------终点纬度--4
		String END_LONGITUDE = "end_longitude";// ----------------终点经度--5
		String END_ANGLE = "end_angle";// -----------------------------终点角度--6

		String SPEED_LIMIT = "speed_limit";// ---------------------------限速--------7
		String DISTANCE = "distance";// ------------------------------------距离--------8
		String TYPE = "type";// -------------------------------------------------类型--------9
		String INFO = "info";// -------------------------------------------------信息--------10
		String NEW_ADD = "new_add";// -----------------------------------新增--------11
	}

	private Context mContext;

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onUpgrade(db, 0, DB_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		createTable(db);
	}

	private void createTable(SQLiteDatabase db) {
		Log.d(TAG, "createTable");
		db.beginTransaction();
		try {
			// 创建数据表及其索引
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GPSDATA);
			db.execSQL("CREATE TABLE " + DB_TABLE_GPSDATA + "(" + GpsDb._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GpsDb.START_LATITUDE + " INTEGER, "
					+ GpsDb.START_LONGITUDE + " INTEGER, " + GpsDb.START_ANGLE + " INTEGER, " + GpsDb.END_LATITUDE + " INTEGER, " + GpsDb.END_LONGITUDE
					+ " INTEGER, " + GpsDb.END_ANGLE + " INTEGER, " + GpsDb.SPEED_LIMIT + " INTEGER, " + GpsDb.DISTANCE + " INTEGER, " + GpsDb.TYPE
					+ " INTEGER, " + GpsDb.INFO + " TEXT" + ");");

//			db.execSQL("CREATE INDEX " + GpsDb.START_LATITUDE + "_index ON " + DB_TABLE_GPSDATA + "(" + GpsDb.START_LATITUDE + ");");
//			db.execSQL("CREATE INDEX " + GpsDb.START_LONGITUDE + "_index ON " + DB_TABLE_GPSDATA + "(" + GpsDb.START_LONGITUDE + ");");
//			db.execSQL("CREATE INDEX " + GpsDb.END_LATITUDE + "_index ON " + DB_TABLE_GPSDATA + "(" + GpsDb.END_LATITUDE + ");");
//			db.execSQL("CREATE INDEX " + GpsDb.END_LONGITUDE + "_index ON " + DB_TABLE_GPSDATA + "(" + GpsDb.END_LONGITUDE + ");");

			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CHANGE_TYPE);
			db.execSQL("CREATE TABLE " + DB_TABLE_CHANGE_TYPE + "(" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT" + ");");

			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ORDER);
			db.execSQL("CREATE TABLE " + DB_TABLE_ORDER + "(" + GpsDb._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GpsDb.START_LATITUDE + " INTEGER, "
					+ GpsDb.START_LONGITUDE + " INTEGER, " + GpsDb.START_ANGLE + " INTEGER, " + GpsDb.END_LATITUDE + " INTEGER, " + GpsDb.END_LONGITUDE
					+ " INTEGER, " + GpsDb.END_ANGLE + " INTEGER, " + GpsDb.SPEED_LIMIT + " INTEGER, " + GpsDb.DISTANCE + " INTEGER, " + GpsDb.TYPE
					+ " INTEGER, " + GpsDb.INFO + " TEXT, " +GpsDb.NEW_ADD 	+ " INTEGER" + ");");
			
			db.execSQL("CREATE INDEX " + GpsDb.START_LATITUDE + "_index ON " + DB_TABLE_ORDER + "(" + GpsDb.START_LATITUDE + ");");
			db.execSQL("CREATE INDEX " + GpsDb.START_LONGITUDE + "_index ON " + DB_TABLE_ORDER + "(" + GpsDb.START_LONGITUDE + ");");
			db.execSQL("CREATE INDEX " + GpsDb.END_LATITUDE + "_index ON " + DB_TABLE_ORDER + "(" + GpsDb.END_LATITUDE + ");");
			db.execSQL("CREATE INDEX " + GpsDb.END_LONGITUDE + "_index ON " + DB_TABLE_ORDER + "(" + GpsDb.END_LONGITUDE + ");");

			// 完成
			db.setTransactionSuccessful();
		} catch (SQLException ex) {
			Log.e(TAG, "couldn't create tables");
			throw ex;
		} finally {
			db.endTransaction();
		}
	}

	public void ChangeByType() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SQLiteDatabase db;
				db = getWritableDatabase();
				db.beginTransaction();
				try {
					db.execSQL("INSERT INTO change_type(name) SELECT info FROM eddata GROUP BY info;");// 直接写了
					// db.execSQL("update eddata set type=(select _id from change_type where eddata.info=change_type.name);");
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
				Log.d(TAG, "归类");

				SQLiteDatabase db2;
				db2 = getWritableDatabase();
				db2.beginTransaction();
				try {
					// db.execSQL("INSERT INTO change_type(name) SELECT info FROM eddata GROUP BY info;");//
					// 直接写了
					db2.execSQL("update eddata set type=(select _id from change_type where eddata.info=change_type.name);");
					db2.setTransactionSuccessful();
				} finally {
					db2.endTransaction();
				}
				Log.d(TAG, "重新分配归类");
			}
		}).start();

	}

	private List<ContentValues> mValues;
	private final String mDataFilePath = "mnt/sd/mmcblk0p11/gpsdata.csv";

	 //int i = 0;

	/** 读取文件中的数据写入数据库 **/
	public void loadData() {
		mValues = new ArrayList<ContentValues>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				File file = new File(mDataFilePath);
				if (file.exists()) {
					InputStreamReader read;
					try {
						read = new InputStreamReader(new FileInputStream(file), "GBK");
						BufferedReader bufferedReader = new BufferedReader(read);
						String lineTxt = null;
						while ((lineTxt = bufferedReader.readLine()) != null) {// &&i!=200
							Log.d(TAG, lineTxt);
							if (!lineTxt.contains("序号")) {
								String[] s = lineTxt.split(",");
								ContentValues value = new ContentValues();
//								long a = (long) (Float.valueOf(s[2]) * sCardinalNumber);
								double f = Double.parseDouble(s[2]);
								double f1 = f * sCardinalNumber;
								Log.d(TAG, "f = " + (int)f + "   f1 = " + (int)f1);
								
								value.put(GpsDb.START_LATITUDE, (int) (Double.parseDouble(s[1]) * sCardinalNumber));
								value.put(GpsDb.START_LONGITUDE, (int) (Double.parseDouble(s[2]) * sCardinalNumber));
								value.put(GpsDb.START_ANGLE, Integer.valueOf(s[3]));
								value.put(GpsDb.END_LATITUDE, (int) (Double.parseDouble(s[4]) * sCardinalNumber));
								value.put(GpsDb.END_LONGITUDE, (int) (Double.parseDouble(s[5]) * sCardinalNumber));
								value.put(GpsDb.END_ANGLE, Integer.valueOf(s[6]));
								value.put(GpsDb.SPEED_LIMIT, Integer.valueOf(s[7]));
								value.put(GpsDb.DISTANCE, Integer.valueOf(s[8]));
								value.put(GpsDb.TYPE, Integer.valueOf(s[9]));
								value.put(GpsDb.INFO, s[10].contains("未知类型") ? "易肇事路段" : s[10]);
								mValues.add(value);
								 //i++;
								if (mValues.size() == 100) {
									insertDatas(mValues);
									mValues.clear();
								}
							}
						}
						if (mValues != null && !mValues.isEmpty()) {
							insertDatas(mValues);
							mValues.clear();
						}
						read.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/** 按起点纬度排序 **/
	public void order() {
		// INSERT INTO
		// order_table(start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info)
		// SELECT
		// start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info
		// FROM eddata ORDER BY
		// start_lattitude,start_longitude,end_lattitude,end_longitude;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				SQLiteDatabase db;
				db = getWritableDatabase();
				db.beginTransaction();
				try {
					Log.d(TAG, "----------------开始排序-----------");
					String sql = "INSERT INTO order_table(start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info) " +  
							"SELECT start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info FROM eddata ORDER BY start_lattitude,start_longitude,end_lattitude,end_longitude";
					db.execSQL(sql);
					db.setTransactionSuccessful();
				}finally{
					db.endTransaction();
				}
				Log.d(TAG, "----------------排序成功------------");
				
			}
		}).start();
	}

	/** 单条数据插入 **/
	public long insertData(ContentValues values) {
		long rowId;
		SQLiteDatabase db;

		db = getWritableDatabase();
		if (db == null)
			return -1;

		db.beginTransaction();
		try {
			rowId = db.insert(DB_TABLE_GPSDATA, null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return rowId;
	}

	/** 多条数据插入 **/
	public int insertDatas(List<ContentValues> values) {
		int count = 0;
		SQLiteDatabase db;

		db = getWritableDatabase();
		if (db == null)
			return 0;

		db.beginTransaction();
		try {
			for (int i = 0, len = values.size(); i < len; i++) {
				if (db.insert(DB_TABLE_GPSDATA, null, values.get(i)) != -1) {
					count++;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return count;
	}

	/** 排序数据表插入 **/
	public int insertDatasToOrder(List<ContentValues> values) {
		int count = 0;
		SQLiteDatabase db;

		db = getWritableDatabase();
		if (db == null)
			return 0;

		db.beginTransaction();
		try {
			for (int i = 0, len = values.size(); i < len; i++) {
				if (db.insert(DB_TABLE_ORDER, null, values.get(i)) != -1) {
					count++;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return count;
	}

	/** 删除数据 **/
	public int deleteDatas(String selection, String[] selectionArgs) {
		int count;
		SQLiteDatabase db;

		db = getWritableDatabase();
		if (db == null)
			return 0;

		db.beginTransaction();
		try {
			count = db.delete(DB_TABLE_GPSDATA, selection, selectionArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return count;
	}

	/** 更新数据 **/
	public int updateDatas(ContentValues values, String selection, String[] selectionArgs) {
		int count;
		SQLiteDatabase db;
		db = getWritableDatabase();
		if (db == null)
			return 0;
		db.beginTransaction();
		try {
			count = db.update(DB_TABLE_GPSDATA, values, selection, selectionArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return count;
	}

	public Cursor queryDatas(String[] projection, String selection, String[] selectionArgs, String sortOrder, String limit) {
		Cursor c = query(DB_TABLE_GPSDATA, projection, selection, selectionArgs, sortOrder, limit);
		// c.setNotificationUri(mContext.getContentResolver(),
		// MediaStore.Audio.Playlist.CONTENT_URI);
		return c;
	}

	private Cursor query(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder, String limit) {
		SQLiteDatabase db = getReadableDatabase();

		SQLiteQueryBuilder qb;
		qb = new SQLiteQueryBuilder();
		qb.setTables(table);
		return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
	}

	long time;

	/** 测试查询 **/
	public void query() {
		time = System.currentTimeMillis();
		// Log.d(TAG, "开始时间" + System.currentTimeMillis());
		SQLiteDatabase db;
		db = getWritableDatabase();
		db.beginTransaction();
		try {
			// db.execSQL("SELECT * FROM eddata WHERE _id IN(3000,6000,9000,8000,12000,15000,17000,180000,320000,500000);");
			String ins = "3000";
			for (int i = 0; i < 500; i++) {
				ins += "," + (3000 + i * 1000);
			}

			// Cursor cursor =
			// db.rawQuery("SELECT * FROM eddata WHERE _id IN(3000,6000,9000,8000,12000,15000,17000,180000,320000,500000);",
			// null);
			Cursor cursor = db.rawQuery("SELECT * FROM eddata WHERE _id IN(" + ins + ");", null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				int infoColumn = cursor.getColumnIndex(GpsDb.INFO);
				int idColumn = cursor.getColumnIndex(GpsDb._ID);
				String aaa = cursor.getString(infoColumn);
				aaa += "  _id " + cursor.getInt(idColumn);
				Log.d(TAG, aaa);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		// String[] projection = new String[]{GpsDb._ID};
		// query(DB_TABLE_GPSDATA, projection, selection, selectionArgs,
		// sortOrder, limit);
		Log.d(TAG, "结束时间" + (System.currentTimeMillis() - time));

	}

}
