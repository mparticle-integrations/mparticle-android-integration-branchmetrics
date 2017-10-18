package com.mparticle.kits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mparticle.DeepLinkError;
import com.mparticle.DeepLinkResult;
import com.mparticle.MPEvent;
import com.mparticle.MParticle;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.InstallListener;

/**
 * <p/>
 * Embedded implementation of the Branch Metrics SDK
 * <p/>
 */
public class BranchMetricsKit extends KitIntegration implements KitIntegration.EventListener, Branch.BranchReferralInitListener, KitIntegration.AttributeListener, KitIntegration.ActivityListener {

    private String BRANCH_APP_KEY = "branchKey";
    private final String FORWARD_SCREEN_VIEWS = "forwardScreenViews";
    private boolean mSendScreenEvents;
    private boolean backgrounded = true;

    @Override
    public Object getInstance() {
        return getBranch();
    }

    @Override
    public String getName() {
        return "Branch Metrics";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        Branch.disableDeviceIDFetch(MParticle.isAndroidIdDisabled());
        Branch.getAutoInstance(getContext().getApplicationContext(), getSettings().get(BRANCH_APP_KEY)).initSession(this);
        String sendScreenEvents = settings.get(FORWARD_SCREEN_VIEWS);
        mSendScreenEvents = sendScreenEvents != null && sendScreenEvents.equalsIgnoreCase("true");
        return null;
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean b) {
        return null;
    }

    @Override
    public List<ReportingMessage> leaveBreadcrumb(String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logError(String s, Map<String, String> map) {
        return null;
    }

    @Override
    public List<ReportingMessage> logException(Exception e, Map<String, String> map, String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logEvent(MPEvent event) {
        Map<String, String> attributes = event.getInfo();
        JSONObject jsonAttributes = null;
        if (attributes != null && attributes.size() > 0) {
            jsonAttributes = new JSONObject();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                try {
                    jsonAttributes.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {

                }
            }
        }
        getBranch().userCompletedAction(event.getEventName(), jsonAttributes);
        List<ReportingMessage> messages = new LinkedList<ReportingMessage>();
        messages.add(ReportingMessage.fromEvent(this, event));
        return messages;
    }

    @Override
    public List<ReportingMessage> logScreen(String screenName, Map<String, String> eventAttributes) {
        if (mSendScreenEvents){
            MPEvent event = new MPEvent.Builder("Viewed " + screenName, MParticle.EventType.Other)
                    .info(eventAttributes)
                    .build();
            return logEvent(event);
        }else {
            return null;
        }
    }

    private Branch getBranch() {
        return Branch.getInstance(getContext(), getSettings().get(BRANCH_APP_KEY));
    }

    @Override
    public void setInstallReferrer(Intent intent) {
        new InstallListener().onReceive(getContext(), intent);
    }

    @Override
    public void setUserAttribute(String s, String s1) {

    }

    @Override
    public void setUserAttributeList(String s, List<String> list) {

    }

    @Override
    public boolean supportsAttributeLists() {
        return true;
    }

    @Override
    public void setAllUserAttributes(Map<String, String> map, Map<String, List<String>> map1) {

    }

    @Override
    public void removeUserAttribute(String s) {

    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String s) {
        if (identityType == MParticle.IdentityType.CustomerId) {
            getBranch().setIdentity(s);
        }
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {

    }

    @Override
    public List<ReportingMessage> logout() {
        getBranch().logout();
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(ReportingMessage.logoutMessage(this));
        return messageList;
    }

    /**
     * Don't do anything here - we make the call to get the latest deep link info during onResume, below.
     */
    @Override
    public void checkForDeepLink() { }

    @Override
    public void onInitFinished(JSONObject jsonResult, BranchError branchError) {
        if (jsonResult != null && jsonResult.length() > 0) {
            DeepLinkResult result = new DeepLinkResult()
                    .setParameters(jsonResult)
                    .setServiceProviderId(this.getConfiguration().getKitId());
            getKitManager().onResult(result);
        }
        if (branchError != null) {
            DeepLinkError error = new DeepLinkError()
                    .setMessage(branchError.toString())
                    .setServiceProviderId(this.getConfiguration().getKitId());
            getKitManager().onError(error);
        }
    }

    @Override
    public List<ReportingMessage> onActivityCreated(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStarted(Activity activity) {
        backgrounded = MParticle.getInstance().getAppStateManager().isBackgrounded();
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityResumed(Activity activity) {
        if (backgrounded) {
            Branch.getInstance(this.getContext()).initSession(this, activity);
        }
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityPaused(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStopped(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityDestroyed(Activity activity) {
        return null;
    }


}