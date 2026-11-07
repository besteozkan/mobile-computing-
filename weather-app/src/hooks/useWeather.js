// Custom React hook: holds all weather state and logic (location, network,
// offline cache) so UI components only have to render.
import { useCallback, useEffect, useState } from 'react';
import * as Location from 'expo-location';

import { getPlaceName, getWeather } from '../api';
import { loadWeather, saveWeather } from '../storage';

export function useWeather() {
  const [place, setPlace] = useState(null); // { name, latitude, longitude }
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [offlineSince, setOfflineSince] = useState(null); // set when showing cached data

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
    } catch {
      setLoading(false);
      setError('Could not get your location. Is GPS turned on?');
    }
  }, [loadPlace]);

  const refresh = useCallback(() => {
    if (place) loadPlace(place);
  }, [place, loadPlace]);

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

  return { place, weather, loading, error, setError, offlineSince, loadPlace, loadMyLocation, refresh };
}
