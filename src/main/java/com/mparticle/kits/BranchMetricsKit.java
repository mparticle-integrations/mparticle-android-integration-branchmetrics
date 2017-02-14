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
public class BranchMetricsKit extends KitIntegration implements KitIntegration.ActivityListener, KitIntegration.EventListener, Branch.BranchReferralInitListener, KitIntegration.AttributeListener {

    private String BRANCH_APP_KEY = "branchKey";
    private final String FORWARD_SCREEN_VIEWS = "forwardScreenViews";
    private boolean mSendScreenEvents;

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
        getBranch().initSession();
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
    public List<ReportingMessage> onActivityStopped(Activity activity) {
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(
                new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null)
        );
        return messageList;
    }

    @Override
    public List<ReportingMessage> onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityDestroyed(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityCreated(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStarted(Activity activity) {
        getBranch().initSession(activity);
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(
                new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null)
        );
        return messageList;
    }

    @Override
    public List<ReportingMessage> onActivityResumed(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityPaused(Activity activity) {
        return null;
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
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {

    }

    @Override
    public List<ReportingMessage> logout() {
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(ReportingMessage.logoutMessage(this));
        return messageList;
    }

    @Override
    public void checkForDeepLink() {
        Branch.getAutoInstance(getContext()).initSession(this);
    }

    @Override
    public void onInitFinished(JSONObject jsonResult, BranchError branchError) {
        if (jsonResult != null) {
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
}