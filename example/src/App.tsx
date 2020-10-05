import * as React from 'react';
import { UIManager, findNodeHandle, requireNativeComponent, NativeEventEmitter, StyleSheet, Text, ScrollView, SafeAreaView, Button, PermissionsAndroid } from 'react-native';
import BoostlingoSdk from 'react-native-boostlingo-sdk';

const VideoView = requireNativeComponent('BLVideoView');

export default function App() {

  var localVideoView: typeof VideoView;
  var remoteVideoView: typeof VideoView;

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
    setResult('callDidConnect ' + JSON.stringify(event))
  });
  eventEmitter.addListener('callDidDisconnect', (event) => {
    setResult('callDidDisconnect ' + JSON.stringify(event))
  });
  eventEmitter.addListener('callDidFailToConnect', (event) => {
    setResult('callDidFailToConnect ' + JSON.stringify(event))
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

  React.useEffect(() => {
    BoostlingoSdk.initialize({
      "authToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1laWQiOiIxMSIsInJvbGUiOiJDb3Jwb3JhdGVDbGllbnRSb290QWRtaW4iLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3VzZXJkYXRhIjoie1wiQ3VycmVudENvbXBhbnlJZFwiOjksXCJNYXN0ZXJDb21wYW55SWRcIjo4fSIsImNyZWF0ZWQiOiIwOS8yMC8yMDIwIDE4OjUwOjMwIiwiY3VsdHVyZUlkIjoiMSIsIm5iZiI6MTYwMDYyNzgzMCwiZXhwIjoxNjAzMjE5ODMwLCJpYXQiOjE2MDA2Mjc4MzAsImlzcyI6IkJvb3N0bGluZ28iLCJhdWQiOiJVc2VycyJ9.RqNJuojLd-cJC7LEkZJwO9aPjvzg1jx_dG94P5kq65INUms7va7YLhq2_3m51QTcqSl55-Umgbw21_2kSqw-xpmHBpA0yOVouLu9xSm0i3TLBayjCdxVi8psBUakqq08t__C6IRI0xflj1YB4pjIqJEVncHWfVyfzTYsiKsl1c4flwYW82SAj9letHGi-RgJRhhYtkf2HcuebtHRMxOKiWJ6gyea7e1A2864RWq8SmLpJSh9KQiI_EsWpFfOTUTXIqqv0w5tFa3v9m26xitlW5TzUE4IXGpnivR9hiCh2Omr2BF1IAx-BtQHxDm3rh4or7mrRt7AEhOMzWLfS4AlEQ",
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

  return (
    <SafeAreaView style={styles.container}>
       <ScrollView style={styles.scrollView}>
       <VideoView 
        style={styles.video}
        ref={(e: any) => {
          localVideoView = e;
        }}/>
        <VideoView 
        style={styles.video}
        ref={(e: any) => {
          remoteVideoView = e;
        }}/>
        <Text>initialize: {initializeResult}</Text>
        <Button
          title="multiply"
          onPress={() => {
            BoostlingoSdk.multiply(3, 9)
            .then((result: any) => {
              setResult(result);
            })
            .catch((error: any) => {
              setResult(error);
            });
          }}
        />
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
                        "genderId": null
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
            UIManager.dispatchViewManagerCommand(
              findNodeHandle(localVideoView),
              UIManager.getViewManagerConfig('BLVideoView').Commands.attachAsLocal,
              []);
            UIManager.dispatchViewManagerCommand(
              findNodeHandle(remoteVideoView),
              UIManager.getViewManagerConfig('BLVideoView').Commands.attachAsRemote,
              []);
            BoostlingoSdk.makeVideoCall({
                        "languageFromId": 4,
                        "languageToId": 1,
                        "serviceTypeId": 2,
                        "genderId": null
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
        <Text>Result: {result}</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

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
  video: {
    height: 200,
    width: 200,
    backgroundColor: 'gray',
    flex: 1, 
    alignItems: "center",
    justifyContent: "center"
  }
});
