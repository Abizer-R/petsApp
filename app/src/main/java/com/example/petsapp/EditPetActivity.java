package com.example.petsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetContract.PetEntry;

public class EditPetActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = EditPetActivity.class.getSimpleName();

    private final int PET_LOADER = 2;

    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;
    
    private int mGender = 0;

    private Uri currPetUri;

    private boolean mPetHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        Intent intent = getIntent();
        currPetUri = intent.getData();

        if(currPetUri == null) { {
            getSupportActionBar().setTitle(R.string.editor_activity_title_new_pet);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        } else {
            getSupportActionBar().setTitle(R.string.editor_activity_title_edit_pet);
            getLoaderManager().initLoader(PET_LOADER, null, this);
        }
    }

    private void setupSpinner() {


        ArrayAdapter<CharSequence> genderSpinnerAdapter
                = ArrayAdapter.createFromResource(
                this,
                R.array.array_gender_options,
                android.R.layout.simple_spinner_dropdown_item
        );

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = adapterView.getItemAtPosition(i).toString();
                if(!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male)))
                        mGender = PetEntry.GENDER_MALE;
                    else if (selection.equals(getString(R.string.gender_female)))
                        mGender = PetEntry.GENDER_FEMALE;
                    else
                        mGender = PetEntry.GENDER_UNKNOWN;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGender = 0;
            }
        });
    }

    /**
     * Get user unput from editor and save new pet into database
     */
    private void SavePet() {
        // .trim() eliminates any leading or trailing white space from the string that we got
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // App will crash if weight is not provided by the user
        // So, we need to check before parsing, and if its blank, we will use 0 as default
        int weightInt = 0;
        if(!TextUtils.isEmpty(weightString))
            weightInt = Integer.parseInt(weightString);

        if(currPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weightInt);

        if(currPetUri == null) {
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if(newUri == null)
                Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();

        } else {

            int rowsUpdated = getContentResolver().update(currPetUri,
                    values,
                    null,
                    null);

            if(rowsUpdated == 0)
                Toast.makeText(this, R.string.editor_update_pet_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.editor_update_pet_successful, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardBtnClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardBtnClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardBtnClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardBtnClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editpage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                SavePet();
                finish(); // Exit Activity
                return true;

            case R.id.action_delete:
                Toast.makeText(this, "delete option clicked", Toast.LENGTH_SHORT).show();
                return true;

            case android.R.id.home:
                if(!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardBtnClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditPetActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardBtnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "delete" menu item.
        if (currPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projections = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        Uri uri = getIntent().getData();
        return new CursorLoader(this,
                uri,
                projections,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // move cursor one row ahead to get it on 0th position
        // And it should be the only row
        if(cursor.moveToNext()) {
            int nameCol = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedCol = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderCol = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightCol = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String currPetName = cursor.getString(nameCol);
            String currPetBreed = cursor.getString(breedCol);
            int currPetWeightInt = cursor.getInt(weightCol);
            int currPetGenderInt = cursor.getInt(genderCol);

            mNameEditText.setText(currPetName);
            mBreedEditText.setText(currPetBreed);
            mWeightEditText.setText(String.valueOf(currPetWeightInt));

            mGenderSpinner.setSelection(currPetGenderInt);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
    }
}