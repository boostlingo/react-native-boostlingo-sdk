#import "React/RCTViewManager.h"
#import "React/RCTEventEmitter.h"

@interface RCT_EXTERN_MODULE(BLVideoView, RCTViewManager)

    RCT_EXTERN_METHOD(attachAsLocalRenderer:(nonnull NSNumber *)node commandID:(NSInteger)commandID commandArgs:(NSArray *)commandArgs)

    RCT_EXTERN_METHOD(attachAsRemoteRenderer:(nonnull NSNumber *)node commandID:(NSInteger)commandID commandArgs:(NSArray *)commandArgs)

@end

@interface RCT_EXTERN_MODULE(BoostlingoSdk, RCTEventEmitter)

    RCT_EXTERN_METHOD(supportedEvents)

    RCT_EXTERN_METHOD(getRegions:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getVersion:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(initialize:(NSDictionary *)config resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getCurrentCall:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getCallDictionaries:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getProfile:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getVoiceLanguages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getVideoLanguages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(getCallDetails:(NSInteger *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(makeVoiceCall:(NSDictionary *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(makeVideoCall:(NSDictionary *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(hangUp:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(toggleAudioRoute:(BOOL)toSpeaker)

    RCT_EXTERN_METHOD(sendChatMessage:(NSString *)text resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(muteCall:(BOOL)isMuted)

    RCT_EXTERN_METHOD(enableVideo:(BOOL)isVideoEnabled)

    RCT_EXTERN_METHOD(flipCamera)

    RCT_EXTERN_METHOD(dialThirdParty:(NSString *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(hangUpThirdParty:(NSString *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(muteThirdParty:(NSString *)request resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

    RCT_EXTERN_METHOD(dispose)

@end
