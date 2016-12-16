package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by travi on 10/14/2016.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    
    private static final String LOG_TAG = InventoryDbHelper.class.getName();

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Inventory.db";

    /** Constructor */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PICTURE + " TEXT);";

        //log output for final SQL statement created from above string variable
        Log.d(LOG_TAG, SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS table_name");
    }
}
