package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zsolt on 2017. 11. 13..
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    /**
     * Identifier for the product data loader
     */
    private static final int PRODUCT_LOADER = 1;

    /**
     * UI components
     */
    private TextView mTextViewProduct;
    private TextView mTextViewPrice;
    private TextView mTextViewQuantity;
    private TextView mTextViewSupplier;
    private TextView mTextViewSupplierEmail;
    private Button mButtonDecrement;
    private Button mButtonIncrement;
    private ImageButton mImageButtonEmail;
    private ImageView mImageViewImage;

    private int quantity;

    private Uri mProductUri;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Set the title of this activity
        setTitle(getString(R.string.details_activity_title));

        // Find all relevant views that contain data to be displayed
        mTextViewProduct = (TextView) findViewById(R.id.details_product_name);
        mTextViewPrice = (TextView) findViewById(R.id.details_product_price);
        mTextViewQuantity = (TextView) findViewById(R.id.details_product_quantity);
        mTextViewSupplier = (TextView) findViewById(R.id.details_supplier_name);
        mTextViewSupplierEmail = (TextView) findViewById(R.id.details_supplier_email);
        mButtonDecrement = (Button) findViewById(R.id.details_button_decrement);
        mButtonIncrement = (Button) findViewById(R.id.details_button_increment);
        mImageButtonEmail = (ImageButton) findViewById(R.id.details_imagebutton_email);
        mImageViewImage = (ImageView) findViewById(R.id.details_product_image);

        Intent intent = getIntent();
        mProductUri = intent.getData();
        if (mProductUri != null) {
            // Initialize a loader to read the product data from the database
            // and display the current values
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,   // Parent activity context
                mProductUri,                    // Provider content URI to query
                null,                 // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int productColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            final String product = cursor.getString(productColumnIndex);
            final String image = cursor.getString(imageColumnIndex);
            final String price = cursor.getString(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            final String supplier = cursor.getString(supplierColumnIndex);
            final String email = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mTextViewProduct.setText(product);
            mTextViewPrice.setText(price);
            mTextViewQuantity.setText(String.valueOf(quantity));
            mTextViewSupplier.setText(supplier);
            mTextViewSupplierEmail.setText(email);
            mImageUri = Uri.parse(image);
            ViewTreeObserver viewTreeObserver = mImageViewImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageViewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageViewImage.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });

            mImageButtonEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create a new intent to call implicit intent email
                    Intent intent = new Intent(Intent.ACTION_SENDTO); // Only email apps can handle
                    intent.setData(Uri.parse("mailto:" + mTextViewSupplierEmail.getText()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, mTextViewProduct.getText());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            // Decrement the quantity of the product
            mButtonDecrement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decrement(mProductUri, quantity);
                }
            });

            // Increment the quantity of the product
            mButtonIncrement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    increment(mProductUri, quantity);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTextViewProduct.setText("");
        mTextViewPrice.setText("");
        mTextViewQuantity.setText("");
        mTextViewSupplier.setText("");
        mTextViewSupplierEmail.setText("");
    }

    /**
     * The following code snippet is provided by Carlos Jimenez (@crlsndrsjmnz)
     * from https://github.com/crlsndrsjmnz/MyShareImageExample to display the resized image
     */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageViewImage.getWidth();
        int targetH = mImageViewImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Helper method to reduce the current quantity by one and update it
     */
    private void decrement(Uri uri, int currentQuantity) {
        if (currentQuantity == 0) {
            Toast.makeText(this, R.string.stock_zero, Toast.LENGTH_SHORT).show();
        } else {
            currentQuantity--;
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
            int rowsEffected = getContentResolver().update(uri, values, null, null);
        }
    }

    /**
     * Helper method to increase the current quantity by one and update it
     */
    private void increment(Uri uri, int currentQuantity) {
        currentQuantity++;
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
        int rowsEffected = getContentResolver().update(uri, values, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_details.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    /**
     * Handle the user's different menu option clicking
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Edit/update the current product -> open EditorActivity
            case R.id.action_edit:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(mProductUri);
                startActivity(intent);
                return true;

            // Delete the current product
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete the product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
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
     * Delete the product in the database.
     */
    private void deleteProduct(){
        // Only perform the delete if this is an existing product
        if (mProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.error_deleting_product), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}

