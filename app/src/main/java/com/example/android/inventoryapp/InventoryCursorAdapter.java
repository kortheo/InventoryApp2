package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import static android.R.attr.id;
import static java.security.AccessController.getContext;
import com.example.android.inventoryapp.EditProductActivity;
import com.example.android.inventoryapp.data.InventoryContract;

import java.util.List;

/**
 * Created by travi on 10/14/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {


    /** Constructs an InventoryCursorAdapter object */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Get text views in list items to set values from cursor on
        TextView nameView = (TextView) view.findViewById(R.id.name);
        final TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        Button orderButton = (Button) view.findViewById(R.id.inventory_sales_button);

        //Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow("quantity"));
        String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));

        //Set values on text views
        nameView.setText(name);
        quantityView.setText(quantity);
        priceView.setText(price);

        //set listener on button
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout parentLayout = (LinearLayout) v.getParent();

                //get current quantity from list item
                TextView quantityTextView = (TextView)parentLayout.findViewById(R.id.quantity);
                String quantity = quantityTextView.getText().toString();

                //convert to int
                int quantityInt = Integer.parseInt(quantity);

                //decrement quantity by 1
                if (quantityInt > 0 ) {
                    quantityInt = quantityInt - 1;
                }

                //convert back to string
                quantity = Integer.toString(quantityInt);

                //set quantity back on edit text view
                quantityView.setText(quantity);

                ListView listView = (ListView) v.findViewById(R.id.inventory_list_view);
                Long productId = listView.getSelectedItemId();

                //construct URI to pass with intent
                Uri productUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, productId);

                //new values object
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

                //update the database with the new value
                context.getContentResolver().update(productUri, values, null, null);
            }
        });

    }

}


