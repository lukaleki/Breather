import React, {useEffect, useState} from 'react';
import {
  View,
  Text,
  Button,
  NativeModules,
  AppState,
  StyleSheet,
  FlatList,
} from 'react-native';

const {UsageStatsModule} = NativeModules;

type UsageStat = {
  packageName: string;
  totalTimeInForeground: number;
};

const UsagePermissionScreen = () => {
  const [hasPermission, setHasPermission] = useState(false);
  const [usageStats, setUsageStats] = useState<UsageStat[]>([]);

  const checkPermission = async () => {
    try {
      const permissionGranted = await UsageStatsModule.checkUsagePermission();
      setHasPermission(permissionGranted);
    } catch (error) {
      console.error('Error checking permission:', error);
    }
  };

  useEffect(() => {
    checkPermission();
    const subscription = AppState.addEventListener('change', nextAppState => {
      if (nextAppState === 'active') {
        checkPermission();
      }
    });
    return () => subscription.remove();
  }, []);

  const requestPermission = () => {
    UsageStatsModule.openUsageAccessSettings();
  };

  const fetchUsageStats = async () => {
    try {
      const stats = await UsageStatsModule.getUsageStats();
      setUsageStats(stats);
    } catch (error) {
      console.error('Error fetching usage stats:', error);
    }
  };

  useEffect(() => {
    fetchUsageStats();
  }, []);

  return (
    <View style={styles.container}>
      {!hasPermission && (
        <>
          <Text style={styles.title}>Enable Usage Access</Text>
          <Text style={styles.description}>
            This app requires usage access permission. Please enable it in
            settings.
          </Text>
          <Button title="Grant Permission" onPress={requestPermission} />
        </>
      )}
      {hasPermission && (
        <View>
          <Text style={styles.title}>App Usage Stats (Last 24h)</Text>
          <FlatList
            data={usageStats}
            keyExtractor={item => item.packageName}
            renderItem={({item}) => (
              <View style={styles.item}>
                <Text style={styles.appName}>{item.packageName}</Text>
                <Text style={styles.time}>
                  Time in foreground:{' '}
                  {(item.totalTimeInForeground / 60000).toFixed(2)} min
                </Text>
              </View>
            )}
          />
          <Button title="Refresh" onPress={fetchUsageStats} />
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    color: 'white',
  },
  title: {fontSize: 18, fontWeight: 'bold', marginBottom: 10, color: 'white'},
  description: {
    fontSize: 14,
    textAlign: 'center',
    marginBottom: 20,
    color: 'white',
  },
  item: {padding: 10, borderBottomWidth: 1, borderColor: '#ddd'},
  appName: {fontSize: 16, fontWeight: 'bold'},
  time: {fontSize: 14, color: 'gray'},
});

export default UsagePermissionScreen;
