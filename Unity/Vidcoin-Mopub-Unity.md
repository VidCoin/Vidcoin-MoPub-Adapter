# Vidcoin Mediation Adapter for MoPub SDK for Unity
![Vidcoin](https://d3rud9259azp35.cloudfront.net/documentation/Vidcoin-Logo.png)

Adapter Version: 1.0.0    
Manager: https://manager.vidcoin.com    
Contact: publishers@vidcoin.com    

## Requirements
- Unity 5.0 or higher
- iOS
	- Xcode 7.0 or higher
	- Mopub SDK v4.11.0 or higher
- Android
	- Android SDK v2.3.3 (API level 10) or higher
	- Mopub SDK v4.11.0 or higher

## Includes
This UnityPackage includes:
- Vidcoin iOS SDK (& post-build scripts) + Adapter
- Vidcoin Android SDK (& dependencies) + Adapter

## Instructions
- Add the MoPub SDK. See the  [getting started guide](http://www.mopub.com/resources/docs/unity-engine-integration/#GettingStarted) for detailed instructions on how to integrate the MoPub SDK.
- Drag & Drop the UnityPackage into your project.
- Add a new ad network in the MoPub dashboard (in  _Networks -> Add a Network_, choose _Custom Native Network_), and define AdUnit information according Vidcoin MoPub iOS & Android documentations.

## Notes
- The `rewardedVideoDidFailToLoadAdForCustomEvent`, `rewardedVideoDidExpireForCustomEvent`, `rewardedVideoWillLeaveApplicationForCustomEvent` and `rewardedVideoDidReceiveTapEventForCustomEvent` events for iOS are not supported for ads served with Vidcoin.
- The `onRewardedVideoPlaybackError` and `onRewardedVideoClicked` events for Android are not supported for ads served with Vidcoin.

The latest documentation and code samples for the Google Mobile Ads SDK are available  [here](http://www.mopub.com/resources/docs/unity-engine-integration/#GettingStarted).
