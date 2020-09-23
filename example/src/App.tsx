import * as React from 'react';
import { StyleSheet, View, Text } from 'react-native';
import BoostlingoSdk from 'react-native-boostlingo-sdk';

export default function App() {
  // const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    // BoostlingoSdk.multiply(3, 7).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      {/* <Text>Result: {result}</Text> */}
      <Text>Result: BoostlingoSdk</Text>
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
