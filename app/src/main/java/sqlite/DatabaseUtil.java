package sqlite;

import android.content.Context;

import java.util.List;

public class DatabaseUtil<T> {
//    private static final int DB_VERSION = 2;
//    private static final String DN_NAME = "PDFREADER";
//    private SQLiteQueryHelper dbHelper;
//    private static DatabaseUtil instant;
//    private Context context;
//    private List<T> data;
//
//    private DatabaseUtil(List<T> data) {
//        this.data = data;
//    }
//    private DatabaseUtil() {
//    }
//
//    public DatabaseUtil getInstant(Context ctx) {
//        if (instant == null) {
//            synchronized (DatabaseUtil.class) {
//                if (instant == null) {
//                    instant = new DatabaseUtil();
//                    instant.context = ctx;
//                    instant.dbHelper = new SQLiteQueryHelper(ctx, DN_NAME,
//                            DB_VERSION, data.getClass());
//                    instant.dbHelper.init();
//                }
//            }
//        }
//        return instant;
//    }
//
//    public void insertFavorite() {
//        try {
//            if (dbHelper != null) {
//                dbHelper.insert(data);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void upDateReading(Reading reading, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.updateFix(reading, whereClause);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public List<PDFInfo> getList() {
//        if (dbHelper == null) return null;
//        List<PDFInfo> list = dbHelper.get(PDFInfo.class, null);
//        return list;
//    }
//
//    public void deleteFavorite(Favorite favorite, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.delete(favorite, whereClause, null);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void deleteReading(Reading reading, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.delete(reading, whereClause, null);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void deleteHistory(PDFInfo pdfInfo, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.delete(pdfInfo, whereClause, null);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void upDateFavorite(Favorite favorite, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.updateFix(favorite, whereClause);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void upDatePdfInfo(PDFInfo pdfInfo, String whereClause) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.updateFix(pdfInfo, whereClause);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public List<Favorite> getListFavorite() {
//        if (dbHelper == null) return null;
//        List<Favorite> list = dbHelper.get(Favorite.class, null);
//        return list;
//    }
//
//    public List<Reading> getReading() {
//        if (dbHelper == null) return null;
//        List<Reading> list = dbHelper.get(Reading.class, null);
//        return list;
//    }
//
//    public void deleteAllFile(Favorite favorite) {
//        try {
//            if (dbHelper != null) {
//                dbHelper.deleteAllFile(favorite);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
