package net.devemperor.chatgpt.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import net.devemperor.chatgpt.items.ChatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context context;

    public DatabaseHelper(@Nullable Context context) {
        super(context, "chatHistory.db", null, 3);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE CHAT_HISTORY_TABLE (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT, LAST_EDIT LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            List<Long> ids = new ArrayList<>();
            Cursor cursor = db.rawQuery("SELECT ID FROM CHAT_HISTORY_TABLE", null);
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
            cursor.close();

            if (context == null) { return; }
            for (File file : Objects.requireNonNull(context.getFilesDir().listFiles())) {
                if (file.getName().startsWith("chat_")) {
                    long id = Long.parseLong(file.getName().substring(5, file.getName().length() - 5));
                    if (!ids.contains(id)) {
                        file.delete();
                    }
                }
            }
        }
        if (oldVersion < 3 && newVersion == 3) {
            db.execSQL("ALTER TABLE CHAT_HISTORY_TABLE ADD COLUMN LAST_EDIT LONG");

            List<Long> ids = new ArrayList<>();
            Cursor cursor = db.rawQuery("SELECT ID FROM CHAT_HISTORY_TABLE", null);
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
            cursor.close();

            for (Long id : ids) {
                ContentValues cv = new ContentValues();
                cv.put("LAST_EDIT", 0);
                db.update("CHAT_HISTORY_TABLE", cv, "ID=" + id, null);
            }
        }
    }

    public long add(Context context, ChatHistoryModel entry) throws JSONException, IOException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TITLE", entry.getTitle());
        cv.put("LAST_EDIT", System.currentTimeMillis());
        long id = db.insert("CHAT_HISTORY_TABLE", null, cv);
        db.close();

        JSONArray chatObject = new JSONArray();
        for (ChatItem chatItem : entry.getChatItems()) {
            JSONObject chatItemObject = new JSONObject();
            chatItemObject.put("role", chatItem.getChatMessage().getRole());
            chatItemObject.put("content", chatItem.getChatMessage().getContent());
            chatItemObject.put("cost", chatItem.getTotalCost());
            chatObject.put(chatItemObject);
        }
        String filePath = context.getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        out.write(chatObject.toString());
        out.close();

        return id;
    }

    public void edit(Context context, long id, ChatItem item) throws IOException, JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("LAST_EDIT", System.currentTimeMillis());
        db.update("CHAT_HISTORY_TABLE", cv, "ID=" + id, null);
        db.close();

        String filePath = context.getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        JSONArray chatObject = new JSONArray(in.readLine());
        in.close();

        JSONObject chatItemObject = new JSONObject();
        chatItemObject.put("role", item.getChatMessage().getRole());
        chatItemObject.put("content", item.getChatMessage().getContent());
        chatItemObject.put("cost", item.getTotalCost());
        chatObject.put(chatItemObject);

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        out.write(chatObject.toString());
        out.close();
    }

    public void delete(Context context, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("DELETE FROM CHAT_HISTORY_TABLE WHERE ID=" + id, null);
        cursor.moveToFirst();
        cursor.close();
        db.close();

        String filePath = context.getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
        new File(filePath).delete();
    }

    public void reset(Context context, long id, List<ChatItem> items) throws IOException, JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("LAST_EDIT", System.currentTimeMillis());
        db.update("CHAT_HISTORY_TABLE", cv, "ID=" + id, null);
        db.close();

        String filePath = context.getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
        JSONArray chatObject = new JSONArray();

        for (ChatItem item : items) {
            JSONObject chatItemObject = new JSONObject();
            chatItemObject.put("role", item.getChatMessage().getRole());
            chatItemObject.put("content", item.getChatMessage().getContent());
            chatItemObject.put("cost", item.getTotalCost());
            chatObject.put(chatItemObject);
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        out.write(chatObject.toString());
        out.close();
    }

    public String getTitle(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TITLE FROM CHAT_HISTORY_TABLE WHERE ID=" + id, null);
        cursor.moveToFirst();
        String title = cursor.getString(0);
        cursor.close();
        db.close();
        return title;
    }

    public void setTitle(long id, String newTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TITLE", newTitle);
        cv.put("LAST_EDIT", System.currentTimeMillis());
        db.update("CHAT_HISTORY_TABLE", cv, "ID=" + id, null);
        db.close();
    }

    public long getModified(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT LAST_EDIT FROM CHAT_HISTORY_TABLE WHERE ID=" + id, null);
        cursor.moveToFirst();
        long lastEdit = cursor.getLong(0);
        cursor.close();
        db.close();
        return lastEdit;
    }

    public long getChatCost(Context context, long id) throws IOException, JSONException {
        String filePath = context.getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        JSONArray chatObject = new JSONArray(in.readLine());
        in.close();

        long totalCost = 0;
        for (int i = 0; i < chatObject.length(); i++) {
            totalCost += chatObject.getJSONObject(i).getLong("cost");
        }
        return totalCost;
    }

    public List<ChatHistoryModel> getAllChats() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CHAT_HISTORY_TABLE ORDER BY LAST_EDIT DESC", null);
        List<ChatHistoryModel> chatHistoryModels = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                chatHistoryModels.add(new ChatHistoryModel(cursor.getLong(0), cursor.getString(1), null));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chatHistoryModels;
    }
}
