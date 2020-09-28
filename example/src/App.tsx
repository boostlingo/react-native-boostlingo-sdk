import * as React from 'react';
import { StyleSheet, Text, ScrollView, SafeAreaView } from 'react-native';
import BoostlingoSdk from 'react-native-boostlingo-sdk';

export default function App() {
  const [multiplyResult, setMultiplyResult] = React.useState<number | undefined>();
  const [getRegionsResult, setGetRegionsResult] = React.useState<any | undefined>();
  const [getVersionResult, setGetVersionResult] = React.useState<any | undefined>();
  const [initializeResult, setInitializeResult] = React.useState<any | undefined>();
  const [getCurrentCallResult, setGetCurrentCallResult] = React.useState<any | undefined>();
  const [getCallDictionariesResult, setGetCallDictionariesResult] = React.useState<any | undefined>();
  const [getProfileResult, setGetProfilesResult] = React.useState<any | undefined>();
  const [getVoiceLanguagesResult, setGetVoiceLanguages] = React.useState<any | undefined>();

  React.useEffect(() => {
    BoostlingoSdk.multiply(3, 9)
    .then((result: any) => {
      setMultiplyResult(result);
    });

    BoostlingoSdk.getRegions()
    .then((result: any) => {
      setGetRegionsResult(result);
    })

    BoostlingoSdk.getVersion()
    .then((result: any) => {
      setGetVersionResult(result);
    });

    BoostlingoSdk.initialize({
      "authToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1laWQiOiIxMSIsInJvbGUiOiJDb3Jwb3JhdGVDbGllbnRSb290QWRtaW4iLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3VzZXJkYXRhIjoie1wiQ3VycmVudENvbXBhbnlJZFwiOjksXCJNYXN0ZXJDb21wYW55SWRcIjo4fSIsImNyZWF0ZWQiOiIwOS8yMC8yMDIwIDE4OjUwOjMwIiwiY3VsdHVyZUlkIjoiMSIsIm5iZiI6MTYwMDYyNzgzMCwiZXhwIjoxNjAzMjE5ODMwLCJpYXQiOjE2MDA2Mjc4MzAsImlzcyI6IkJvb3N0bGluZ28iLCJhdWQiOiJVc2VycyJ9.RqNJuojLd-cJC7LEkZJwO9aPjvzg1jx_dG94P5kq65INUms7va7YLhq2_3m51QTcqSl55-Umgbw21_2kSqw-xpmHBpA0yOVouLu9xSm0i3TLBayjCdxVi8psBUakqq08t__C6IRI0xflj1YB4pjIqJEVncHWfVyfzTYsiKsl1c4flwYW82SAj9letHGi-RgJRhhYtkf2HcuebtHRMxOKiWJ6gyea7e1A2864RWq8SmLpJSh9KQiI_EsWpFfOTUTXIqqv0w5tFa3v9m26xitlW5TzUE4IXGpnivR9hiCh2Omr2BF1IAx-BtQHxDm3rh4or7mrRt7AEhOMzWLfS4AlEQ",
      "region" : "qa"
    })
    .then((result: any) => {
      setInitializeResult(result);

      BoostlingoSdk.getCurrentCall()
      .then((result: any) => {
        setGetCurrentCallResult(result);
      });
  
      BoostlingoSdk.getCallDictionaries()
      .then((result: any) => {
        setGetCallDictionariesResult(result);
      });
  
      BoostlingoSdk.getProfile()
      .then((result: any) => {
        setGetProfilesResult(result);
      });

      BoostlingoSdk.getVoiceLanguages()
      .then((result: any) => {
        setGetVoiceLanguages(result);
      });
    });

    // BoostlingoSdk.getVersion()
    // .then((version: string) => {
    //   setResult(version)
    //   // BoostlingoSdk.initialize({
    //   //   "authToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1laWQiOiIxMSIsInJvbGUiOiJDb3Jwb3JhdGVDbGllbnRSb290QWRtaW4iLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3VzZXJkYXRhIjoie1wiQ3VycmVudENvbXBhbnlJZFwiOjksXCJNYXN0ZXJDb21wYW55SWRcIjo4fSIsImNyZWF0ZWQiOiIwOS8yMC8yMDIwIDE4OjUwOjMwIiwiY3VsdHVyZUlkIjoiMSIsIm5iZiI6MTYwMDYyNzgzMCwiZXhwIjoxNjAzMjE5ODMwLCJpYXQiOjE2MDA2Mjc4MzAsImlzcyI6IkJvb3N0bGluZ28iLCJhdWQiOiJVc2VycyJ9.RqNJuojLd-cJC7LEkZJwO9aPjvzg1jx_dG94P5kq65INUms7va7YLhq2_3m51QTcqSl55-Umgbw21_2kSqw-xpmHBpA0yOVouLu9xSm0i3TLBayjCdxVi8psBUakqq08t__C6IRI0xflj1YB4pjIqJEVncHWfVyfzTYsiKsl1c4flwYW82SAj9letHGi-RgJRhhYtkf2HcuebtHRMxOKiWJ6gyea7e1A2864RWq8SmLpJSh9KQiI_EsWpFfOTUTXIqqv0w5tFa3v9m26xitlW5TzUE4IXGpnivR9hiCh2Omr2BF1IAx-BtQHxDm3rh4or7mrRt7AEhOMzWLfS4AlEQ",
    //   //   "region" : "qa"
    //   // })
    //   // .then(() => {
    //   //   BoostlingoSdk.getProfile()
    //   //   .then((profile: { accountName: any; }) => 
    //   //     {
    //   //       setResult(profile.accountName)
    //   //       // BoostlingoSdk.makeVoiceCall({
    //   //       //   "languageFromId": 4,
    //   //       //   "languageToId": 1,
    //   //       //   "serviceTypeId": 2,
    //   //       //   "genderId": null
    //   //       // })
    //   //       // .then((call: any) => {
  
    //   //       // });
    //   //     });
    //   // });
    // });
  }, []);

  return (
    <SafeAreaView style={styles.container}>
       <ScrollView style={styles.scrollView}>
        <Text>multiply: {multiplyResult}</Text>
        <Text>getRegionsResult: {getRegionsResult}</Text>
        <Text>getVersionResult: {getVersionResult}</Text>
        <Text>initialize: {initializeResult}</Text>
        <Text>getCurrentCall: {getCurrentCallResult}</Text>
        {/* <Text>getCallDictionaries: {JSON.stringify(getCallDictionariesResult)}</Text> */}
        <Text>getProfile: {JSON.stringify(getProfileResult)}</Text>
        {/* <Text>getVoiceLanguages: {JSON.stringify(getVoiceLanguagesResult)}</Text> */}
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
  }
});
