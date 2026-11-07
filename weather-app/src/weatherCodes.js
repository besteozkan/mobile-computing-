// WMO weather codes used by Open-Meteo -> label + emoji icon.
const CODES = {
  0: ['Clear sky', '☀️'],
  1: ['Mainly clear', '🌤️'],
  2: ['Partly cloudy', '⛅'],
  3: ['Overcast', '☁️'],
  45: ['Fog', '🌫️'],
  48: ['Rime fog', '🌫️'],
  51: ['Light drizzle', '🌦️'],
  53: ['Drizzle', '🌦️'],
  55: ['Heavy drizzle', '🌧️'],
  61: ['Light rain', '🌦️'],
  63: ['Rain', '🌧️'],
  65: ['Heavy rain', '🌧️'],
  71: ['Light snow', '🌨️'],
  73: ['Snow', '❄️'],
  75: ['Heavy snow', '❄️'],
  80: ['Rain showers', '🌦️'],
  81: ['Rain showers', '🌧️'],
  82: ['Violent showers', '⛈️'],
  95: ['Thunderstorm', '⛈️'],
  96: ['Thunderstorm, hail', '⛈️'],
  99: ['Thunderstorm, hail', '⛈️'],
};

export function describeWeather(code) {
  const [label, icon] = CODES[code] || ['Unknown', '🌡️'];
  return { label, icon };
}

export function dayName(isoDate, index) {
  if (index === 0) return 'Today';
  return new Date(`${isoDate}T12:00:00`).toLocaleDateString('en-US', { weekday: 'short' });
}
