package com.storm.pepper;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.storm.posh.plan.planelements.PlanElement;

import java.util.List;

public class ElementsListAdapter extends ArrayAdapter {
    private final Activity context;
    private final List<PlanElement> elements;

    public ElementsListAdapter(Activity context, List<PlanElement> elements){

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
        elementName.setText(elements.get(position).getNameOfElement());
        elementNotes.setText("");

        if ((position + 1) == elements.size()) {
            elementName.setTypeface(null, Typeface.BOLD);
        } else {
            elementName.setTypeface(null, Typeface.NORMAL);
        }

        return rowView;

    };
}
