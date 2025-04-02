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
  error: string;
};

const UsagePermissionScreen = () => {
  const [hasPermission, setHasPermission] = useState(false);
  const [usageStats, setUsageStats] = useState<UsageStat[]>([]);
  const [currentApp, setCurrentApp] = useState<string | null>(null);

  const checkPermission = async () => {
    try {
      const permissionGranted = await UsageStatsModule.checkUsagePermission();
      setHasPermission(permissionGranted);
    } catch (error) {
      console.error('Error checking permission:', error);
    }
  };

  useEffect(() => {
    UsageStatsModule.getUsageStats()
      .then((stats: UsageStat[]) => {
        const sortedStats = stats.sort(
          (a, b) => b.totalTimeInForeground - a.totalTimeInForeground,
        );
        setUsageStats(sortedStats);
      })
      .catch((error: UsageStat[]) =>
        console.error('Error fetching usage stats', error),
      );
  }, []);

  useEffect(() => {
    checkPermission();
    const subscription = AppState.addEventListener('change', nextAppState => {
      if (nextAppState === 'active') {
        checkPermission();
      }
    });
    return () => subscription.remove();
  }, []);

  useEffect(() => {
    UsageStatsModule.startBackgroundService();
  }, []);

  useEffect(() => {
    const checkForegroundApp = () => {
      UsageStatsModule.getForegroundApp()
        .then((app: string) => {
          setCurrentApp(app);
        })
        .catch((err: any) => console.log('Error fetching foreground app', err));
    };

    // Check every 5 seconds
    const interval = setInterval(checkForegroundApp, 5000);

    return () => clearInterval(interval);
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
          <Text style={styles.title}>most recent app: {currentApp}</Text>
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
