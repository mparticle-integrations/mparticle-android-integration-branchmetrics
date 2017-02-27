package com.mparticle.branchsample.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mparticle.branchsample.R;
import com.mparticle.MParticle;

public abstract class BaseActivity extends AppCompatActivity {
    private Button mNextScreen;
    private TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        mNextScreen = (Button)findViewById(R.id.button);
        mTitle = (TextView)findViewById(R.id.title);

        mNextScreen.setText(getButtonTitle());
        mNextScreen.setOnClickListener(getButtonListener());
        mTitle.setText(getTextTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            MParticle.getInstance().logout();
            startActivity(new Intent(BaseActivity.this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTextTitle(String title) {
        mTitle.setText(title);
    }

    public abstract String getTextTitle();
    public abstract View.OnClickListener getButtonListener();
    public abstract String getButtonTitle();
}
