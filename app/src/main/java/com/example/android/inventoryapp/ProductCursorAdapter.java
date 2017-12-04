package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Zsolt on 2017. 11. 10..
 */

/**
 * {@link ProductCursorAdapter} is an adapter for a list that uses a {@link Cursor} of product data
 * as its data source. This adapter knows how to create list items for each row of product data
 * in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    private static Context mContext;

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    /**
     * Create the ViewHolder class for references
     */
    public class ViewHolder {
        private ImageView imageView;
        private TextView nameView;
        private TextView priceView;
        private TextView quantityView;
        private ImageButton cartView;

        // Add a public constructor, instantiate all of the references to the private variables
        public ViewHolder(View view) {

            imageView = (ImageView) view.findViewById(R.id.list_product_image);
            nameView = (TextView) view.findViewById(R.id.list_product_name);
            priceView = (TextView) view.findViewById(R.id.list_product_price);
            quantityView = (TextView) view.findViewById(R.id.list_product_quantity);
            cartView = (ImageButton) view.findViewById(R.id.list_cart_image);
        }
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
        view.setTag(viewholder);

        return view;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Set data to respective views within ListView
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        String productPrice = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        final int productQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));

        // Get the image resource path as String and than parse it as a URI
        String productImage = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
        Uri imageUri = Uri.parse(productImage);

        viewHolder.imageView.setImageURI(imageUri);

        viewHolder.nameView.setText(productName);
        viewHolder.priceView.setText(productPrice);
        viewHolder.quantityView.setText(String.valueOf(productQuantity));

        // If cart ImageButton is clicked the current quantity is reduced by one
        viewHolder.cartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                reduceQuantity(context, productUri, productQuantity);
            }
        });
    }

    /**
     * Helper method to reduce the current quantity by one and update it
     */
    private void reduceQuantity(Context context, Uri uri, int currentQuantity) {
        if (currentQuantity == 0) {
            Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
        } else {
            currentQuantity--;
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
            int rowsEffected = context.getContentResolver().update(uri, values, null, null);
        }
    }
}

