// Network layer: talks to the free Open-Meteo APIs (no API key needed).

const FORECAST_URL = 'https://api.open-meteo.com/v1/forecast';
const GEOCODING_URL = 'https://geocoding-api.open-meteo.com/v1/search';
const REVERSE_URL = 'https://nominatim.openstreetmap.org/reverse';

const TIMEOUT_MS = 10000;

// fetch() with a timeout, so a weak mobile connection does not hang the app.
async function fetchJson(url, options = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    if (!response.ok) {
      throw new Error(`Server error (${response.status})`);
    }
    return await response.json();
  } catch (e) {
    if (e.name === 'AbortError') throw new Error('Request timed out');
    throw e;
  } finally {
    clearTimeout(timer);
  }
}

// Current weather + 5-day forecast for a coordinate.
export async function getWeather(latitude, longitude) {
  const params = new URLSearchParams({
    latitude: String(latitude),
    longitude: String(longitude),
    current: 'temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code',
    daily: 'weather_code,temperature_2m_max,temperature_2m_min',
    timezone: 'auto',
    forecast_days: '5',
  });
  const data = await fetchJson(`${FORECAST_URL}?${params}`);

  return {
    current: {
      temperature: Math.round(data.current.temperature_2m),
      feelsLike: Math.round(data.current.apparent_temperature),
      humidity: data.current.relative_humidity_2m,
      wind: Math.round(data.current.wind_speed_10m),
      code: data.current.weather_code,
    },
    daily: data.daily.time.map((date, i) => ({
      date,
      code: data.daily.weather_code[i],
      max: Math.round(data.daily.temperature_2m_max[i]),
      min: Math.round(data.daily.temperature_2m_min[i]),
    })),
  };
}

// City name -> list of matching places with coordinates.
export async function searchCity(name) {
  const params = new URLSearchParams({ name, count: '5', language: 'en' });
  const data = await fetchJson(`${GEOCODING_URL}?${params}`);
  return (data.results || []).map((r) => ({
    name: r.name,
    country: r.country,
    latitude: r.latitude,
    longitude: r.longitude,
  }));
}

// GPS coordinate -> city name (reverse geocoding). Falls back to coordinates.
export async function getPlaceName(latitude, longitude) {
  try {
    const params = new URLSearchParams({
      lat: String(latitude),
      lon: String(longitude),
      format: 'json',
      zoom: '10',
    });
    const data = await fetchJson(`${REVERSE_URL}?${params}`, {
      headers: { 'User-Agent': 'SkyCheck-student-project' },
    });
    const a = data.address || {};
    return a.city || a.town || a.village || a.province || a.state || 'My location';
  } catch {
    return `${latitude.toFixed(2)}, ${longitude.toFixed(2)}`;
  }
}
