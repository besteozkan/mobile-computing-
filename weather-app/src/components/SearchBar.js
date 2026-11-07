// City search: text input, Search button and the list of matching cities.
import { useState } from 'react';
import { Keyboard, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { searchCity } from '../api';

export default function SearchBar({ onSelect, onError }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const onSearch = async () => {
    const text = query.trim();
    if (!text) return;
    Keyboard.dismiss();
    onError(null);
    try {
      const found = await searchCity(text);
      setResults(found);
      if (found.length === 0) onError(`No city found for "${text}".`);
    } catch (e) {
      onError(`Search failed: ${e.message}`);
    }
  };

  const pick = (city) => {
    setResults([]);
    setQuery('');
    onSelect({
      name: `${city.name}, ${city.country}`,
      latitude: city.latitude,
      longitude: city.longitude,
    });
  };

  return (
    <View>
      <View style={styles.row}>
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
        <Pressable key={`${city.latitude}-${i}`} style={styles.result} onPress={() => pick(city)}>
          <Text>
            {city.name}, {city.country}
          </Text>
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', gap: 8 },
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
});
