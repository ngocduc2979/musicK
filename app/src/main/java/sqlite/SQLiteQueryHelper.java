package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SQLiteQueryHelper{
    
//    private Class[] tables;
//
//    public SQLiteQueryHelper(Context context, String name, int version,
//                             Class... _class) {
//        // TODO Auto-generated constructor stub
//        super(context, name, null, version);
//        tables = _class;
//    }
//
//    private SQLiteDatabase db;
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        // TODO Auto-generated method stub
//        createTable(db, tables);
//    }
//
//    public void init() {
//        // TODO Auto-generated constructor stub
//        db = getWritableDatabase();
//        db.enableWriteAheadLogging();
//    }
//
//    public <T> List<T> get(Class<T> _class) {
//        Cursor cursor = db.query(_class.getSimpleName(), null, null, null,
//                null, null, null, null);
//        try {
//            return cursoToList(_class, cursor);
//        } catch (IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public <T> List<T> get(Class<T> _class, String where, String... whereArgs) {
//        Cursor cursor = db.query(_class.getSimpleName(), null, where,
//                whereArgs, null, null, null, null);
//        try {
//            return cursoToList(_class, cursor);
//        } catch (IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private <T> List<T> cursoToList(Class<T> _class, Cursor cursor)
//            throws IllegalAccessException, IllegalArgumentException {
//        T object = null;
//        ArrayList<T> listObject = new ArrayList<T>();
//        while (cursor.moveToNext()) {
//            try {
//                object = _class.newInstance();
//            } catch (InstantiationException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            for (Field field : _class.getDeclaredFields()) {
//                int index = cursor.getColumnIndex(field.getName());
//                String colName = field.getName();
//                if (isPrivateFieldDefault(colName)) {
//                    continue;
//                }
//                if (field.getType().isPrimitive()) {
//                    if (field.getType().toString().equalsIgnoreCase("int")) {
//                        field.setInt(object, cursor.getInt(index));
//                    } else if (field.getType().toString()
//                            .equalsIgnoreCase("long")) {
//                        field.setLong(object, cursor.getLong(index));
//                        // values.put(colName, field.getLong(object));
//                    } else if (field.getType().toString()
//                            .equalsIgnoreCase("float")) {
//                        field.setFloat(object, cursor.getFloat(index));
//                        // values.put(colName, field.getFloat(object));
//                    }
//                } else {
//                    if (field.getType().toString().contains("java.util.List")) {
//                        field.set(new Gson().toJson(object), cursor.getString(index));
//                    } else {
//                        field.set(object, cursor.getString(index));
//                    }
//                    // values.put(colName, (field.get(object) != null) ?
//                    // field.get(object).toString() : "");
//                }
//
//            }
//            listObject.add(object);
//        }
//        if (cursor != null) {
//            cursor.close();
//        }
//        return listObject;
//    }
//
//    public <T> void insert(T object) {
//        try {
//            String DATABASE_TABLE = object.getClass().getSimpleName();
//            db.insertOrThrow(DATABASE_TABLE, null,
//                    objectToValue(object));
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public <T> void update(T obj, String whereClause, String... whereArgs) {
//        try {
//            db.update(obj.getClass().getSimpleName(), objectToValue(obj),
//                    whereClause, whereArgs);
//        } catch (IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public <T> void updateFix(T obj, String whereClause) {
//        try {
//            db.update(obj.getClass().getSimpleName(), objectToValue(obj),
//                    whereClause, null);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public <T> void update(T obj) {
//        try {
//            db.update(obj.getClass().getSimpleName(), objectToValue(obj),
//                    null, null);
//        } catch (IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
////     return db.delete(DATABASE_TABLE, KEY_NAME + "=" + name, null) > 0;
//
//    public <T> void delete(T obj, String whereClause) {
//        String DATABASE_TABLE = obj.getClass().getSimpleName();
//        db.delete(DATABASE_TABLE, whereClause, null);
//    }
//
//    public <T> void delete(T obj, String where, String... whereArgs) {
//        try {
//            db.delete(obj.getClass().getSimpleName(), where, whereArgs);
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public <T> void delete(Class<T> _class, String where, String... whereArgs) {
//        try {
//            db.delete(_class.getSimpleName(), where, whereArgs);
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public  <T> void deleteAllFile(T obj){
//        String TABLE_NAME = obj.getClass().getSimpleName();
//        db.execSQL("delete from "+ TABLE_NAME);
//    }
//
//    private <T> ContentValues objectToValue(T object)
//            throws IllegalAccessException, IllegalArgumentException {
//        Field[] fields = object.getClass().getDeclaredFields();
//        ContentValues values = new ContentValues();
//        Field[] superFields = object.getClass().getSuperclass()
//                .getDeclaredFields();
//        fields = ArrayUtils.addAll(superFields, fields);
//        for (Field field : fields) {
//            String colName = field.getName();
//            if (isPrivateFieldDefault(colName)) {
//                continue;
//            }
//
//            if (field.getType().isPrimitive()) {
//                if (field.getType().toString().equalsIgnoreCase("int")) {
//                    values.put(colName, field.getInt(object));
//                } else if (field.getType().toString().equalsIgnoreCase("long")) {
//                    values.put(colName, field.getLong(object));
//                } else if (field.getType().toString().equalsIgnoreCase("float")) {
//                    values.put(colName, field.getFloat(object));
//                }
//            } else {
//                String value;
//                System.out.println("DatabaseUtil: error:::::" + field.get(object));
//                try {
//                    if (field.get(object) != null) {
//                        value = field.get(object).toString();
//                    } else {
//                        value = "";
//                    }
//
//                    values.put(colName, value);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//
//        return values;
//    }
//
//    private boolean isPrivateFieldDefault(String name) {
//        if (name.startsWith("shadow$")) {
//            return true;
//        }
//        if (name.startsWith("$change")) {
//            return true;
//        }
//        if (name.contains("shadow$_klass_")) {
//            return true;
//        }
//        if (name.contains("shadow$_monitor_")) {
//            return true;
//        }
//        if (name.contains("serialVersionUID")) {
//            return true;
//        }
//        return false;
//    }
//
//    public void createTable(SQLiteDatabase db, Class... _class) {
//        for (int i = 0; i < _class.length; i++) {
//            String attr = "";
//            Field[] superFields = _class[i].getSuperclass().getDeclaredFields();
//            Field[] fields = _class[i].getDeclaredFields();
//            fields = ArrayUtils.addAll(superFields, fields);
//            for (Field field : fields) {
//                if (isPrivateFieldDefault(field.getName())) {
//                    continue;
//                }
//                if (field.getType().isPrimitive()) {
//                    if (field.getType().toString().equalsIgnoreCase("int")) {
//                        attr += field.getName() + " integer, ";
//                    } else if (field.getType().toString()
//                            .equalsIgnoreCase("long")) {
//                        attr += field.getName() + " long, ";
//                    } else if (field.getType().toString()
//                            .equalsIgnoreCase("float")) {
//                        attr += field.getName() + " float, ";
//                    }
//                } else if (field.getType().isAssignableFrom(String.class)) {
//                    attr += field.getName() + " text, ";
//                } else {
//                    attr += field.getName() + " blob, ";
//                }
//            }
//            attr = attr.substring(0, attr.length() - 2);
//
//            String tableName =  _class[i].getSimpleName();
//            String query = String
//                    .format("CREATE TABLE %s (_id integer primary key, %s)",
//                            tableName, attr);
//            db.execSQL(query);
//        }
//
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // TODO Auto-generated method stub
//        Log.w(SQLiteQueryHelper.class.getName(),
//                "Upgrading database from version " + oldVersion + " to "
//                        + newVersion + ", which will destroy all old data");
//        for (int i = 0; i < tables.length; i++) {
//            db.execSQL("DROP TABLE IF EXISTS " + tables[i].getSimpleName());
//        }
//        onCreate(db);
//    }
//
//    public void closeDb() {
//        if (db != null) {
//            try {
//                db.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
