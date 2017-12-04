package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Zsolt on 2017. 11. 10..
 */

public final class ProductContract {

    /** ContentProvider Name */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /** ContentProvider base URI */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Possible path (appended to base content URI for possible URI's) */
    public static final String PATH_PRODUCTS = "products";

    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.
     */
    private ProductContract() {}

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product item.
     */
    public static final class ProductEntry implements BaseColumns {

        /** The content URI to access the data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /** MIME type of the {@link #CONTENT_URI} for a list of products */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** MIME type of the {@link #CONTENT_URI} for a single product item */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product item (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Product name
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="product_name";

        /**
         * Product image
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_IMAGE ="product_image";

        /**
         * Product price
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_PRICE ="product_price";

        /**
         * Product quantity
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY ="product_quantity";

        /**
         * Supplier name
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME ="supplier_name";

        /**
         * Supplier e-mail address
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_EMAIL ="supplier_email";
    }
}
