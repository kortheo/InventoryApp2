package com.example.android.inventoryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.*;
import java.util.Date;

import static android.R.attr.bitmap;

/**
 * Created by travi on 10/15/2016.
 */

public class AddProductActivity extends AppCompatActivity {

    private static String LOG_TAG = AddProductActivity.class.getName();

    /**
     * Create variables for EditText fields
     */
    private EditText nameTextView;
    private EditText quantityTextView;
    private EditText priceTextView;
    private EditText supplierTextView;
    private ImageView pictureImageView;
    private String mCurrentPhotoPath;

    //variable for function of camera intent
    private static final int REQUEST_TAKE_PHOTO = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        /** initialize EditText variables */
        nameTextView = (EditText) findViewById(R.id.edit_add_name);
        quantityTextView = (EditText) findViewById(R.id.edit_add_quantity);
        priceTextView = (EditText) findViewById(R.id.edit_add_price);
        supplierTextView = (EditText) findViewById(R.id.edit_add_supplier);
        pictureImageView = (ImageView) findViewById(R.id.add_picture_image);
        mCurrentPhotoPath = "";

        //Setup OnClickListener for take picture button
        Button takePictureButton = (Button) findViewById(R.id.add_take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launches camera app to take picture and save to disk, and set on imageview.
                dispatchTakePictureIntent();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.add_product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                //get text from fields
                String nameText = nameTextView.getText().toString();
                String quantityText = quantityTextView.getText().toString();
                String priceText = priceTextView.getText().toString();
                String supplierText = supplierTextView.getText().toString();

                //if no values have been entered on save, just close out the activity.
                if (TextUtils.isEmpty(nameText) && TextUtils.isEmpty(quantityText) && TextUtils.isEmpty(priceText) && TextUtils.isEmpty(supplierText)) {
                    finish();
                    return true;
                }

                //if not all values are present, notify the user with a toast message
                if (TextUtils.isEmpty(nameText) || TextUtils.isEmpty(quantityText) || TextUtils.isEmpty(priceText) || TextUtils.isEmpty(supplierText)) {
                    String toastString = getResources().getString(R.string.add_product_toast_warning);
                    Toast toast = Toast.makeText(this, toastString, Toast.LENGTH_LONG);
                    toast.show();
                    return true;
                }

                //Save product to the database
                saveProduct(nameText, quantityText, priceText, supplierText, mCurrentPhotoPath);

                //Exit activity
                finish();
                return true;
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveProduct(String nameText, String quantityText, String priceText, String supplierText, String picturePath) {

        //put values into ContentValues object
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameText);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityText);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceText);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierText);
        values.put(InventoryEntry.COLUMN_INVENTORY_PICTURE, picturePath);

        //use content resolver to add insert values into database
        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }

    /**
     * Camera code to save full image to file + save file URI / or path to database, and set image on the imageview in the add activity.
     */

    //launch the camera app
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                //create scaled bitmap from file and set on imageview
                setPic();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        }
    }

    //creates an image file at a location with a unique name. used to generate a file to fill with JPEG data from dispatchTakePictureIntent.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = pictureImageView.getWidth();
        int targetH = pictureImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        pictureImageView.setImageBitmap(bitmap);
    }

}
