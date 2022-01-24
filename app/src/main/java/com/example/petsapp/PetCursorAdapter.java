package com.example.petsapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* Flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View newView = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        return newView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView rvName = view.findViewById(R.id.rv_name);
        TextView rvSummary = view.findViewById(R.id.rv_summary);

        int nameColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);

        String petName = cursor.getString(nameColumn);
        String petBreed = cursor.getString(breedColumn);

        rvName.setText(petName);
        if (TextUtils.isEmpty(petBreed)) {
            rvSummary.setText(R.string.unknow_breed_rv);
        } else {
            rvSummary.setText(petBreed);
        }
    }
}
