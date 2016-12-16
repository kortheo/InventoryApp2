package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;

/**
 * Created by travi on 10/15/2016.
 */

public class EditProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static String LOG_TAG = EditProductActivity.class.getSimpleName();

    /**
     * variables for edit text fields
     */
    private EditText mNameTextView;
    private TextView mQuantityTextView;
    private EditText mPriceTextView;
    private EditText mSupplierTextView;
    private ImageView mImageView;
    private String mImagePath;

    private final static int EDIT_LOADER = 1;


    Uri mProductUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //get handles for EditText fields
        mNameTextView = (EditText) findViewById(R.id.edit_add_name);
        mQuantityTextView = (TextView) findViewById(R.id.edit_add_quantity);
        mPriceTextView = (EditText) findViewById(R.id.edit_add_price);
        mSupplierTextView = (EditText) findViewById(R.id.edit_add_supplier);
        mImageView = (ImageView) findViewById(R.id.edit_picture_image);

        //Get data from intent that started activity.
        Intent intent = getIntent();

        //get URI from intent data field and populate views with existing product data from database
        mProductUri = intent.getData();

        //Setup OnClickListener for sales button
        Button salesButton = (Button) findViewById(R.id.edit_sale_button);
        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellProduct();
            }

        });

        //Setup OnClickListener for receive button
        Button receiveButton = (Button) findViewById(R.id.edit_receive_button);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveProduct();
            }
        });

        //Setup OnClickListener for order email button
        Button orderButton = (Button) findViewById(R.id.edit_order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launches email app to email supplier
                orderProductEmail();
            }
        });


        //Kick off the loader
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(EDIT_LOADER, null, this);
    }

    public void sellProduct() {

        //get current quantity of product
        String currentQuantity = mQuantityTextView.getText().toString();

        //Convert string quantity value to integer
        int quantityInt = Integer.parseInt(currentQuantity);

        //decrement quantity by 1
        if (quantityInt > 0) {
            quantityInt = quantityInt - 1;
        } else {
            Toast.makeText(this, getString(R.string.decrement_error),
                    Toast.LENGTH_SHORT).show();

        }

        //convert back to string
        currentQuantity = Integer.toString(quantityInt);

        //set quantity back on edit text view
        mQuantityTextView.setText(currentQuantity);
    }

    public void receiveProduct() {

        //get current quantity of product
        String currentQuantity = mQuantityTextView.getText().toString();

        //Convert string quantity value to integer
        int quantityInt = Integer.parseInt(currentQuantity);

        //increment quantity by 1
        quantityInt = quantityInt + 1;

        //convert back to string
        currentQuantity = Integer.toString(quantityInt);

        //set quantity back on edit text view
        mQuantityTextView.setText(currentQuantity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.edit_product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                //get text from fields
                String nameText = mNameTextView.getText().toString();
                String quantityText = mQuantityTextView.getText().toString();
                String priceText = mPriceTextView.getText().toString();
                String supplierText = mSupplierTextView.getText().toString();

                //if not all values are present, notify the user with a toast message
                if (TextUtils.isEmpty(nameText) || TextUtils.isEmpty(quantityText) || TextUtils.isEmpty(priceText) || TextUtils.isEmpty(supplierText)) {
                    String toastString = getResources().getString(R.string.edit_product_toast_warning);
                    Toast toast = Toast.makeText(this, toastString, Toast.LENGTH_LONG);
                    toast.show();
                    return true;
                }

                //Save product to the database
                saveProduct(nameText, quantityText, priceText, supplierText);
                //Exit activity
                finish();
                return true;
            case R.id.action_delete:
                //delete current product from database
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        //delete current product from database
        int rowsDeleted = getContentResolver().delete(mProductUri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.delete_product_toast_error),
                    Toast.LENGTH_LONG).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_product_toast_success),
                    Toast.LENGTH_LONG).show();
        }

        finish();
    }

    public void saveProduct(String nameText, String quantityText, String priceText, String supplierText) {

        //put values into ContentValues object
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameText);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityText);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceText);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierText);

        //use content resolver to add insert values into database. check if operation was successful or not.
        int rowsUpdated = getContentResolver().update(mProductUri, values, null, null);

        //Create toast message confirming product updated/inserted successfully.
        //If error, newUri will be null, and the toast will show an error message.
        int toastLength = Toast.LENGTH_LONG;
        String toastString = "";

        //rowsUpdated will =1 if successful, otherwise error.
        if (rowsUpdated != 1) {
            toastString = getResources().getString(R.string.update_product_toast_error);
        } else {
            toastString = getResources().getString(R.string.update_product_toast_success);
        }

        Toast toast = Toast.makeText(this, toastString, toastLength);
        toast.show();

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
                mProductUri, // Table to query
                projection,            // Projection to return
                selection,            //  Selection clause
                selectionArgs,            // Selection arguments
                null             // Default sort order

        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //move cursor to 0th position; there should only be one row in this cursor. set values from cursor data
        if (data.moveToFirst()) {
            mNameTextView.setText(data.getString(data.getColumnIndex("name")));
            mQuantityTextView.setText(data.getString(data.getColumnIndex("quantity")));
            mPriceTextView.setText(data.getString(data.getColumnIndex("price")));
            mSupplierTextView.setText(data.getString(data.getColumnIndex("supplier")));
            mImagePath = data.getString(data.getColumnIndex("picture"));
            setPic();

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //on loader reset, set all fields to empty string
        mNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        mSupplierTextView.setText("");
        //clear image view
        mImageView.setImageResource(0);

    }

    public void orderProductEmail() {

        //gather strings to be filled into email
        String subject = getResources().getString(R.string.order_email_subject);
        String supplier = mSupplierTextView.getText().toString();
        String body = getResources().getString(R.string.order_email_body) + " " + mNameTextView.getText().toString();

        //create intent to launch email app
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplier});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setPic() {

        if (mImagePath != null) {

            //scale factor to size the image by
            int scaleFactor;

            // Get the dimensions of the View
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImagePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            if (targetW == 0 && targetH == 0){
                scaleFactor = 1;
            } else {
                // Determine how much to scale down the image
                scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            }

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, bmOptions);
            mImageView.setImageBitmap(bitmap);
        }

    }

}
