package com.example.petsapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.petsapp.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {
    public static final String TAG = PetDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shelter.db";

    public PetDbHelper(@Nullable Context context) {
        /**
         * In constructor below:
         * public PetDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
         *         super(context, name, factory, version);
         *     }
         * Erase name, factory and version from arguments because we will define those in our class ourself.
         * So here name, factory and version will be replaced as follows:
         * name -> DATABASE_NAME
         * factory -> null
         * version -> DATABASE_VERSION
         */
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetEntry.TABLE_NAME + "("
                + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
