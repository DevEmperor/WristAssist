package net.devemperor.wristassist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UsageDatabaseHelper extends SQLiteOpenHelper {

    Context context;

    public UsageDatabaseHelper(@Nullable Context context) {
        super(context, "usage.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE USAGE (MODEL_NAME TEXT PRIMARY KEY, TOKENS LONG, COST DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public void edit(String model, long tokensToAdd, double costToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USAGE WHERE MODEL_NAME='" + model + "'", null);

        boolean entryExists = cursor.moveToFirst();
        cursor.close();

        if (!entryExists) {
            ContentValues cv = new ContentValues();
            cv.put("MODEL_NAME", model);
            cv.put("TOKENS", tokensToAdd);
            cv.put("COST", costToAdd);
            db.insert("USAGE", null, cv);
        } else {
            cursor = db.rawQuery("SELECT * FROM USAGE WHERE MODEL_NAME='" + model + "'", null);
            if (cursor.moveToFirst()) {
                long lastTokens = cursor.getLong(1);
                double lastCost = cursor.getDouble(2);
                ContentValues cv = new ContentValues();
                cv.put("TOKENS", lastTokens + tokensToAdd);
                cv.put("COST", lastCost + costToAdd);
                db.update("USAGE", cv, "MODEL_NAME='" + model + "'", null);
            }
            cursor.close();
        }

        db.close();
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM USAGE");
        db.close();
    }

    public List<UsageModel> getAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USAGE", null);

        List<UsageModel> models = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                models.add(new UsageModel(cursor.getString(0), cursor.getLong(1), cursor.getDouble(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return models;
    }

    public double getTotalCost() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(COST) FROM USAGE", null);

        double totalCost = 0;
        if (cursor.moveToFirst()) {
            totalCost = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return totalCost;
    }
}
