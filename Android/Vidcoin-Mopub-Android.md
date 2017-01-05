# Vidcoin Mediation Adapter for MoPub SDK for Android
![Vidcoin](https://d3rud9259azp35.cloudfront.net/documentation/Vidcoin-Logo.png)

Adapter Version: 1.0.0    
Manager: https://manager.vidcoin.com    
Contact: publishers@vidcoin.com    

## Requirements
- Android SDK v2.3.3 (API level 10) or higher
- Mopub SDK v4.11.0 or higher
- Vidcoin Android SDK v1.3.1 or higher

## Instructions
- Add the MoPub SDK to your project. Please refer to the  [getting started guide](https://www.mopub.com/resources/docs/android-sdk-integration/android-getting-started/) for detailed instructions.
- Add the Vidcoin Java class (`VidcoinRewardVideo.java`) into your project according to the MoPub documentation.
- Import the Vidcoin Android SDK in your Android project. The  [documentation](https://github.com/VidCoin/VidCoin-Android-SDK/blob/master/Documentation.md) contains detailed instructions on how to do so.
- Add a new ad network in the MoPub dashboard (in  _Networks -> Add a Network_, choose _Custom Native Network_), and use the following configuration for each AdUnit:
    - The `Custom Event Class` value should be set to `com.mopub.mobileads.VidcoinRewardedVideo`
    - The `Custom Event Class Data` value must be formatted as follows:
```json
{
  "appId": "APP_ID",
  "placementCode":"PLACEMENT_CODE"
}
```
where "appId" and "placementCode" are found on  [Vidcoin's Manager](https://manager.vidcoin.com), in your app's details.

## Using VidcoinMediationSettings
- The `VidcoinMediationSettings` class can be used to create a `MediationSettings` with optional information that can be passed to the adapter. The example below shows how to use the bundle builder class. The `VidcoinMediationSettings` class provides `userAppId`, `userBirthYear` and `userGender` parameters, that can be used to give information about the user.
```java
VidcoinRewardedVideo.VidcoinMediationSettings vidcoinMediationSettings = new VidcoinRewardedVideo.VidcoinMediationSettings.Builder()
        .withUserAppId("USER_APP_ID")
        .withUserBirthYear("1990")
        .withUserGender(VC_USER_GENDER_MALE)
        .build();
MoPub.initializeRewardedVideo(getActivity(), vidcoinMediationSettings);
```

## Notes
- The `onRewardedVideoPlaybackError` event and `onRewardedVideoClicked` event for Android are not supported for ads served with Vidcoin.
- If you prefer using a jar file, you can extract the classes.jar file from the .aar using a standard zip extract tool.

See the  [getting started guide](https://www.mopub.com/resources/docs/android-sdk-integration/android-getting-started/) for the latest documentation and code samples for the MoPub SDK.
