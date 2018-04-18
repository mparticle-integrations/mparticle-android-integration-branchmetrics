package com.mparticle.branchsample.Activities;

import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.mparticle.MPEvent;
import com.mparticle.branchsample.R;
import com.mparticle.MParticle;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    EditText mUserName;
    Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserName = (EditText)findViewById(R.id.email);
        mUserName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSubmit.setEnabled(s.length() > 0);
            }
        });
        mSubmit = (Button)findViewById(R.id.email_sign_in_button);
        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               // MParticle.getInstance().setUserIdentity(mUserName.getText().toString(), MParticle.IdentityType.CustomerId);
                MPEvent event = new MPEvent.Builder("Log in", MParticle.EventType.UserContent)
                        .duration(300)
                        .category("Signin/Signout").build();
                MParticle.getInstance().logEvent(event);
                startActivity(new Intent(LoginActivity.this, SecondActivity.class));
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mUserName.setText("");
    }
}

