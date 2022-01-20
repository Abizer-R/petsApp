package com.example.petsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetContract.PetEntry;
import com.example.petsapp.data.PetDbHelper;

import java.util.Locale;

public class EditPetActivity extends AppCompatActivity {

    private final String TAG = EditPetActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;
    
    private int mGender = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);
        
        setupSpinner();
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
    private void insertPet() {
        // .trim() eliminates any leading or trailing white space from the string that we got
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weightInt = Integer.parseInt(weightString);

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weightInt);

        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        if(newUri == null)
            Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
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
                insertPet();
                finish(); // Exit Activity
                return true;

            case R.id.action_delete:
                Toast.makeText(this, "delete option clicked", Toast.LENGTH_SHORT).show();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}