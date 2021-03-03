package com.dmatsanganis.hardaliapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.dmatsanganis.hardaliapp.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConfiguration extends SQLiteOpenHelper {

    // Initialize SQLite Database.
    private static final String DB_NAME ="MessageReasonsDB";
    public static final String TABLE_NAME ="Messages";
    private static final int DATABASE_VERSION = 1;
    private static final String ID = "reason_id";
    private static final String DATA = "reason_data";
    private static final String TITLE = "reason_title";
    private SQLiteDatabase db;
    private String[] reasonIDArray, reasonDataArray, reasonTitleArray;

    // Messages Titles.
    private String title_no1 = "Φαρμακείο - Ιατρείο";
    private String title_no2 = "Κατάστημα προμήθειας αγαθών";
    private String title_no3 = "Τράπεζα";
    private String title_no4 = "Παροχή βοήθειας";
    private String title_no5 = "Τελετή - Γονείς σε διάσταση";
    private String title_no6 = "Άθληση - Ανάγκες κατοικίδιου";

    // Messages Data.
    private String data_no1 = "Μετάβαση σε φαρμακείο ή επίσκεψη στον γιατρό, εφόσον αυτό συνιστάται μετά από σχετική επικοινωνία.";
    private String data_no2 = "Μετάβαση σε εν λειτουργία κατάστημα προμηθειών αγαθών πρώτης ανάγκης (σούπερ μάρκετ, μίνι μάρκετ), όπου δεν είναι δυνατή η αποστολή τους.";
    private String data_no3 = "Μετάβαση στην τράπεζα, στο μέτρο που δεν είναι δυνατή η ηλεκτρονική συναλλαγή.";
    private String data_no4 = "Κίνηση για παροχή βοήθειας σε ανθρώπους που βρίσκονται σε ανάγκη ή συνοδεία ανηλίκων μαθητών από/προς το σχολείο.";
    private String data_no5 = "Μετάβαση σε τελετή κηδείας υπό τους όρους που προβλέπει ο νόμος ή μετάβαση διαζευγμένων γονέων ή γονέων που τελούν σε διάσταση που είναι αναγκαία για τη διασφάλιση της επικοινωνίας γονέων και τέκνων, σύμφωνα με τις κείμενες διατάξεις.";
    private String data_no6 = "Σωματική άσκηση σε εξωτερικό χώρο ή κίνηση με κατοικίδιο ζώο, ατομικά ή ανά δύο άτομα, τηρώντας στην τελευταία αυτή περίπτωση την αναγκαία απόσταση 1,5 μέτρου.";


    public DatabaseConfiguration(@Nullable Context context)
    {
        super(context, DB_NAME, null  , DATABASE_VERSION);
        this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+ "("+ID+","+DATA+","+TITLE+");");
        db.execSQL("insert into messages values ('"+1+"','"+data_no1+"','"+title_no1+"')");
        db.execSQL("insert into messages values ('"+2+"','"+data_no2+"','"+title_no2+"')");
        db.execSQL("insert into messages values ('"+3+"','"+data_no3+"','"+title_no3+"')");
        db.execSQL("insert into messages values ('"+4+"','"+data_no4+"','"+title_no4+"')");
        db.execSQL("insert into messages values ('"+5+"','"+data_no5+"','"+title_no5+"')");
        db.execSQL("insert into messages values ('"+6+"','"+data_no6+"','"+title_no6+"')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getData() {
        db = this.getWritableDatabase();

        // Database's cursor.
        Cursor cursor = db.rawQuery("select * from Messages",null);
        return cursor;
    }

    // Get Arrays voids.
    public String[] getReasonDataArray() {
        return reasonDataArray;
    }

    public String[] getReasonIDArray() {
        return reasonIDArray;
    }

    public String[] getReasonTitleArray() {
        return reasonTitleArray;
    }

    public List<DatabaseModel> getmessages(){
        String sql  = "select * from " + TABLE_NAME;
        SQLiteDatabase db =this.getWritableDatabase();
        List<DatabaseModel> messages = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            DatabaseModel obj = new DatabaseModel(cursor.getString(0), cursor.getString(2), cursor.getString(1));
            messages.add(obj);
        }
        return messages;
    }

    // Delete Message Database's void.
    public void updatemessage(DatabaseModel databaseModel){
        db =this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConfiguration.ID,databaseModel.getId());
        contentValues.put(DatabaseConfiguration.DATA,databaseModel.getData());
        contentValues.put(DatabaseConfiguration.TITLE,databaseModel.getReason());
        db.update(TABLE_NAME,contentValues,ID + " = ?", new String[]
                {
                String.valueOf(databaseModel.getId())});
    }

    // Delete Message Database's void.
    public  void deletemessage(String id){
        db =this.getWritableDatabase();
        db.delete(TABLE_NAME, ID + " = ? ", new String[]{String.valueOf(id)});
    }

    // Add Message Database's void.
    public void addmessage(DatabaseModel databaseModel){
        db =this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConfiguration.ID,databaseModel.getId());
        contentValues.put(DatabaseConfiguration.DATA,databaseModel.getData());
        contentValues.put(DatabaseConfiguration.TITLE,databaseModel.getReason());
        db.insert(TABLE_NAME,null,contentValues);
    }
}
