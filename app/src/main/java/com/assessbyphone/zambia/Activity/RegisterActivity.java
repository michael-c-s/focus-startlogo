package com.assessbyphone.zambia.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.assessbyphone.zambia.CallbackUtils.DefaultCallback;
import com.assessbyphone.zambia.R;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class RegisterActivity extends Activity {
    private final static java.text.SimpleDateFormat SIMPLE_DATE_FORMAT = new java.text.SimpleDateFormat("yyyy/MM/dd");
    private EditText emailField;
    private EditText nameField;
    private EditText passwordField;
    private EditText utnField;
    private Button registerButton;
    private String email;
    private String name;
    private String password;
    private String utn;
    private BackendlessUser user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        initUI();
    }

    private void initUI() {
        emailField = findViewById(R.id.emailField);
        nameField = findViewById(R.id.nameField);
        passwordField = findViewById(R.id.passwordField);
        utnField = findViewById(R.id.utnField);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterButtonClicked();
            }
        });
    }

    private void onRegisterButtonClicked() {
        String emailText = emailField.getText().toString().trim();
        String nameText = nameField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();
        String utnText = utnField.getText().toString().trim();

        if (emailText.equals("")) {
            showToast("Field 'email' cannot be empty.");
            return;
        }

        if (passwordText.equals("")) {
            showToast("Field 'password' cannot be empty.");
            return;
        }

        if (!emailText.equals("")) {
            email = emailText;
        }

        if (!nameText.equals("")) {
            name = nameText;
        }

        if (!passwordText.equals("")) {
            password = passwordText;
        }

        if (!utnText.equals("")) {
            utn = utnText;
        }

        user = new BackendlessUser();

        if (email != null) {
            user.setProperty("email", email);
        }

        if (name != null) {
            user.setProperty("name", name);
        }

        if (password != null) {
            user.setProperty("password", password);
        }

        if (utn != null) {
            user.setProperty("utn", utn);
        }

        Backendless.UserService.register(user, new DefaultCallback<BackendlessUser>(RegisterActivity.this) {
            @Override
            public void handleResponse(BackendlessUser response) {
                super.handleResponse(response);
                startActivity(new Intent(RegisterActivity.this, RegistrationSuccessActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
