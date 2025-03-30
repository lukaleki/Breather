import React, {useEffect, useState} from 'react';
import {View, Text, Button, Linking} from 'react-native';
import {NativeModules} from 'react-native';

const {UsageStats} = NativeModules;

export default function App() {
  const [hasPermission, setHasPermission] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const permission = await UsageStats.checkUsageAccessPermission();
        setHasPermission(permission);
        console.log('Usage Access Granted?', permission);
      } catch (error) {
        console.error('Error checking permission:', error);
      }
    })();
  }, []);

  const openSettings = () => {
    Linking.openSettings();
  };

  return (
    <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
      <Text>
        {hasPermission === null
          ? 'Checking permission...'
          : hasPermission
          ? 'Permission Granted'
          : 'Permission Denied'}
      </Text>
      {hasPermission === false && (
        <Button title="Open Settings" onPress={openSettings} />
      )}
    </View>
  );
}
