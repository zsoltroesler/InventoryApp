package com.example.android.inventoryapp;

import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zsolt on 2017. 11. 10..
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Code for the image request
     */
    private static final int IMAGE_REQUEST_CODE = 0;

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * State of image URI
     */
    private static final String IMAGE_STATE_URI = "IMAGE_STATE_URI";

    /**
     * UI components
     */
    private EditText mEditTextProduct;
    private EditText mEditTextPrice;
    private EditText mEditTextStock;
    private EditText mEditTextSupplier;
    private TextView mEditTextSupplierEmail;
    private Button mButtonAddImage;
    private ImageView mImageViewImage;


    private Uri mProductUri;

    private Uri mImageUri;

    private int quantity;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    /**
     * Boolean flag that keeps track of whether the product has all the necessary information
     * (true) or not (false)
     */
    private boolean mProductHasAllData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mProductUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mEditTextProduct = (EditText) findViewById(R.id.editor_product_name);
        mEditTextPrice = (EditText) findViewById(R.id.editor_product_price);
        mEditTextStock = (EditText) findViewById(R.id.editor_product_quantity);
        mEditTextSupplier = (EditText) findViewById(R.id.editor_supplier_name);
        mEditTextSupplierEmail = (EditText) findViewById(R.id.editor_supplier_email);
        mButtonAddImage = (Button) findViewById(R.id.button_add_image);
        mImageViewImage = (ImageView) findViewById(R.id.editor_product_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mEditTextProduct.setOnTouchListener(mTouchListener);
        mEditTextPrice.setOnTouchListener(mTouchListener);
        mEditTextStock.setOnTouchListener(mTouchListener);
        mEditTextSupplier.setOnTouchListener(mTouchListener);
        mEditTextSupplierEmail.setOnTouchListener(mTouchListener);

        // Setuo OnClickListener on the "Add Photo" button
        mButtonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImageToProduct();
                mProductHasChanged = true;
            }
        });

        // If the intent does not contain a product content URI, then we know that we are
        // creating a new product.
        if (mProductUri == null) {
            // This is a new product, so change the app bar to say "Add Product"
            setTitle(getString(R.string.editor_activity_title_add_product));
            // Set button text to "Add photo"
            mButtonAddImage.setText(R.string.add_photo);

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));
            // Set button text to "Change photo"
            mButtonAddImage.setText(R.string.change_photo);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    /**
     * Load the cursor with records from database
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the products table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mProductUri,                    // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    /**
     * Called when cursor has finished loading
     */
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
            String product = cursor.getString(productColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String email = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mEditTextProduct.setText(product);
            mImageUri = Uri.parse(image);
            mImageViewImage.setImageBitmap(getBitmapFromUri(mImageUri));
            mEditTextPrice.setText(price);
            mEditTextStock.setText(String.valueOf(quantity));
            mEditTextSupplier.setText(supplier);
            mEditTextSupplierEmail.setText(email);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextProduct.setText("");
        mEditTextPrice.setText("");
        mEditTextStock.setText("");
        mEditTextSupplier.setText("");
        mEditTextSupplierEmail.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving status of product update for rotating the device
        outState.putBoolean("productHasChanged", mProductHasChanged);

        if (mImageUri != null)
            outState.putString(IMAGE_STATE_URI, mImageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the status of updates if we rotated the device
        mProductHasChanged = savedInstanceState.getBoolean("productHasChanged");

        if (savedInstanceState.containsKey(IMAGE_STATE_URI) &&
                !savedInstanceState.getString(IMAGE_STATE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(IMAGE_STATE_URI));

            ViewTreeObserver viewTreeObserver = mImageViewImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageViewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageViewImage.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });
        }
    }

    /**
     * Pick the corresponding image from the device's gallery
     */
    public void addImageToProduct() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select image"), IMAGE_REQUEST_CODE);
    }

    /**
     * This method delivers the image as result and put into mImageViewImage
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                mImageViewImage.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Handle the user's different menu option clicking
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Local variable to navigate the user always back to CatalogActivity after saving
        // even though EditorActivity was opened from DetailsActivity.
        Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);

        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct()) {
                    NavUtils.navigateUpTo(EditorActivity.this, intent);
                }
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private boolean saveProduct() {
        // Read from input fields. Use trim to eliminate leading or trailing white space
        String productString = mEditTextProduct.getText().toString().trim();
        String priceString = mEditTextPrice.getText().toString().trim();
        String supplierString = mEditTextSupplier.getText().toString().trim();
        String supplierEmailString = mEditTextSupplierEmail.getText().toString().trim();
        String quantityString = mEditTextStock.getText().toString();

        // Check if this is supposed to be a new product and check if all the fields in the editor are blank.
        // If so we can return earlier without creating a new row in the database for no values
        if (mProductUri == null &&
                TextUtils.isEmpty(productString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                TextUtils.isEmpty(quantityString) &&
                mImageUri == null) {
            mProductHasAllData = true;
            return mProductHasAllData;
        }

        // Create a ContentValues object where column names are the keys
        ContentValues values = new ContentValues();

        // Following the user would be warned to add product attributes if he left empty these fields
        // and these attributes will be the values for ContentValues object
        if (TextUtils.isEmpty(productString)) {
            Toast.makeText(this, getString(R.string.add_product_name), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productString);

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.add_product_price), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.add_product_stock), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, getString(R.string.add_supplier_name), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierString);

        if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, getString(R.string.add_supplier_email), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.add_product_image), Toast.LENGTH_SHORT).show();
            return mProductHasAllData;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());

        if (mProductUri == null) {
            // Insert a new product into the provider, returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.error_saving_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing product, so update it with content URI: mProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_saving_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (mProductHasChanged) {
                    Toast.makeText(this, getString(R.string.product_saved),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        mProductHasAllData = true;
        return mProductHasAllData;
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}