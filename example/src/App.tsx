import * as React from 'react';
import { StyleSheet, View, Text } from 'react-native';
import BoostlingoSdk from 'react-native-boostlingo-sdk';

export default function App() {
  const [result, setResult] = React.useState<any | undefined>();

  React.useEffect(() => {
    BoostlingoSdk.getRegions().then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
