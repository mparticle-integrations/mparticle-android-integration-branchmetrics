package com.mparticle.branchsample.Activities;

import android.content.Intent;
import android.view.View;

public class SecondActivity extends BaseActivity {
    @Override
    public String getTextTitle() {
        return "Second Page";
    }

    @Override
    public View.OnClickListener getButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, ThirdActivity.class));
            }
        };
    }

    @Override
    public String getButtonTitle() {
        return "Next Activity";
    }
}
