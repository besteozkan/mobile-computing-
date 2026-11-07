// Card showing the current weather for the selected place.
import { StyleSheet, Text, View } from 'react-native';

import { describeWeather } from '../weatherCodes';

function Detail({ title, value }) {
  return (
    <View style={styles.detail}>
      <Text style={styles.detailValue}>{value}</Text>
      <Text style={styles.detailTitle}>{title}</Text>
    </View>
  );
}

export default function CurrentWeatherCard({ placeName, current }) {
  const { label, icon } = describeWeather(current.code);

  return (
    <View style={styles.card}>
      <Text style={styles.place}>{placeName}</Text>
      <Text style={styles.icon}>{icon}</Text>
      <Text style={styles.temp}>{current.temperature}°C</Text>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.detailsRow}>
        <Detail title="Feels like" value={`${current.feelsLike}°`} />
        <Detail title="Humidity" value={`${current.humidity}%`} />
        <Detail title="Wind" value={`${current.wind} km/h`} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 20, marginTop: 16 },
  place: { fontSize: 20, fontWeight: '600', textAlign: 'center', color: '#1d3557' },
  icon: { fontSize: 64, textAlign: 'center', marginVertical: 8 },
  temp: { fontSize: 48, fontWeight: '700', textAlign: 'center' },
  label: { fontSize: 18, textAlign: 'center', color: '#555' },
  detailsRow: { flexDirection: 'row', justifyContent: 'space-around', marginTop: 16 },
  detail: { alignItems: 'center' },
  detailValue: { fontSize: 18, fontWeight: '600' },
  detailTitle: { fontSize: 13, color: '#777' },
});
