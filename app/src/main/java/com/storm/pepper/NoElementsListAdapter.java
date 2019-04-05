package com.storm.pepper;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NoElementsListAdapter extends ArrayAdapter {
    private final Activity context;
    private final List<String> elements;

    public NoElementsListAdapter(Activity context, List<String> elements){

        super(context, R.layout.elements_row, elements);

        this.context = context;
        this.elements = elements;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.elements_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView elementName = (TextView) rowView.findViewById(R.id.elementName);
        TextView elementNotes = (TextView) rowView.findViewById(R.id.elementNotes);

        //this code sets the values of the objects to values from the arrays
        elementName.setText(elements.get(position));

        return rowView;

    };
}
