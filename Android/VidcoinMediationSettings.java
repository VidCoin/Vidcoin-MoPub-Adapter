package com.mopub.mobileads;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.MediationSettings;
import com.vidcoin.sdkandroid.core.VidCoinBase;

/**
 * VidcoinMediationSettings implementation
 */
final class VidcoinMediationSettings implements MediationSettings {

    @Nullable
    final VidCoinBase.VCUserGender userGender;

    @Nullable
    final String userAppId;

    @Nullable
    final String userBirthYear;

    static class Builder {
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
