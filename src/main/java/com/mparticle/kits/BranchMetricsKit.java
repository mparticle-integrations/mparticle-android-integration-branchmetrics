package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.mparticle.AttributionError;
import com.mparticle.AttributionResult;
import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.identity.MParticleUser;
import com.mparticle.internal.Logger;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.PrefHelper;
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
        KitIntegration.IdentityListener,
        Branch.BranchReferralInitListener {

    private String BRANCH_APP_KEY = "branchKey";
    private static final String FORWARD_SCREEN_VIEWS = "forwardScreenViews";
    private static final String USER_IDENTIFICATION_TYPE = "userIdentificationType";
    private boolean isMpidIdentityType = false;
    MParticle.IdentityType identityType = MParticle.IdentityType.CustomerId;
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
        Branch.registerPlugin("mParticle", getClass().getPackage() != null ? getClass().getPackage().getSpecificationVersion() : "0" );
        Branch.getAutoInstance(getContext().getApplicationContext(), getSettings().get(BRANCH_APP_KEY)).sessionBuilder(null).withCallback(this).init();
        if (Logger.getMinLogLevel() != MParticle.LogLevel.NONE) {
            Branch.enableLogging();
        }
        String sendScreenEvents = settings.get(FORWARD_SCREEN_VIEWS);
        mSendScreenEvents = sendScreenEvents != null && sendScreenEvents.equalsIgnoreCase("true");
        setIdentityType(settings);
        return null;
    }

    void setIdentityType(Map<String, String> settings) {
        String userIdentificationType = settings.get(USER_IDENTIFICATION_TYPE);
        if (!KitUtils.isEmpty(userIdentificationType)) {
            if (userIdentificationType.equals("MPID")) {
                isMpidIdentityType = true;
            } else if(userIdentificationType.equals("Email")) {
                identityType = null;
            } else {
                identityType = MParticle.IdentityType.valueOf(userIdentificationType);
            }
        }
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean b) {
        getBranch().disableTracking(b);
        List<ReportingMessage> messages = new LinkedList<>();
        messages.add(new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null));
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
            messages.add(new ReportingMessage(this, ReportingMessage.MessageType.SCREEN_VIEW, System.currentTimeMillis(), eventAttributes));
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
        PrefHelper.LogAlways("setInstallReferrer(intent) was ignored, INSTALL_REFERRER broadcast intent is deprecated, relevant data is now collected automatically using the Play Install Referrer Library bundled together with Branch SDK.");
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
        getBranch().logout();
        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(ReportingMessage.logoutMessage(this));
        return messageList;
    }

    @Override
    public void onIdentifyCompleted(MParticleUser mParticleUser, FilteredIdentityApiRequest filteredIdentityApiRequest) {
        updateUser(mParticleUser);
    }

    @Override
    public void onLoginCompleted(MParticleUser mParticleUser, FilteredIdentityApiRequest filteredIdentityApiRequest) {
        updateUser(mParticleUser);
    }

    @Override
    public void onLogoutCompleted(MParticleUser mParticleUser, FilteredIdentityApiRequest filteredIdentityApiRequest) {
        updateUser(mParticleUser);
    }

    @Override
    public void onModifyCompleted(MParticleUser mParticleUser, FilteredIdentityApiRequest filteredIdentityApiRequest) {
        updateUser(mParticleUser);
    }

    @Override
    public void onUserIdentified(MParticleUser mParticleUser) {

    }

    private void updateUser(@NonNull MParticleUser mParticleUser) {
        String identity = null;
        if (isMpidIdentityType) {
            identity = String.valueOf(mParticleUser.getId());
        } else if (identityType != null ) {
            String mPIdentity = mParticleUser.getUserIdentities().get(identityType);
            if (mPIdentity != null) {
                identity = mPIdentity;
            }
        }
        getBranch().setIdentity(identity);
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
            if (branchError.getErrorCode() != BranchError.ERR_BRANCH_ALREADY_INITIALIZED) {
                AttributionError error = new AttributionError()
                        .setMessage(branchError.toString())
                        .setServiceProviderId(this.getConfiguration().getKitId());
                getKitManager().onError(error);
            }
        }
    }

    @Override
    public void onApplicationForeground() {
        Branch.getAutoInstance(getContext().getApplicationContext(), getSettings().get(BRANCH_APP_KEY)).sessionBuilder(null).withCallback(this).init();
    }

    @Override
    public void onApplicationBackground() {

    }
}
