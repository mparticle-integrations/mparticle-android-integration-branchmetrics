package com.mparticle.branchsample.Activities;

import android.content.Intent;
import android.view.View;

public class FourthActivity extends BaseActivity {
    @Override
    public String getTextTitle() {
        return "Fourth Activity";
    }

    @Override
    public View.OnClickListener getButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FourthActivity.this, ThirdActivity.class));
            }
        };
    }

    @Override
    public String getButtonTitle() {
        return "End of the road";
    }
}
