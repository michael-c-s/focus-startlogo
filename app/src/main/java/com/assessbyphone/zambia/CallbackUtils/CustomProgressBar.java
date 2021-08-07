package com.assessbyphone.zambia.CallbackUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.assessbyphone.zambia.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CustomProgressBar {
    private Dialog dialog;

    public Dialog showMe(Context context) {
        return showMe(context, null);
    }

    public Dialog showMe(Context context, String title) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.my_progress_design, null);

        TextView mProgressTv = view.findViewById(R.id.mProgressTv_ids);

        if (title != null)
            mProgressTv.setText(title);

        dialog = new Dialog(context, R.style.CustomProgressBarTheme);
        dialog.setContentView(view);
        dialog.show();

        return dialog;
    }

    public void dismissMe() {
        if (dialog != null)
            dialog.dismiss();
    }
}
