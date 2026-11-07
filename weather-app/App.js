import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Keyboard,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import * as Location from 'expo-location';

import { getPlaceName, getWeather, searchCity } from './src/api';
import { loadWeather, saveWeather } from './src/storage';
import { dayName, describeWeather } from './src/weatherCodes';

export default function App() {
  const [place, setPlace] = useState(null); // { name, latitude, longitude }
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [offlineSince, setOfflineSince] = useState(null); // set when showing cached data

  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  // Download weather for a place, cache it, and fall back to the cache on failure.
  const loadPlace = useCallback(async (p) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getWeather(p.latitude, p.longitude);
      setPlace(p);
      setWeather(data);
      setOfflineSince(null);
      await saveWeather({ place: p, weather: data });
    } catch (e) {
      const cached = await loadWeather();
      if (cached) {
        setPlace(cached.place);
        setWeather(cached.weather);
        setOfflineSince(cached.savedAt);
      }
      setError(`Could not load weather: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }, []);

  // Ask for location permission, read GPS, then load weather for that point.
  const loadMyLocation = useCallback(async () => {
    setError(null);
    const { status } = await Location.requestForegroundPermissionsAsync();
    if (status !== 'granted') {
      setError('Location permission denied. Search for a city instead.');
      return;
    }
    setLoading(true);
    try {
      const position =
        (await Location.getLastKnownPositionAsync({ maxAge: 10 * 60 * 1000 })) ||
        (await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced }));
      const { latitude, longitude } = position.coords;
      const name = await getPlaceName(latitude, longitude);
      await loadPlace({ name, latitude, longitude });
    } catch (e) {
      setLoading(false);
      setError('Could not get your location. Is GPS turned on?');
    }
  }, [loadPlace]);

  // On start: show cached data immediately, then try the current location.
  useEffect(() => {
    (async () => {
      const cached = await loadWeather();
      if (cached) {
        setPlace(cached.place);
        setWeather(cached.weather);
      }
      loadMyLocation();
    })();
  }, [loadMyLocation]);

  const onSearch = async () => {
    const text = query.trim();
    if (!text) return;
    Keyboard.dismiss();
    setError(null);
    try {
      const found = await searchCity(text);
      setResults(found);
      if (found.length === 0) setError(`No city found for "${text}".`);
    } catch (e) {
      setError(`Search failed: ${e.message}`);
    }
  };

  const pickCity = (city) => {
    setResults([]);
    setQuery('');
    loadPlace({ name: `${city.name}, ${city.country}`, latitude: city.latitude, longitude: city.longitude });
  };

  const now = weather && describeWeather(weather.current.code);

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="dark" />
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
        refreshControl={
          <RefreshControl refreshing={loading} onRefresh={() => place && loadPlace(place)} />
        }
      >
        <Text style={styles.appTitle}>SkyCheck</Text>

        {/* Search bar */}
        <View style={styles.searchRow}>
          <TextInput
            style={styles.input}
            placeholder="Search a city…"
            value={query}
            onChangeText={setQuery}
            onSubmitEditing={onSearch}
            returnKeyType="search"
          />
          <Pressable style={styles.button} onPress={onSearch}>
            <Text style={styles.buttonText}>Search</Text>
          </Pressable>
        </View>

        {results.map((city, i) => (
          <Pressable key={`${city.latitude}-${i}`} style={styles.result} onPress={() => pickCity(city)}>
            <Text>{city.name}, {city.country}</Text>
          </Pressable>
        ))}

        <Pressable style={styles.locationButton} onPress={loadMyLocation}>
          <Text style={styles.locationText}>📍 Use my location</Text>
        </Pressable>

        {error && <Text style={styles.error}>{error}</Text>}
        {offlineSince && (
          <Text style={styles.offline}>
            Offline – showing saved data from {new Date(offlineSince).toLocaleString()}
          </Text>
        )}

        {loading && !weather && <ActivityIndicator size="large" style={{ marginTop: 40 }} />}

        {weather && (
          <>
            {/* Current weather */}
            <View style={styles.card}>
              <Text style={styles.place}>{place?.name}</Text>
              <Text style={styles.icon}>{now.icon}</Text>
              <Text style={styles.temp}>{weather.current.temperature}°C</Text>
              <Text style={styles.label}>{now.label}</Text>
              <View style={styles.detailsRow}>
                <Detail title="Feels like" value={`${weather.current.feelsLike}°`} />
                <Detail title="Humidity" value={`${weather.current.humidity}%`} />
                <Detail title="Wind" value={`${weather.current.wind} km/h`} />
              </View>
            </View>

            {/* 5-day forecast */}
            <View style={styles.card}>
              <Text style={styles.sectionTitle}>5-day forecast</Text>
              {weather.daily.map((d, i) => (
                <View key={d.date} style={styles.dayRow}>
                  <Text style={styles.dayName}>{dayName(d.date, i)}</Text>
                  <Text style={styles.dayIcon}>{describeWeather(d.code).icon}</Text>
                  <Text style={styles.dayTemp}>
                    {d.min}° / {d.max}°
                  </Text>
                </View>
              ))}
            </View>
          </>
        )}

        <Text style={styles.footer}>Pull down to refresh · Data: Open-Meteo</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

function Detail({ title, value }) {
  return (
    <View style={styles.detail}>
      <Text style={styles.detailValue}>{value}</Text>
      <Text style={styles.detailTitle}>{title}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#e8f1fb' },
  container: { padding: 16, paddingTop: 40 },
  appTitle: { fontSize: 28, fontWeight: '700', color: '#1d3557', marginBottom: 12 },
  searchRow: { flexDirection: 'row', gap: 8 },
  input: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 16,
  },
  button: { backgroundColor: '#1d3557', borderRadius: 10, paddingHorizontal: 16, justifyContent: 'center' },
  buttonText: { color: '#fff', fontWeight: '600' },
  result: { backgroundColor: '#fff', padding: 12, marginTop: 4, borderRadius: 8 },
  locationButton: { marginTop: 10, alignSelf: 'flex-start' },
  locationText: { color: '#1d3557', fontWeight: '600', fontSize: 15 },
  error: { color: '#b00020', marginTop: 10 },
  offline: { color: '#8a6d00', backgroundColor: '#fff4cc', padding: 8, borderRadius: 8, marginTop: 10 },
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 20, marginTop: 16 },
  place: { fontSize: 20, fontWeight: '600', textAlign: 'center', color: '#1d3557' },
  icon: { fontSize: 64, textAlign: 'center', marginVertical: 8 },
  temp: { fontSize: 48, fontWeight: '700', textAlign: 'center' },
  label: { fontSize: 18, textAlign: 'center', color: '#555' },
  detailsRow: { flexDirection: 'row', justifyContent: 'space-around', marginTop: 16 },
  detail: { alignItems: 'center' },
  detailValue: { fontSize: 18, fontWeight: '600' },
  detailTitle: { fontSize: 13, color: '#777' },
  sectionTitle: { fontSize: 17, fontWeight: '600', marginBottom: 8, color: '#1d3557' },
  dayRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  dayName: { flex: 1, fontSize: 16 },
  dayIcon: { fontSize: 22, width: 50, textAlign: 'center' },
  dayTemp: { width: 90, textAlign: 'right', fontSize: 16 },
  footer: { textAlign: 'center', color: '#888', marginTop: 20, fontSize: 12 },
});
