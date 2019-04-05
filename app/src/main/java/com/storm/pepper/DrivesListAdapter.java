package com.storm.pepper;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.storm.posh.plan.planelements.drives.DriveCollection;

import java.util.List;

public class DrivesListAdapter extends ArrayAdapter {
    private final Activity context;
    private final List<DriveCollection> drives;
    private final DriveCollection currentDrive;

    public DrivesListAdapter(Activity context, List<DriveCollection> drives, DriveCollection currentDrive){

        super(context, R.layout.drives_row, drives);

        this.context = context;
        this.drives = drives;
        this.currentDrive = currentDrive;
    }

    public boolean isStale(DriveCollection newDrive) {
        return (currentDrive != newDrive);
    }

    public View getView(int position, View view, ViewGroup parent) {
        DriveCollection drive = drives.get(position);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.drives_row, null,true);

        // this code gets references to objects in the row xml file
        TextView driveName = rowView.findViewById(R.id.driveName);
        TextView driveNotes = rowView.findViewById(R.id.driveNotes);
        driveNotes.setText("");

        //this code sets the values of the objects to values from the arrays
        driveName.setText(drive.getNameOfElement());
        if (currentDrive != null && drive.getNameOfElement() == currentDrive.getNameOfElement()) {
            driveName.setTypeface(null, Typeface.BOLD);
        } else {
            driveName.setTypeface(null, Typeface.NORMAL);
        }

        return rowView;
    }
}
