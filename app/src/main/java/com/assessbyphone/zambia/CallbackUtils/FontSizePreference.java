package com.assessbyphone.zambia.CallbackUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/* FontTypePreference
 * 		Special file type preference so that each option is actually
 * 		an example of the font. */
public class FontSizePreference extends DialogPreference {
    private List<String> fonts = null;
    private int selected;

    // This is the constructor called by the inflater
    public FontSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // figure out the current size.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String font = sharedPref.getString("fontsize", "Medium");

        switch (font) {
            case "Extra Small":
                selected = 0;
                break;
            case "Small":
                selected = 1;
                break;
            case "Medium":
                selected = 2;
                break;
            case "Large":
                selected = 3;
                break;
            case "Huge":
                selected = 4;
                break;
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // Data has changed, notify so UI can be refreshed!
        builder.setTitle("Choose a font type");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // save the choice in the preferences
                Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

                if (selected == 0)
                    editor.putString("fontsize", "Extra Small");
                else if (selected == 1)
                    editor.putString("fontsize", "Small");
                else if (selected == 2)
                    editor.putString("fontsize", "Medium");
                else if (selected == 3)
                    editor.putString("fontsize", "Large");
                else if (selected == 4)
                    editor.putString("fontsize", "Huge");

                editor.apply();

                notifyChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // load the font names and create the adapter
        String[] arrayOfFonts = {"Extra Small", "Small", "Medium", "Large", "Huge"};
        fonts = Arrays.asList(arrayOfFonts);

        FontTypeArrayAdapter adapter = new FontTypeArrayAdapter(getContext(), android.R.layout.simple_list_item_single_choice, fonts);
        builder.setSingleChoiceItems(adapter, selected, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // make sure we know what is selected
                selected = which;
            }
        });
    } // onPrepareDialogBuilder()


    /********************************************************************
     * class FontTypeArrayAdapter
     * 		Array adapter for font type picker */
    public static class FontTypeArrayAdapter extends ArrayAdapter<String> {
        // just a basic constructor
        public FontTypeArrayAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);

        } // end constructor one

        /****************************************************************
         * getView
         * 		the overroad getView method */
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the view that would normally be returned
            View v = super.getView(position, convertView, parent);
            final TextView tv = (TextView) v;


            final String option = tv.getText().toString();
            switch (option) {
                case "Extra Small":
                    tv.setTextSize(12.0f);
                    break;
                case "Small":
                    tv.setTextSize(16.0f);
                    break;
                case "Medium":
                    tv.setTextSize(20.0f);
                    break;
                case "Large":
                    tv.setTextSize(24.0f);
                    break;
                case "Huge":
                    tv.setTextSize(28.0f);
                    break;
            }

            // general options
            tv.setTextColor(Color.BLACK);
            tv.setPadding(10, 3, 3, 3);

            return v;
        } // end getView()

    } // end class FontTypeArrayAdapter

} // end class ClearListPreference