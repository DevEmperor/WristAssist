package net.devemperor.chatgpt.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import net.devemperor.chatgpt.adapters.ChatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context) {
        super(context, "chatHistory.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE CHAT_HISTORY_TABLE (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public long add(Context context, ChatHistoryModel entry) throws JSONException, IOException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TITLE", entry.getTitle());
        long id = db.insert("CHAT_HISTORY_TABLE", null, cv);

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

    public void delete(ChatHistoryModel entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("DELETE FROM CHAT_HISTORY_TABLE WHERE ID=" + entry.getId(), null);
        cursor.moveToFirst();
        cursor.close();
        db.close();
    }
}
