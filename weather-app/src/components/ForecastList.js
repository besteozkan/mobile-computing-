// Card listing the 5-day forecast, one row per day.
import { StyleSheet, Text, View } from 'react-native';

import { dayName, describeWeather } from '../weatherCodes';

function ForecastRow({ day, index }) {
  return (
    <View style={styles.row}>
      <Text style={styles.day}>{dayName(day.date, index)}</Text>
      <Text style={styles.icon}>{describeWeather(day.code).icon}</Text>
      <Text style={styles.temp}>
        {day.min}° / {day.max}°
      </Text>
    </View>
  );
}

export default function ForecastList({ days }) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>5-day forecast</Text>
      {days.map((day, i) => (
        <ForecastRow key={day.date} day={day} index={i} />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 20, marginTop: 16 },
  title: { fontSize: 17, fontWeight: '600', marginBottom: 8, color: '#1d3557' },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  day: { flex: 1, fontSize: 16 },
  icon: { fontSize: 22, width: 50, textAlign: 'center' },
  temp: { width: 90, textAlign: 'right', fontSize: 16 },
});
