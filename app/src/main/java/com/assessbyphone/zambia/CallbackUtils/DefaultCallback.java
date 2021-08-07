package com.assessbyphone.zambia.CallbackUtils;

import android.content.Context;
import android.widget.Toast;

import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

public class DefaultCallback<T> extends BackendlessCallback<T> {
    public Context context;

    public DefaultCallback(Context context) {
        this.context = context;
    }

    public DefaultCallback(Context context, String message) {
        this.context = context;
    }

    @Override
    public void handleResponse(T response) {

    }

    @Override
    public void handleFault(BackendlessFault fault) {
        Toast.makeText(context, fault.getMessage(), Toast.LENGTH_LONG).show();
    }
}
