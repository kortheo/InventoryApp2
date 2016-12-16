package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = InventoryActivity.class.getName();

    private InventoryCursorAdapter mCursorAdapter;

    /**
     * ID for inventory loader object
     */
    private static final int INVENTORY_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        //get a handle for the listview and empty view and set an empty view on it
        ListView listView = (ListView) findViewById(R.id.inventory_list_view);
        View emptyView = findViewById(R.id.empty_list_text);
        listView.setEmptyView(emptyView);

        //initialize mCursorAdapter and set it on the list view
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //construct URI to pass with intent
                Uri productUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                //Create EditorActivity intent, passing along title info and URI
                Intent intent = new Intent(InventoryActivity.this, EditProductActivity.class);

                //set URI on the data field of the intent
                intent.setData(productUri);

                //start intent
                startActivity(intent);
            }
        });

        // Setup FAB to open AddProductActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

//        //Setup OnClickListener for sales button
//        Button salesButton = (Button) findViewById(R.id.inventory_sales_button);
//        salesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                sellProduct();
//            }
//
//        });

        //Kick off the loader
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(INVENTORY_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Cursor parameters
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryEntry.COLUMN_INVENTORY_PICTURE,
        };

        String selection = null;
        String[] selectionArgs = null;

        return new CursorLoader(
                this,            // Parent activity context
                InventoryEntry.CONTENT_URI, // Table to query
                projection,            // Projection to return
                selection,            //  Selection clause
                selectionArgs,            // Selection arguments
                null             // Default sort order

        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    /*
     * Clears out the adapter's reference to the Cursor.
     * This prevents memory leaks.
     */
        mCursorAdapter.swapCursor(null);
    }
}
