package com.mopub.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.BaseLifecycleListener;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.vidcoin.sdkandroid.VidCoin;
import com.vidcoin.sdkandroid.core.VidCoinBase;
import com.vidcoin.sdkandroid.core.interfaces.VidCoinCallBack;

import java.util.HashMap;
import java.util.Map;

import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_CANCEL;
import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_ERROR;
import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_SUCCESS;

public class VidcoinRewardedVideo extends CustomEventRewardedVideo {

    private static final String VIDCOIN_AD_NETWORK_CONSTANT = "vidcoin_id";
    private static final String APP_ID = "appId";
    private static final String PLACEMENT_CODE = "placementCode";

    private static final String KEY_USER_APP_ID = "VC_USER_APP_ID";
    private static final String KEY_USER_BIRTH_YEAR = "VC_USER_BIRTH_YEAR";
    private static final String KEY_USER_GENDER = "VC_USER_GENDER";

    private static VidcoinRewardedVideoListener sVidcoinVideoListener = new VidcoinRewardedVideoListener();
    private static VidcoinLifecycleListener sVidcoinLifecycleListener = new VidcoinLifecycleListener();
    private static String placementCode = null;
    private static boolean sIsInitialized = false;

    private Activity vidcoinContext = null;

    @Nullable
    @Override
    protected CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return sVidcoinVideoListener;
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return sVidcoinLifecycleListener;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return VIDCOIN_AD_NETWORK_CONSTANT;
    }

    @Override
    protected void onInvalidate() { }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (sIsInitialized) {
            MoPubLog.d("Vidcoin already initialized");
            return false;
        }

        String appId;
        if (serverExtras.containsKey(APP_ID)) {
            appId = serverExtras.get(APP_ID);
            if (TextUtils.isEmpty(appId)) {
                MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
                return false;
            }
        } else {
            MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
            return false;
        }

        VidCoin.getInstance().setVerboseTag(true);
        VidCoin.getInstance().init(launcherActivity, appId, sVidcoinVideoListener);

        vidcoinContext = launcherActivity;
        sIsInitialized = true;
        return true;
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (serverExtras.containsKey(PLACEMENT_CODE)) {
            placementCode = serverExtras.get(PLACEMENT_CODE);
            if (TextUtils.isEmpty(placementCode)) placementCode = "";
        }

        Object adUnitObject = localExtras.get("com_mopub_ad_unit_id");
        if ((adUnitObject != null) && ((adUnitObject instanceof String))) {
            setUpMediationSettings(((String)adUnitObject));
        }
        vidcoinContext = activity;
        loadAvailableVideos();
    }

    @Override
    protected boolean hasVideoAvailable() {
        return VidCoin.getInstance().videoIsAvailableForPlacement(placementCode);
    }

    @Override
    protected void showVideo() {
        if (hasVideoAvailable() && vidcoinContext != null) {
            MoPubLog.d("Presenting Vidcoin ad.");
            VidCoin.getInstance().playAdForPlacement(vidcoinContext, placementCode, -1);
        } else {
            MoPubLog.d("Failed to present Vidcoin ad.");
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

    private static void loadAvailableVideos() {
        if (VidCoin.getInstance().videoIsAvailableForPlacement(placementCode)){
            MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT);
        } else {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    /**
     * CustomEventRewardedVideoListener implementation
     */
    private static class VidcoinRewardedVideoListener implements CustomEventRewardedVideoListener, VidCoinCallBack {

        @Override
        public void vidCoinCampaignsUpdate() {
            loadAvailableVideos();
        }

        @Override
        public void vidCoinViewWillAppear() {
            MoPubRewardedVideoManager.onRewardedVideoStarted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT);
        }

        @Override
        public void vidCoinViewDidDisappearWithViewInformation(HashMap<VidCoinBase.VCData, String> hashMap) {
            //
        }

        @Override
        public void vidCoinDidValidateView(HashMap<VidCoinBase.VCData, String> hashMap) {
            String statusCode = hashMap.get(VidCoin.VCData.VC_DATA_STATUS_CODE);
            if (statusCode.equalsIgnoreCase(VC_STATUS_CODE_SUCCESS.toString())) {
                int reward = Integer.parseInt(hashMap.get(VidCoin.VCData.VC_DATA_REWARD));
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.success("", reward));
            } else if (statusCode.equalsIgnoreCase(VC_STATUS_CODE_ERROR.toString())) {
                MoPubLog.e( "An error occurred during view validation.");
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.failure());
            } else if (statusCode.equalsIgnoreCase(VC_STATUS_CODE_CANCEL.toString())) {
                MoPubRewardedVideoManager.onRewardedVideoClosed(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT);
            } else {
                MoPubLog.e("An unknown occurred during view validation.");
                MoPubRewardedVideoManager.onRewardedVideoCompleted(VidcoinRewardedVideo.class, VIDCOIN_AD_NETWORK_CONSTANT, MoPubReward.failure());
            }
        }
    }

    /**
     * VidcoinLifecycleListener implementation
     */
    private static final class VidcoinLifecycleListener extends BaseLifecycleListener {
        @Override
        public void onStart(@NonNull Activity activity) {
            super.onStart(activity);
            VidCoin.getInstance().onStart();
        }

        @Override
        public void onStop(@NonNull Activity activity) {
            super.onStop(activity);
            VidCoin.getInstance().onStop();
        }
    }


    /**
     * VidcoinMediationSettings implementation
     */
    public static final class VidcoinMediationSettings implements MediationSettings {
        
        @Nullable
        private final VidCoinBase.VCUserGender userGender;

        @Nullable
        private final String userAppId;

        @Nullable
        private final String userBirthYear;

        public static class Builder {
            @Nullable
            private VidCoinBase.VCUserGender userGender;

            @Nullable
            private String userAppId;

            @Nullable
            private String userBirthYear;

            public Builder withUserGender(@NonNull VidCoinBase.VCUserGender gender) {
                this.userGender = gender;
                return this;
            }

            public Builder withUserAppId(@NonNull String appId) {
                this.userAppId = appId;
                return this;
            }

            public Builder withUserBirthYear(@NonNull String birthYear) {
                this.userBirthYear = birthYear;
                return this;
            }

            public VidcoinMediationSettings build() {
                return new VidcoinMediationSettings(this);
            }
        }

        private VidcoinMediationSettings(@NonNull Builder builder) {
            this.userGender = builder.userGender;
            this.userAppId = builder.userAppId;
            this.userBirthYear = builder.userBirthYear;
        }
    }
}
