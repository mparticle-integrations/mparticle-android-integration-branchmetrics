package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;

import com.mparticle.AttributionError;
import com.mparticle.AttributionResult;
import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.internal.KitManager;
import com.mparticle.internal.Logger;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.InstallListener;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;

/**
 * <p/>
 * Embedded implementation of the Branch Metrics SDK
 * <p/>
 */
public class BranchMetricsKit extends KitIntegration implements
        KitIntegration.EventListener,
        KitIntegration.CommerceListener,
        KitIntegration.AttributeListener,
        KitIntegration.ApplicationStateListener,
        Branch.BranchReferralInitListener {
    
    private String BRANCH_APP_KEY = "branchKey";
    private static final String FORWARD_SCREEN_VIEWS = "forwardScreenViews";
    private boolean mSendScreenEvents;
    private BranchUtil branchUtil;
    
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
        branchUtil = new BranchUtil();
        Branch.disableDeviceIDFetch(MParticle.isAndroidIdDisabled());
        Branch.getAutoInstance(getContext().getApplicationContext(), getSettings().get(BRANCH_APP_KEY)).initSession(this);
        if (Logger.getMinLogLevel() != MParticle.LogLevel.NONE) {
            Branch.enableLogging();
        }
        String sendScreenEvents = settings.get(FORWARD_SCREEN_VIEWS);
        mSendScreenEvents = sendScreenEvents != null && sendScreenEvents.equalsIgnoreCase("true");
        return null;
    }
    
    @Override
    public List<ReportingMessage> setOptOut(boolean b) {
        getBranch().disableTracking(b);
        List<ReportingMessage> messages = new LinkedList<>();
        messages.add(ReportingMessage.fromEvent(this, new MPEvent.Builder("setOptOut " + b, MParticle.EventType.Other).build()));
        return messages;
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
        branchUtil.createBranchEventFromMPEvent(event).logEvent(getContext());
        List<ReportingMessage> messages = new LinkedList<>();
        messages.add(ReportingMessage.fromEvent(this, event));
        return messages;
    }
    
    @Override
    public List<ReportingMessage> logLtvIncrease(BigDecimal bigDecimal, BigDecimal bigDecimal1, String s, Map<String, String> map) {
        return null;
    }
    
    @Override
    public List<ReportingMessage> logEvent(CommerceEvent commerceEvent) {
        branchUtil.createBranchEventFromMPCommerceEvent(commerceEvent).logEvent(getContext());
        List<ReportingMessage> messages = new LinkedList<>();
        messages.add(ReportingMessage.fromEvent(this, commerceEvent));
        return messages;
    }
    
    
    @Override
    public List<ReportingMessage> logScreen(String screenName, Map<String, String> eventAttributes) {
        if (mSendScreenEvents) {
            BranchEvent logScreenEvent = new BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEM);
            branchUtil.updateBranchEventWithCustomData(logScreenEvent, eventAttributes);
            logScreenEvent.logEvent(getContext());
            
            List<ReportingMessage> messages = new LinkedList<>();
            messages.add(ReportingMessage.fromEvent(this, new MPEvent.Builder("Viewed " + screenName, MParticle.EventType.Other).build()));
            return messages;
        } else {
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
        // Logout the current user from Branch when Identity is removed.
        if (identityType == MParticle.IdentityType.CustomerId) {
            getBranch().logout();
        }
    }
    
    @Override
    public List<ReportingMessage> logout() {
        getBranch().logout();
        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(ReportingMessage.logoutMessage(this));
        return messageList;
    }
    
    /**
     * Don't do anything here - we make the call to get the latest deep link info during onResume, below.
     */
    @Override
    public void onInitFinished(JSONObject jsonResult, BranchError branchError) {
        if (jsonResult != null && jsonResult.length() > 0) {
            AttributionResult result = new AttributionResult()
                    .setParameters(jsonResult)
                    .setServiceProviderId(this.getConfiguration().getKitId());
            getKitManager().onResult(result);
        }
        if (branchError != null) {
            AttributionError error = new AttributionError()
                    .setMessage(branchError.toString())
                    .setServiceProviderId(this.getConfiguration().getKitId());
            getKitManager().onError(error);
        }
    }
    
    @Override
    public void onApplicationForeground() {
        Branch.getAutoInstance(getContext().getApplicationContext(), getSettings().get(BRANCH_APP_KEY)).initSession(this);
    }
    
    @Override
    public void onApplicationBackground() {
    
    }
    
    
    // Region Branch event translation methods
    
    
    // end Region
}