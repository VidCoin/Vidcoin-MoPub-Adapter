package com.mopub.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.PersonalInfoManager;
import com.vidcoin.sdkandroid.VidCoin;
import com.vidcoin.sdkandroid.core.VidCoinBase;
import com.vidcoin.sdkandroid.core.interfaces.VidCoinCallBack;

import java.util.HashMap;
import java.util.Map;

import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_ERROR;
import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_SUCCESS;

public class VidcoinRewardedVideo extends CustomEventRewardedVideo {

    private static final String VIDCOIN_AD_NETWORK_CONSTANT = "vidcoin_id";
    private static final String APP_ID = "appId";
    private static final String ZONE_ID = "zoneId";
    private static final String PLACEMENT_CODE = "placementCode";

    private VidcoinRewardedVideoListener vidcoinVideoListener = new VidcoinRewardedVideoListener();
    private VidcoinLifecycleListener vidcoinLifecycleListener = new VidcoinLifecycleListener();
    private String placementCode = null;
    private String zoneId = null;
    private boolean sIsInitialized = false;

    private Activity vidcoinContext = null;

    private boolean isCampaignLoadEnd = false;
    private boolean isCampaignAvailable= false;


    @Nullable
    @Override
    protected CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return vidcoinVideoListener;
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return vidcoinLifecycleListener;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return VIDCOIN_AD_NETWORK_CONSTANT;
    }

    @Override
    protected void onInvalidate() {
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (sIsInitialized) {
            MoPubLog.d("Vidcoin already initialized");
            return false;
        }
        String appId = null;
        if (serverExtras.containsKey(APP_ID)) {
            appId = serverExtras.get(APP_ID);
            if (TextUtils.isEmpty(appId)) {
                MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
            }
        } else {
            MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
        }

        if (appId == null) {
            return false;
        }

        //pass the zoneId from moPub to vidCoin sdk
        fetchZoneIdFromExtra(serverExtras);

        VidCoin.getInstance().setVerboseTag(true);
        PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
        boolean isGDPRApplicable = true;
        if (personalInfoManager != null && personalInfoManager.gdprApplies()) {
            isGDPRApplicable = personalInfoManager.gdprApplies();
        }

        VidCoin.getInstance().init(launcherActivity, appId, MoPub.canCollectPersonalInformation(),
                isGDPRApplicable);

        vidcoinContext = launcherActivity;
        sIsInitialized = true;
        return true;
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
        if (personalInfoManager != null)
            VidCoin.getInstance().setGDPRApplicable(personalInfoManager.gdprApplies());
        if (personalInfoManager != null && personalInfoManager.gdprApplies()) {
            boolean canCollectPersonalInfo = personalInfoManager.canCollectPersonalInformation();
            VidCoin.getInstance().setUserConsent(canCollectPersonalInfo);
            if (!canCollectPersonalInfo) {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(VidcoinRewardedVideo.class, "vidcoin_id", MoPubErrorCode.NETWORK_NO_FILL);
            }

        }
        if (serverExtras.containsKey(PLACEMENT_CODE)) {
            placementCode = serverExtras.get(PLACEMENT_CODE);
            if (TextUtils.isEmpty(placementCode)) placementCode = "";
        }

        // pass the zoneId from moPub to vidCoin sdk
        fetchZoneIdFromExtra(serverExtras);

        Object adUnitObject = localExtras.get("com_mopub_ad_unit_id");
        if (adUnitObject instanceof String) {
            setUpMediationSettings((String) adUnitObject);
        }
        vidcoinContext = activity;

        VidCoin.getInstance().requestAdForPlacement(placementCode, zoneId, vidcoinVideoListener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCampaignLoadEnd) {
                    MoPubLog.d("Vidcoin: Timeout runnable rewarded");
                    MoPubRewardedVideoManager.onRewardedVideoLoadFailure(VidcoinRewardedVideo.class, "vidcoin_id", MoPubErrorCode.EXPIRED);
                    onInvalidate();
                }
            }
        }, 5000);

    }

    @Override
    protected boolean hasVideoAvailable() {
        return isCampaignAvailable;
    }

    @Override
    protected void showVideo() {
        if (VidCoin.getInstance().videoIsAvailableForPlacement(placementCode, zoneId) && vidcoinContext != null) {
            MoPubLog.d("Presenting Vidcoin ad.");
            VidCoin.getInstance().playAdForPlacement(vidcoinContext, placementCode, zoneId, -1);
        } else {
            MoPubLog.d("Failed to present Vidcoin ad.");
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(VidcoinRewardedVideo.class, "vidcoin_id", MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }
    }

    private void fetchZoneIdFromExtra(@NonNull Map<String, String> serverExtras) {
        if (serverExtras.containsKey(ZONE_ID)) {
            zoneId = serverExtras.get(ZONE_ID);
            MoPubLog.d("Fetch zone id " + zoneId);
        }
    }

    private void setUpMediationSettings(String addUnitId) {
        VidcoinMediationSettings globalMediationSettings = MoPubRewardedVideoManager.getGlobalMediationSettings(VidcoinMediationSettings.class);
        VidcoinMediationSettings instanceMediationSettings = MoPubRewardedVideoManager.getInstanceMediationSettings(VidcoinMediationSettings.class, addUnitId);

        if (instanceMediationSettings != null) {
            updateSettings(instanceMediationSettings);
        } else if (globalMediationSettings != null) {
            updateSettings(globalMediationSettings);
        }
    }

    private void updateSettings(VidcoinMediationSettings mediationSettings) {
        if (mediationSettings != null) {
            HashMap<VidCoinBase.VCUserInfos, String> vidcoinData = new HashMap<>();

            if (!TextUtils.isEmpty(mediationSettings.userBirthYear)) {
                vidcoinData.put(VidCoinBase.VCUserInfos.VC_USER_BIRTH_YEAR, mediationSettings.userBirthYear);
            }
            if (!TextUtils.isEmpty(mediationSettings.userAppId)) {
                vidcoinData.put(VidCoinBase.VCUserInfos.VC_USER_APP_ID, mediationSettings.userAppId);
            }
            if (mediationSettings.userGender != null && !TextUtils.isEmpty(mediationSettings.userGender.toString())) {
                vidcoinData.put(VidCoinBase.VCUserInfos.VC_USER_GENDER, mediationSettings.userGender.toString());
            }

            if (!vidcoinData.isEmpty()) {
                VidCoin.getInstance().setUserInfos(vidcoinData);
            }
        }
    }

    /**
     * CustomEventRewardedVideoListener implementation
     */
    private class VidcoinRewardedVideoListener implements CustomEventRewardedVideoListener, VidCoinCallBack {

        @Override
        public void vidCoinCampaignLoadEnd(String placementCode, boolean campaignAvailable) {
            isCampaignLoadEnd = true;
            isCampaignAvailable = campaignAvailable;
            if (campaignAvailable) {
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT);
            } else {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubErrorCode.NETWORK_NO_FILL);
            }
        }

        @Override
        public void vidCoinViewWillAppear() {
            MoPubRewardedVideoManager.onRewardedVideoStarted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT);
        }

        @Override
        public void vidCoinViewDidDisappearWithViewInformation(HashMap<VidCoinBase.VCData, String> hashMap) {
            vidCoinDidValidateView(hashMap, true);
            MoPubRewardedVideoManager.onRewardedVideoClosed(VidcoinRewardedVideo.class, "vidcoin_id");
        }

        @Override
        public void vidCoinDidValidateView(HashMap<VidCoinBase.VCData, String> hashMap) {
            vidCoinDidValidateView(hashMap, false);
        }

        private void vidCoinDidValidateView(HashMap<VidCoinBase.VCData, String> hashMap, boolean display) {
            if (!display) return;

            String statusCode = hashMap.get(VidCoin.VCData.VC_DATA_STATUS_CODE);
            if (VC_STATUS_CODE_SUCCESS.toString().equalsIgnoreCase(statusCode)) {
                int reward;
                try {
                    String value = hashMap.get(VidCoin.VCData.VC_DATA_REWARD);
                    reward = value != null ? Integer.parseInt(value) : 0;
                } catch (Exception e) {
                    reward = 0;
                }
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.success("", reward));
            } else if (VC_STATUS_CODE_ERROR.toString().equalsIgnoreCase(statusCode)) {
                MoPubLog.e("An error occurred during view validation.");
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.failure());
            } else {
                MoPubLog.e("An unknown occurred during view validation.");
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.failure());
            }
        }
    }

}
