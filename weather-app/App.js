// Root React component: composes the screen from smaller components.
// All data and logic come from the custom useWeather() hook.
import { Pressable, RefreshControl, SafeAreaView, ScrollView, StyleSheet, Text, ActivityIndicator } from 'react-native';
import { StatusBar } from 'expo-status-bar';

import { useWeather } from './src/hooks/useWeather';
import SearchBar from './src/components/SearchBar';
import StatusMessages from './src/components/StatusMessages';
import CurrentWeatherCard from './src/components/CurrentWeatherCard';
import ForecastList from './src/components/ForecastList';

export default function App() {
  const { place, weather, loading, error, setError, offlineSince, loadPlace, loadMyLocation, refresh } =
    useWeather();

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="dark" />
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
        refreshControl={<RefreshControl refreshing={loading} onRefresh={refresh} />}
      >
        <Text style={styles.appTitle}>SkyCheck</Text>

        <SearchBar onSelect={loadPlace} onError={setError} />

        <Pressable style={styles.locationButton} onPress={loadMyLocation}>
          <Text style={styles.locationText}>📍 Use my location</Text>
        </Pressable>

        <StatusMessages error={error} offlineSince={offlineSince} />

        {loading && !weather && <ActivityIndicator size="large" style={styles.spinner} />}

        {weather && (
          <>
            <CurrentWeatherCard placeName={place?.name} current={weather.current} />
            <ForecastList days={weather.daily} />
          </>
        )}

        <Text style={styles.footer}>Pull down to refresh · Data: Open-Meteo</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#e8f1fb' },
  container: { padding: 16, paddingTop: 40 },
  appTitle: { fontSize: 28, fontWeight: '700', color: '#1d3557', marginBottom: 12 },
  locationButton: { marginTop: 10, alignSelf: 'flex-start' },
  locationText: { color: '#1d3557', fontWeight: '600', fontSize: 15 },
  spinner: { marginTop: 40 },
  footer: { textAlign: 'center', color: '#888', marginTop: 20, fontSize: 12 },
});
