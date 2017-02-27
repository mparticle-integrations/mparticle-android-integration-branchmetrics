package com.mparticle.branchsample.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mparticle.DeepLinkError;
import com.mparticle.DeepLinkListener;
import com.mparticle.DeepLinkResult;
import com.mparticle.MParticle;

public class ThirdActivity extends BaseActivity {
    private int i = 1;
    private final String mTitleString = "Third Activity";

    @Override
    public String getTextTitle() {
        return getTitleText();
    }

    @Override
    public View.OnClickListener getButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThirdActivity.this, FourthActivity.class));
            }
        };
    }

    @Override
    public String getButtonTitle() {
        return "Next Activity";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        setTextTitle(getTitleText());
    }

    private String getTitleText() {
        return "Third Activity \nIntent# " + i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MParticle.getInstance().checkForDeepLink(new DeepLinkListener() {
            @Override
            public void onResult(DeepLinkResult deepLinkResult) {
                //TODO
                // implement logic for deeplinking to this Activity when user already has app installed
            }

            @Override
            public void onError(DeepLinkError deepLinkError) {
                //TODO
                // implement error handling
            }
        });
    }
}