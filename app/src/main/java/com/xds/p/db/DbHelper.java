package com.xds.p.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.xds.p.bean.XdsBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private final String TAG = "DbHelper";

    private static String DB_NAME = "xdsdata.db";// ----------------------数据库名
    private static String DB_TABLE_XDSDATA = "xds_data";// ------------表名
    private static int DB_VERSION = 1;// -------------------------------------版本号

    public static interface XdsDb extends BaseColumns {
        String NUM = "num";// -----------------起点纬度--1
        String TIME = "time";// ------------起点经度--2
    }

    private Context mContext;


    private HashMap<String, String> numMap;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;

        getWritableDatabase();
        initColorMap();
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
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_XDSDATA);
            db.execSQL("CREATE TABLE " + DB_TABLE_XDSDATA + "(" + XdsDb._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + XdsDb.NUM + " INTEGER, "
                    + XdsDb.TIME + " INTEGER" + ");");

            db.execSQL("CREATE INDEX " + XdsDb.NUM + "_index ON " + DB_TABLE_XDSDATA + "(" + XdsDb.NUM + ");");

            // 完成
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e(TAG, "couldn't create tables");
            throw ex;
        } finally {
            db.endTransaction();
        }
    }


    private List<ContentValues> mValues;
    private final String mDataFilePath = "mnt/sd/mmcblk0p11/gpsdata.csv";


    /** 读取文件中的数据写入数据库 **/
//	public void loadData() {
//		mValues = new ArrayList<ContentValues>();
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				File file = new File(mDataFilePath);
//				if (file.exists()) {
//					InputStreamReader read;
//					try {
//						read = new InputStreamReader(new FileInputStream(file), "GBK");
//						BufferedReader bufferedReader = new BufferedReader(read);
//						String lineTxt = null;
//						while ((lineTxt = bufferedReader.readLine()) != null) {// &&i!=200
//							Log.d(TAG, lineTxt);
//							if (!lineTxt.contains("序号")) {
//								String[] s = lineTxt.split(",");
//								ContentValues value = new ContentValues();
////								long a = (long) (Float.valueOf(s[2]) * sCardinalNumber);
//								double f = Double.parseDouble(s[2]);
//								double f1 = f * sCardinalNumber;
//								Log.d(TAG, "f = " + (int)f + "   f1 = " + (int)f1);
//
//								value.put(GpsDb.START_LATITUDE, (int) (Double.parseDouble(s[1]) * sCardinalNumber));
//								value.put(GpsDb.START_LONGITUDE, (int) (Double.parseDouble(s[2]) * sCardinalNumber));
//								value.put(GpsDb.START_ANGLE, Integer.valueOf(s[3]));
//								value.put(GpsDb.END_LATITUDE, (int) (Double.parseDouble(s[4]) * sCardinalNumber));
//								value.put(GpsDb.END_LONGITUDE, (int) (Double.parseDouble(s[5]) * sCardinalNumber));
//								value.put(GpsDb.END_ANGLE, Integer.valueOf(s[6]));
//								value.put(GpsDb.SPEED_LIMIT, Integer.valueOf(s[7]));
//								value.put(GpsDb.DISTANCE, Integer.valueOf(s[8]));
//								value.put(GpsDb.TYPE, Integer.valueOf(s[9]));
//								value.put(GpsDb.INFO, s[10].contains("未知类型") ? "易肇事路段" : s[10]);
//								mValues.add(value);
//								 //i++;
//								if (mValues.size() == 100) {
//									insertDatas(mValues);
//									mValues.clear();
//								}
//							}
//						}
//						if (mValues != null && !mValues.isEmpty()) {
//							insertDatas(mValues);
//							mValues.clear();
//						}
//						read.close();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
//	}

    /** 按起点纬度排序 **/
