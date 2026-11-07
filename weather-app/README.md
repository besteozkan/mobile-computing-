# SkyCheck – Weather App (React + React Native)

Mobile Computing course project. Shows current weather and a 5-day forecast
for your GPS location or any city you search.

## Features
- Weather for current location (GPS + runtime permission)
- City search (geocoding)
- Current temperature, feels-like, humidity, wind + 5-day forecast
- Offline mode: last result is cached on the device
- Pull-to-refresh

## Run
1. Install Node.js LTS: https://nodejs.org
2. In this folder:
   ```
   npm install
   npx expo install --fix
   npx expo start
   ```
3. Install **Expo Go** on your phone and scan the QR code
   (or press `a` for an Android emulator).

## Structure (React components + custom hook)
```
App.js                              Root React component (composes the screen)
src/hooks/useWeather.js             Custom React hook: state, location, network, cache
src/components/SearchBar.js         City search input + results
src/components/CurrentWeatherCard.js  Current weather card
src/components/ForecastList.js      5-day forecast list
src/components/StatusMessages.js    Error text + offline banner
src/api.js                          Network requests (Open-Meteo, Nominatim)
src/storage.js                      Local cache (AsyncStorage)
src/weatherCodes.js                 Weather code -> label/icon
```
