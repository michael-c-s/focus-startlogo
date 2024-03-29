package com.assessbyphone.zambia.CallbackUtils;

import android.content.Context;
import android.preference.Preference;
import android.provider.SearchRecentSuggestions;
import android.util.AttributeSet;
import android.widget.Toast;

import com.assessbyphone.zambia.R;

/* ClearSearchSuggestions
 * 		The Special clear recent search suggestions
 * 		Needs its own special class so you can just click on it */
public class ClearSearchSuggestions extends Preference {
    // This is the constructor called by the inflater
    public ClearSearchSuggestions(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        // Data has changed, notify so UI can be refreshed!
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getContext(),
                SearchSuggestions.AUTHORITY, SearchSuggestions.MODE);
        suggestions.clearHistory();

        Toast.makeText(getContext(), R.string.onSuggestionsCleared, Toast.LENGTH_LONG).show();
        notifyChanged();
    }
} // end class ClearSearchSuggestions