//	public void order() {
//		// INSERT INTO
//		// order_table(start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info)
//		// SELECT
//		// start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info
//		// FROM eddata ORDER BY
//		// start_lattitude,start_longitude,end_lattitude,end_longitude;
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				SQLiteDatabase db;
//				db = getWritableDatabase();
//				db.beginTransaction();
//				try {
//					Log.d(TAG, "----------------开始排序-----------");
//					String sql = "INSERT INTO order_table(start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info) " +
//							"SELECT start_lattitude,start_longitude,start_angle,end_lattitude,end_longitude,end_angle,speed_limit,distance,type,info FROM eddata ORDER BY start_lattitude,start_longitude,end_lattitude,end_longitude";
//					db.execSQL(sql);
//					db.setTransactionSuccessful();
//				}finally{
//					db.endTransaction();
//				}
//				Log.d(TAG, "----------------排序成功------------");
//
//			}
//		}).start();
//	}

    /**
     * 单条数据插入
     **/
    public long insertData(ContentValues values) {
        long rowId;
        SQLiteDatabase db;

        db = getWritableDatabase();
        if (db == null)
            return -1;

        db.beginTransaction();
        try {
            rowId = db.insert(DB_TABLE_XDSDATA, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rowId;
    }

    /** 多条数据插入 **/
//	public int insertDatas(List<ContentValues> values) {
//		int count = 0;
//		SQLiteDatabase db;
//
//		db = getWritableDatabase();
//		if (db == null)
//			return 0;
//
//		db.beginTransaction();
//		try {
//			for (int i = 0, len = values.size(); i < len; i++) {
//				if (db.insert(DB_TABLE_XDSDATA, null, values.get(i)) != -1) {
//					count++;
//				}
//			}
//			db.setTransactionSuccessful();
//		} finally {
//			db.endTransaction();
//		}
//		return count;
//	}

    /**
     * 删除数据
     **/
    public int deleteDatas(String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db;

        db = getWritableDatabase();
        if (db == null)
            return 0;

        db.beginTransaction();
        try {
            count = db.delete(DB_TABLE_XDSDATA, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }

    /**
     * 更新数据
     **/
    public int updateDatas(ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db;
        db = getWritableDatabase();
        if (db == null)
            return 0;
        db.beginTransaction();
        try {
            count = db.update(DB_TABLE_XDSDATA, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public Cursor queryDatas(String[] projection, String selection, String[] selectionArgs, String sortOrder, String limit) {
        Cursor c = query(DB_TABLE_XDSDATA, projection, selection, selectionArgs, sortOrder, limit);
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


    /**
     * 查询最后一个添加的数据信息
     *
     * @return
     */
    public XdsBean queryLastData() {
        SQLiteDatabase db;
        db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("select * from " + DB_TABLE_XDSDATA + " order by _id desc limit 0,1;", null);
            int firstId = -1;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int numColumn = cursor.getColumnIndex(XdsDb.NUM);
                int idColumn = cursor.getColumnIndex(XdsDb._ID);
                int timeColumn = cursor.getColumnIndex(XdsDb.TIME);

                firstId = cursor.getInt(idColumn);
                int num = cursor.getInt(numColumn);
                int time = cursor.getInt(timeColumn);

                String numS = String.valueOf(num);
                if (num > 0 && num < 10) {
                    numS = 0 + numS;
                }

                String[] shuXingList = getShuXing(String.valueOf(numS));
                String yangSe = shuXingList[0];
                String shengXiao = shuXingList[1];
                Log.d(TAG, "firstId:" + firstId + "  num:" + numS + "  yangSe:" + yangSe + "  shengXiao:" + shengXiao);
                db.setTransactionSuccessful();
                return new XdsBean(firstId, numS, time, yangSe, shengXiao);
            }
            db.setTransactionSuccessful();
            return null;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 查询号码前两个数
     *
     * @return
     */
    public ArrayList<XdsBean> queryTheFirstTwoNumbers(int firstId) {
        SQLiteDatabase db;
        db = getWritableDatabase();
        db.beginTransaction();
        try {
            ArrayList<XdsBean> list = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                firstId--;
                Cursor cursor1 = db.rawQuery("select * from " + DB_TABLE_XDSDATA + " where _id=" + firstId + ";", null);
                for (cursor1.moveToFirst(); !cursor1.isAfterLast(); cursor1.moveToNext()) {
                    int numColumn = cursor1.getColumnIndex(XdsDb.NUM);
                    int idColumn = cursor1.getColumnIndex(XdsDb._ID);
                    int timeColumn = cursor1.getColumnIndex(XdsDb.TIME);

                    int id = cursor1.getInt(idColumn);
                    int num = cursor1.getInt(numColumn);
                    int time = cursor1.getInt(timeColumn);

                    String numS = String.valueOf(num);
                    if (num > 0 && num < 10) {
                        numS = 0 + numS;
                    }

                    String[] shuXingList = getShuXing(String.valueOf(numS));

                    String yangSe = shuXingList[0];
                    String shengXiao = shuXingList[1];
                    Log.d(TAG, "id:" + firstId + "  num:" + numS + "  yangSe:" + yangSe + "  shengXiao:" + shengXiao);
                    list.add(new XdsBean(id, numS, time, yangSe, shengXiao));
                }
            }
            db.setTransactionSuccessful();
            return list;
        } finally {
            db.endTransaction();
        }
    }


    /**
     * 查询所有数据
     **/
    public ArrayList<XdsBean> query() {
        SQLiteDatabase db;
        db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + DB_TABLE_XDSDATA + ";", null);
            ArrayList<XdsBean> list = new ArrayList<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int numColumn = cursor.getColumnIndex(XdsDb.NUM);
                int idColumn = cursor.getColumnIndex(XdsDb._ID);
                int timeColumn = cursor.getColumnIndex(XdsDb.TIME);

                int id = cursor.getInt(idColumn);
                int num = cursor.getInt(numColumn);
                int time = cursor.getInt(timeColumn);
                Log.d(TAG, "id = " + id + "  num = " + num + "  time = " + time);
                String numS = String.valueOf(num);
                if (num > 0 && num < 10) {
                    numS = 0 + numS;
                }


                String[] shuXingList = getShuXing(numS);
                String yangSe = shuXingList[0];
                String shengXiao = shuXingList[1];
                Log.d(TAG, "num:" + numS + "  yangSe:" + yangSe + "  shengXiao:" + shengXiao);
                list.add(new XdsBean(id, numS, time, yangSe, shengXiao));
            }
            db.setTransactionSuccessful();
            return list;
        } finally {
            db.endTransaction();
        }
    }

    public static String red = "red_";
    public static String green = "green_";
    public static String blue = "blue_";

    public static String shu = "shu";
    public static String niu = "niu";
    public static String hu = "hu";
    public static String tu = "tu";
    public static String long1 = "long";
    public static String she = "she";
    public static String ma = "ma";
    public static String yang = "yang";
    public static String hou = "hou";
    public static String ji = "ji";
    public static String gou = "gou";
    public static String zhu = "zhu";

    private void initColorMap() {
        numMap = new HashMap<>();

        numMap.put("01", red + gou);
        numMap.put("02", red + ji);
        numMap.put("03", blue + hou);
        numMap.put("04", blue + yang);
        numMap.put("05", green + ma);
        numMap.put("06", green + she);
        numMap.put("07", red + long1);
        numMap.put("08", red + tu);
        numMap.put("09", blue + hu);
        numMap.put("10", blue + niu);
        numMap.put("11", green + shu);
        numMap.put("12", red + zhu);
        numMap.put("13", red + gou);
        numMap.put("14", blue + ji);
        numMap.put("15", blue + hou);
        numMap.put("16", green + yang);
        numMap.put("17", green + ma);
        numMap.put("18", red + she);
        numMap.put("19", red + long1);
        numMap.put("20", blue + tu);
        numMap.put("21", green + hu);
        numMap.put("22", green + niu);
        numMap.put("23", red + shu);
        numMap.put("24", red + zhu);
        numMap.put("25", blue + gou);
        numMap.put("26", blue + ji);
        numMap.put("27", green + hou);
        numMap.put("28", green + yang);
        numMap.put("29", red + ma);
        numMap.put("30", red + she);
        numMap.put("31", blue + long1);
        numMap.put("32", green + tu);
        numMap.put("33", green + hu);
        numMap.put("34", red + niu);
        numMap.put("35", red + shu);
        numMap.put("36", blue + zhu);
        numMap.put("37", blue + gou);
        numMap.put("38", green + ji);
        numMap.put("39", green + hou);
        numMap.put("40", red + yang);
        numMap.put("41", blue + ma);
        numMap.put("42", blue + she);
        numMap.put("43", green + long1);
        numMap.put("44", green + tu);
        numMap.put("45", red + hu);
        numMap.put("46", red + niu);
        numMap.put("47", blue + shu);
        numMap.put("48", blue + zhu);
        numMap.put("49", green + gou);
    }


    private String[] getShuXing(String num) {
        return numMap.get(num).split("_");
    }
}
