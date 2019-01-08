package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubLifecycleManager;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.PersonalInfoManager;
import com.vidcoin.sdkandroid.VidCoin;
import com.vidcoin.sdkandroid.core.VidCoinBase;
import com.vidcoin.sdkandroid.core.interfaces.VidCoinCallBack;

import java.util.HashMap;
import java.util.Map;

import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_CANCEL;
import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_ERROR;
import static com.vidcoin.sdkandroid.core.VidCoinBase.VCStatusCode.VC_STATUS_CODE_SUCCESS;

public class VidcoinInterstitialVideo extends CustomEventInterstitial {

    private static final String APP_ID = "appId";
    private static final String PLACEMENT_CODE = "placementCode";
    private static final String ZONE_ID = "zoneId";

    private boolean isInitialized = false;
    private String placementCode = null;
    private String zoneId = null;

    private VidcoinInterstitialVideoListener vidcoinVideoListener = new VidcoinInterstitialVideoListener();
    private CustomEventInterstitialListener customEventInterstitialListener;

    private Context vidcoinContext = null;
    private boolean isCampaignLoadEnd = false;


    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        this.customEventInterstitialListener = customEventInterstitialListener;

        if (!(context instanceof Activity)) {
            MoPubLog.e("loadInterstitial must be called on an Activity context");
            this.customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
            return;
        }

        if (!isInitialized) {
            String appId = null;
            if (serverExtras.containsKey(APP_ID)) {
                appId = serverExtras.get(APP_ID);
                if (TextUtils.isEmpty(appId)) {
                    MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
                    this.customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                }
            } else {
                MoPubLog.e("Vidcoin failed due to empty " + APP_ID);
                this.customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            if (appId == null) {
                return;
            }

            VidCoin.getInstance().setVerboseTag(true);
            PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            boolean isGDPRApplicable = true;
            if (personalInfoManager != null && personalInfoManager.gdprApplies()) {
                isGDPRApplicable = personalInfoManager.gdprApplies();
            }

            VidCoin.getInstance().init(context, appId, MoPub.canCollectPersonalInformation(), isGDPRApplicable);
            isInitialized = true;
            MoPubLifecycleManager.getInstance((Activity) context).addLifecycleListener(new VidcoinLifecycleListener());
        } else {
            PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            if (personalInfoManager != null)
                VidCoin.getInstance().setGDPRApplicable(personalInfoManager.gdprApplies());
            if (personalInfoManager != null && personalInfoManager.gdprApplies()) {
                boolean canCollectPersonalInfo = personalInfoManager.canCollectPersonalInformation();
                VidCoin.getInstance().setUserConsent(canCollectPersonalInfo);
            }
        }

        if (serverExtras.containsKey(PLACEMENT_CODE)) {
            placementCode = serverExtras.get(PLACEMENT_CODE);
            if (TextUtils.isEmpty(placementCode)) placementCode = "";
        }

        //pass the zoneId from moPub to vidCoin sdk before init
        if (serverExtras.containsKey(ZONE_ID)) {
            zoneId = serverExtras.get(ZONE_ID);
        }

        VidCoin.getInstance().requestAdForPlacement(placementCode, zoneId, vidcoinVideoListener);
        vidcoinContext = context;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // vidCoinCampaignsUpdate should have been called in the mean time
                if (!isCampaignLoadEnd) {
                    MoPubLog.d("Vidcoin: Timeout runnable interstitial");
                    VidcoinInterstitialVideo.this.customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.EXPIRED);
                }
            }
        }, 5000);

    }


    @Override
    protected void showInterstitial() {
        if (VidCoin.getInstance().videoIsAvailableForPlacement(placementCode, zoneId) && vidcoinContext != null) {
            MoPubLog.d("Presenting Vidcoin ad.");
            VidCoin.getInstance().playAdForPlacement(vidcoinContext, placementCode, zoneId, -1);
        } else {
            MoPubLog.d("Failed to present Vidcoin ad.");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }
    }

    @Override
    protected void onInvalidate() {

    }

    public class VidcoinInterstitialVideoListener implements VidCoinCallBack {


        @Override
        public void vidCoinCampaignLoadEnd(String placementCode, boolean campaignAvailable) {
            isCampaignLoadEnd = true;
            if (campaignAvailable) {
                customEventInterstitialListener.onInterstitialLoaded();
            } else {
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }
        }

        @Override
        public void vidCoinViewWillAppear() {
            customEventInterstitialListener.onInterstitialShown();
        }

        @Override
        public void vidCoinViewDidDisappearWithViewInformation(HashMap<VidCoinBase.VCData, String> hashMap) {
            vidCoinDidValidateView(hashMap, true);
        }

        @Override
        public void vidCoinDidValidateView(HashMap<VidCoinBase.VCData, String> hashMap) {
            vidCoinDidValidateView(hashMap, false);
        }

        private void vidCoinDidValidateView(HashMap<VidCoinBase.VCData, String> hashMap, boolean display) {
            if (!display) return;

            String statusCode = hashMap.get(VidCoin.VCData.VC_DATA_STATUS_CODE);
            if (VC_STATUS_CODE_SUCCESS.toString().equalsIgnoreCase(statusCode)) {
                customEventInterstitialListener.onInterstitialDismissed();
            } else if (VC_STATUS_CODE_ERROR.toString().equalsIgnoreCase(statusCode)) {
                MoPubLog.e("An error occurred during view validation.");
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
            } else if (VC_STATUS_CODE_CANCEL.toString().equalsIgnoreCase(statusCode)) {
                customEventInterstitialListener.onInterstitialDismissed();
            } else {
                MoPubLog.e("An unknown occurred during view validation.");
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
            }
        }
    }
}
