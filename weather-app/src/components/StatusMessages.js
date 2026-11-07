// Error text and the yellow "Offline" banner.
import { StyleSheet, Text } from 'react-native';

export default function StatusMessages({ error, offlineSince }) {
  return (
    <>
      {error && <Text style={styles.error}>{error}</Text>}
      {offlineSince && (
        <Text style={styles.offline}>
          Offline – showing saved data from {new Date(offlineSince).toLocaleString()}
        </Text>
      )}
    </>
  );
}

const styles = StyleSheet.create({
  error: { color: '#b00020', marginTop: 10 },
  offline: { color: '#8a6d00', backgroundColor: '#fff4cc', padding: 8, borderRadius: 8, marginTop: 10 },
});
