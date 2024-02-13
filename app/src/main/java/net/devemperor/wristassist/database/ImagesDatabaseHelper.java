package net.devemperor.wristassist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesDatabaseHelper extends SQLiteOpenHelper {

    Context context;

    public ImagesDatabaseHelper(@Nullable Context context) {
        super(context, "images.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IMAGES (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "PROMPT TEXT, REVISED_PROMPT TEXT, MODEL TEXT, QUALITY TEXT, SIZE TEXT, STYLE TEXT, CREATED LONG, URL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public int add(ImageModel imageModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("PROMPT", imageModel.getPrompt());
        cv.put("REVISED_PROMPT", imageModel.getRevisedPrompt());
        cv.put("MODEL", imageModel.getModel());
        cv.put("QUALITY", imageModel.getQuality());
        cv.put("SIZE", imageModel.getSize());
        cv.put("STYLE", imageModel.getStyle());
        cv.put("CREATED", imageModel.getCreated());
        cv.put("URL", imageModel.getUrl());

        return (int) db.insert("IMAGES", null, cv);
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("IMAGES", "ID=" + id, null);
        db.close();

        String filePath = context.getFilesDir().getAbsolutePath() + "/image_" + id + ".png";
        new File(filePath).delete();
    }

    public List<ImageModel> getAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM IMAGES ORDER BY CREATED DESC", null);

        List<ImageModel> models = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                models.add(new ImageModel(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6), cursor.getLong(7), cursor.getString(8)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return models;
    }

    public ImageModel get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM IMAGES WHERE ID=" + id, null);

        ImageModel model = null;
        if (cursor.moveToFirst()) {
            model = new ImageModel(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getString(6), cursor.getLong(7), cursor.getString(8));
        }
        cursor.close();
        db.close();
        return model;
    }
}
