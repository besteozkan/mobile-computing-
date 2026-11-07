// Local persistence: the last result is cached on the device so the app
// still shows something when the phone is offline.
import AsyncStorage from '@react-native-async-storage/async-storage';

const KEY = 'skycheck:last-weather';

export async function saveWeather(entry) {
  try {
    await AsyncStorage.setItem(KEY, JSON.stringify({ ...entry, savedAt: Date.now() }));
  } catch {
    // Caching is best-effort; ignore storage errors.
  }
}

export async function loadWeather() {
  try {
    const raw = await AsyncStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}
