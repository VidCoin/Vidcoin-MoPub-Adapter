# Vidcoin Mediation Adapter for MoPub SDK for iOS
![Vidcoin](https://d3rud9259azp35.cloudfront.net/documentation/Vidcoin-Logo.png)

Adapter Version: 1.0.0    
Manager: https://manager.vidcoin.com    
Contact: publishers@vidcoin.com    

## Requirements
- Xcode 7.0 or higher
- Deployment target of iOS 7.0 or higher
- Mopub SDK v4.11.0 or higher
- Vidcoin Android SDK v1.3.1 or higher

## Instructions
- Add the MoPub SDK to your project. Please refer to the  [getting started guide](http://www.mopub.com/resources/docs/ios-sdk-integration/ios-getting-started/) for detailed instructions.
- Add Vidcoin Adapter files (.h and .m) in zip to your Xcode project.
- Download the latest release of the Vidcoin SDK on  [Github](https://github.com/VidCoin/VidCoin-iOS-SDK).
- Add a new ad network in the MoPub dashboard (in  _Networks -> Add a Network_, choose _Custom Native Network_), and use the following configuration for each AdUnit:
    - The `Custom Event Class` value should be set to `VidcoinRewardedVideoCustomEvent`
    - The `Custom Event Class Data` value must be formatted as follows:
```json
{
  "appId": "APP_ID",
  "placementCode": "PLACEMENT_CODE"
}
```
where "appId" and "placementCode" are found on Vidcoin's Manager, in your app's details.

## Using VidcoinInstanceMediationSettings
- The Vidcoin Adapter's classess has a class called `VidcoinInstanceMediationSettings` to provide the `userAppId`, `userBirthYear` and `userGender` parameters.
  Here is an example of its usage:
```objc
VidcoinInstanceMediationSettings *vidcoinSettings = [VidcoinInstanceMediationSettings new];
vidcoinSettings.userAppId = @"USER_APP_ID";
vidcoinSettings.userGender = kVCAdapterUserGenderMale;
vidcoinSettings.userBithYear = @"1990";

[MPRewardedVideo loadRewardedVideoAdWithAdUnitID:adUnitId keywords:keywords location:nil customerId:@"customerId" mediationSettings:@[vidcoinSettings]];
```

## Notes
- The `rewardedVideoDidFailToLoadAdForCustomEvent`, `rewardedVideoDidExpireForCustomEvent`, `rewardedVideoWillLeaveApplicationForCustomEvent` and `rewardedVideoDidReceiveTapEventForCustomEvent` events for iOS are not supported for ads served with Vidcoin.

The latest documentation and code samples for the MoPub SDK are available  [here](http://www.mopub.com/resources/docs/ios-sdk-integration/ios-getting-started/).
