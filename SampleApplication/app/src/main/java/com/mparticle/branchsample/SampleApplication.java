package com.mparticle.branchsample;

import android.app.Application;

import com.mparticle.DeepLinkError;
import com.mparticle.DeepLinkListener;
import com.mparticle.DeepLinkResult;
import com.mparticle.MParticle;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MParticle.start(this);
        MParticle instance = MParticle.getInstance();

        instance.setLogLevel(MParticle.LogLevel.DEBUG);
        instance.setDeepLinkListener(new DeepLinkListener() {
            @Override
            public void onResult(DeepLinkResult deepLinkResult) {
                //TODO
                //implement logic for deferred deep linking when user has to newly install application
            }

            @Override
            public void onError(DeepLinkError deepLinkError) {
                //TODO
                // implement error handling logic
            }
        });
    }
}
