package com.example.petsapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.petsapp.data.PetContract.PetEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.security.auth.login.LoginException;


public class PetProvider extends ContentProvider {

    public static final String TAG = PetProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null,null, sortOrder );
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                int idNumber = (int) ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(idNumber)};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder );
                break;
            default:
                throw new IllegalArgumentException("Cannot query Unknown URI: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name.");
        }

        // 'int' can also be used below
        Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires a valid gender.");
        }

        // Here null is acceptable, but negative weight isn't
        Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet's weight cannot be negative.");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetEntry.TABLE_NAME,null, contentValues);

        if(newRowId == -1) {
            Log.e(TAG, "insertPet: Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        if(contentValues == null || contentValues.size() == 0) {
            return 0;
        }

        /*
          It is possible not all attributes are being updated.
          So we have to check if a attribute is being updated, before ensuring data validation.
         */
        if(contentValues.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name.");
            }

        }

        if(contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            // 'int' can also be used below
            Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires a valid gender.");
            }
        }

        if(contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Here null is acceptable, but negative weight isn't
            Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet's weight cannot be negative.");
            }
        }

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                long idNumber = ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(idNumber)};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updatedRows = db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        return updatedRows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                long idNumber = ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(idNumber)};
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
}
