/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React, {type PropsWithChildren} from 'react';

import {
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
  PermissionsAndroid,
  Button,
  UIManager,
  findNodeHandle, 
  requireNativeComponent, 
  NativeEventEmitter,
  NativeModules
} from 'react-native';

const { BoostlingoSdk } = NativeModules;
const VideoView = requireNativeComponent('BLVideoView');

var localVideoView: typeof VideoView;
var remoteVideoView: typeof VideoView;

const App = () => {

  const requestAudioPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
        {
          title: "RECORD_AUDIO",
          message:"RECORD_AUDIO",
          buttonNeutral: "Ask Me Later",
          buttonNegative: "Cancel",
          buttonPositive: "OK"
        }
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log("You can use the audio");
      } else {
        console.log("RECORD_AUDIO permission denied");
      }
    } catch (err) {
      console.warn(err);
    }
  };

  const requestCameraPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA,
        {
          title: "CAMERA",
          message: "CAMERA",
          buttonNeutral: "Ask Me Later",
          buttonNegative: "Cancel",
          buttonPositive: "OK"
        }
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log("You can use the camera");
      } else {
        console.log("CAMERA permission denied");
      }
    } catch (err) {
      console.warn(err);
    }
  };

  const [initializeResult, setInitializeResult] = React.useState<any | undefined>();
  const [result, setResult] = React.useState<any | undefined>();

  const eventEmitter = new NativeEventEmitter(BoostlingoSdk);
  // Don't forget to unsubscribe, typically in componentWillUnmount
  eventEmitter.addListener('callDidConnect', (event) => {
    setResult('callDidConnect ' + JSON.stringify(event));
    console.warn('callDidConnect ' + JSON.stringify(event));

    if (event.isVideo && event.participants) {
      var participant = event.participants[0];
      if (participant != null) {
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(remoteVideoView),
          UIManager.getViewManagerConfig('BLVideoView').Commands.attachAsRemoteRenderer,
          [participant.identity]);
      }
    }
  });
  eventEmitter.addListener('callDidDisconnect', (event) => {
    setResult('callDidDisconnect ' + JSON.stringify(event));
  });
  eventEmitter.addListener('callDidFailToConnect', (event) => {
    setResult('callDidFailToConnect ' + JSON.stringify(event));
  });
  eventEmitter.addListener('callParticipantConnected', (event) => {
    setResult('callParticipantConnected ' + JSON.stringify(event));
    console.warn('callParticipantConnected ' + JSON.stringify(event));

    BoostlingoSdk.getCurrentCall()
      .then((result: any) => {
        if (result.isVideo) {
          UIManager.dispatchViewManagerCommand(
            findNodeHandle(remoteVideoView),
            UIManager.getViewManagerConfig('BLVideoView').Commands.attachAsRemoteRenderer,
            [event.identity]);
        }
      })
      .catch((error: any) => {
        setResult(error);
      });
  });
  eventEmitter.addListener('callParticipantUpdated', (event) => {
    setResult('callParticipantUpdated ' + JSON.stringify(event))
  });
  eventEmitter.addListener('callParticipantDisconnected', (event) => {
    setResult('callParticipantDisconnected ' + JSON.stringify(event))
  });
  eventEmitter.addListener('chatConnected', () => {
    setResult('chatConnected')
  });
  eventEmitter.addListener('chatDisconnected', () => {
    setResult('chatDisconnected')
  });
  eventEmitter.addListener('chatMessageRecieved', (event) => {
    setResult('chatMessageRecieved ' + JSON.stringify(event))
  });
  eventEmitter.addListener('error', (event) => {
    setResult('error ' + JSON.stringify(event))
    console.warn('error ' + JSON.stringify(event));
  });

  React.useEffect(() => {
    BoostlingoSdk.initialize({
      "authToken": "",
      "region" : "qa"
    })
    .then(() => {
      setInitializeResult("OK");
      requestAudioPermission();
      requestCameraPermission();
    })
    .catch((error: any) => {
      setInitializeResult(JSON.stringify(error))
    });
  }, []);

  const styles = StyleSheet.create({
    container: {
      marginTop: 20,
      flex: 1,
      alignItems: 'center',
      justifyContent: 'center',
    },
    scrollView: {
      marginHorizontal: 0,
    },
    separator: {
      width: 300,
      height: 10,
      flex: 1, 
      alignItems: "center",
      justifyContent: "center"
    },
    video: {
      height: 300,
      width: 200,
      flex: 1, 
      alignItems: "center",
      justifyContent: "center"
    }
  });

  const remoteVideoViewProps = {
    style: styles.video,
    ref: (e: any) => { remoteVideoView = e; }
  };

  const localVideoViewProps = {
    style: styles.video,
    ref: (e: any) => { localVideoView = e; }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={styles.scrollView}>
        <View style={styles.separator} />
        <VideoView {...remoteVideoViewProps}/>
        <View style={styles.separator} />
        <VideoView {...localVideoViewProps}/>
        <Text>Initialize: {initializeResult}</Text>
        <Text>Result: {result}</Text>
        <Button
          title="getRegions"
          onPress={() => {
            BoostlingoSdk.getRegions()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
        <Button
          title="getVersion"
          onPress={() => {
            BoostlingoSdk.getVersion()
            .then((result: any) => {
              setResult(result);
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
        <Button
          title="getCurrentCall"
          onPress={() => {
            BoostlingoSdk.getCurrentCall()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
        <Button
          title="getCallDictionaries"
          onPress={() => {
            BoostlingoSdk.getCallDictionaries()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
        <Button
          title="getProfile"
          onPress={() => {
            BoostlingoSdk.getProfile()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
         <Button
          title="getVoiceLanguages"
          onPress={() => {
            BoostlingoSdk.getVoiceLanguages()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
         <Button
          title="getVideoLanguages"
          onPress={() => {
            BoostlingoSdk.getVideoLanguages()
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
        <Button
          title="getCallDetails(38554)"
          onPress={() => {
            BoostlingoSdk.getCallDetails(38554)
            .then((result: any) => {
              setResult(JSON.stringify(result));
            })
            .catch((error: any) => {
              setResult(JSON.stringify(error));
            });
          }}
        />
        <Button
          title="makeVoiceCall"
          onPress={() => {
            BoostlingoSdk.makeVoiceCall({
              "languageFromId": 4,
              "languageToId": 1,
              "serviceTypeId": 2,
              "genderId": null,
              "data": [{
                "key": "CustomFieldKey1",
                "value": "CustomFieldValue1"
              }, {
                "key": "CustomFieldKey2",
                "value": "CustomFieldValue2"
              }]
            })
            .then((call: any) => {
              setResult(JSON.stringify(call));
            })
            .catch((error: any) => {
              setResult(JSON.stringify(error));
            });
          }}
          />
          <Button
          title="makeVideoCall"
          onPress={() => {
            (new Promise(resolve => 
              {
                UIManager.dispatchViewManagerCommand(
                  findNodeHandle(localVideoView),
                  UIManager.getViewManagerConfig('BLVideoView').Commands.attachAsLocalRenderer,
                  []);

                setTimeout(resolve, 1000);
              }))
            .then(() => {
              BoostlingoSdk.makeVideoCall({
                "languageFromId": 4,
                "languageToId": 1,
                "serviceTypeId": 2,
                "genderId": null,
                "data": [{
                  "key": "CustomFieldKey1",
                  "value": "CustomFieldValue1"
                }, {
                  "key": "CustomFieldKey2",
                  "value": "CustomFieldValue2"
                }]
              })
              .then((call: any) => {
                setResult(JSON.stringify(call));
              })
              .catch((error: any) => {
                setResult(JSON.stringify(error));
              });
            });
          }}
          />
          <Button
          title="hangUp"
          onPress={() => {
            BoostlingoSdk.hangUp()
              .then(() => {
                setResult("OK");
              })
              .catch((error: any) => {
                setResult(JSON.stringify(error));
              });
          }}
        />
          <Button
          title="toggleAudioRoute(true)"
          onPress={() => {
            BoostlingoSdk.toggleAudioRoute(true);
          }}
          />
          <Button
          title="toggleAudioRoute(false)"
          onPress={() => {
            BoostlingoSdk.toggleAudioRoute(false);
          }}
        />
        <Button
          title="muteCall(true)"
          onPress={() => {
            BoostlingoSdk.muteCall(true);
          }}
          />
        <Button
          title="muteCall(false)"
          onPress={() => {
            BoostlingoSdk.muteCall(false);
          }}
        />
        <Button
          title="enableVideo(true)"
          onPress={() => {
            BoostlingoSdk.enableVideo(true);
          }}
          />
          <Button
          title="enableVideo(false)"
          onPress={() => {
            BoostlingoSdk.enableVideo(false);
          }}
        />
        <Button
          title="flipCamera()"
          onPress={() => {
            BoostlingoSdk.flipCamera();
          }}
        />
        <Button
          title="sendChatMessage('test')"
          onPress={() => {
            BoostlingoSdk.sendChatMessage('test')
              .then((message: any) => {
                setResult(JSON.stringify(message));
              })
              .catch((error: any) => {
                setResult(JSON.stringify(error));
              });
          }}
        />
        <Button 
          title="dialThirdParty(phone: '18004444444')"
          onPress={() => {
            BoostlingoSdk.dialThirdParty('18004444444')
              .then(() => {
                setResult("OK");
              })
              .catch((error: any) => {
                setResult(JSON.stringify(error));
              });
          }}
        />
        <Button 
          title="hangUpThirdParty"
          onPress={() => {
            BoostlingoSdk.getCurrentCall()
              .then((result: any) => {
                for (let i = 0; i < result.participants.length; i++) {
                  const participant = result.participants[i];
                  if (participant.participantType == 3) {
                    BoostlingoSdk.hangUpThirdParty(participant.identity)
                      .then(() => {
                        setResult("OK");
                      })
                      .catch((error: any) => {
                        setResult(JSON.stringify(error));
                      });
                  }
                }
              })
              .catch((error: any) => {
                setResult(error);
              });
          }}
        />
        <Button 
          title="muteThirdParty(true)"
          onPress={() => {
            BoostlingoSdk.getCurrentCall()
              .then((result: any) => {
                for (let i = 0; i < result.participants.length; i++) {
                  const participant = result.participants[i];
                  if (participant.participantType == 3) {
                    BoostlingoSdk.muteThirdParty([participant.identity, true])
                      .then(() => {
                        setResult("OK");
                      })
                      .catch((error: any) => {
                        setResult(JSON.stringify(error));
                      });
                  }
                }
              })
              .catch((error: any) => {
                setResult(error);
              });
          }}
        />
        <Button 
          title="muteThirdParty(false)"
          onPress={() => {
            BoostlingoSdk.getCurrentCall()
              .then((result: any) => {
                for (let i = 0; i < result.participants.length; i++) {
                  const participant = result.participants[i];
                  if (participant.participantType == 3) {
                    BoostlingoSdk.muteThirdParty([participant.identity, false])
                      .then(() => {
                        setResult("OK");
                      })
                      .catch((error: any) => {
                        setResult(JSON.stringify(error));
                      });
                  }
                }
              })
              .catch((error: any) => {
                setResult(error);
              });
          }}
        />
        <Button 
          title="dispose"
          onPress={() => {
            BoostlingoSdk.dispose();
            eventEmitter.removeAllListeners('callDidConnect');
            eventEmitter.removeAllListeners('callDidDisconnect');
            eventEmitter.removeAllListeners('callDidFailToConnect');
            eventEmitter.removeAllListeners('callParticipantConnected');
            eventEmitter.removeAllListeners('callParticipantUpdated');
            eventEmitter.removeAllListeners('callParticipantDisconnected');
            eventEmitter.removeAllListeners('chatConnected');
            eventEmitter.removeAllListeners('chatDisconnected');
            eventEmitter.removeAllListeners('chatMessageRecieved');
            eventEmitter.removeAllListeners('error');
          }}
        />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
