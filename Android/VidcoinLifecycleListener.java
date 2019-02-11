package com.mopub.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.mopub.common.BaseLifecycleListener;
import com.vidcoin.sdkandroid.VidCoin;

/**
 * VidcoinLifecycleListener implementation
 */
final class VidcoinLifecycleListener extends BaseLifecycleListener {
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
