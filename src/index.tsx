import { NativeModules } from 'react-native';

// type BoostlingoSdkType = {
//   multiply(a: number, b: number): Promise<number>;
// };

const { BoostlingoSdk } = NativeModules;

export default BoostlingoSdk;// as BoostlingoSdkType;
