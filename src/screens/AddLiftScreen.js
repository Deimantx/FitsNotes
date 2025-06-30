import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, ScrollView, Picker } from 'react-native';
import liftService from '../services/liftService'; // Assuming this service exists
// For a real date picker, you'd use a library like @react-native-community/datetimepicker
// For now, we'll use a simple text input for the date.

const AddLiftScreen = ({ navigation }) => {
  const [exerciseName, setExerciseName] = useState('');
  const [weight, setWeight] = useState('');
  const [reps, setReps] = useState('');
  const [sets, setSets] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]); // Defaults to today, YYYY-MM-DD
  const [notes, setNotes] = useState('');
  const [isPr, setIsPr] = useState(false); // For personal record

  const predefinedExercises = liftService.getPredefinedExercises();
  // Initialize with the first exercise or leave empty
  useState(() => {
    if (predefinedExercises.length > 0) {
      setExerciseName(predefinedExercises[0].name);
    }
  });


  const handleAddLift = async () => {
    if (!exerciseName || !weight || !reps || !sets || !date) {
      Alert.alert('Error', 'Please fill in all required fields (Exercise, Weight, Reps, Sets, Date).');
      return;
    }

    const liftData = {
      exerciseName,
      weight: parseFloat(weight),
      reps: parseInt(reps, 10),
      sets: parseInt(sets, 10),
      date: new Date(date), // Convert string date to Date object
      notes,
      isPr,
    };

    try {
      await liftService.addLift(liftData);
      Alert.alert('Success', 'Lift added successfully!');
      // Clear form or navigate away
      setWeight('');
      setReps('');
      setSets('');
      setNotes('');
      setIsPr(false);
      // navigation.goBack(); // Or navigate to a history screen
    } catch (error) {
      Alert.alert('Error', `Failed to add lift: ${error.message}`);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Add New Lift</Text>

      <Text style={styles.label}>Exercise:</Text>
      {predefinedExercises.length > 0 ? (
        <Picker
          selectedValue={exerciseName}
          style={styles.input}
          onValueChange={(itemValue) => setExerciseName(itemValue)}
        >
          {predefinedExercises.map(exercise => (
            <Picker.Item key={exercise.id} label={exercise.name} value={exercise.name} />
          ))}
        </Picker>
      ) : (
        <TextInput
          style={styles.input}
          placeholder="e.g., Bench Press"
          value={exerciseName}
          onChangeText={setExerciseName}
        />
      )}


      <Text style={styles.label}>Weight (kg/lbs):</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g., 100"
        value={weight}
        onChangeText={setWeight}
        keyboardType="numeric"
      />

      <Text style={styles.label}>Reps:</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g., 5"
        value={reps}
        onChangeText={setReps}
        keyboardType="numeric"
      />

      <Text style={styles.label}>Sets:</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g., 3"
        value={sets}
        onChangeText={setSets}
        keyboardType="numeric"
      />

      <Text style={styles.label}>Date:</Text>
      <TextInput
        style={styles.input}
        placeholder="YYYY-MM-DD"
        value={date}
        onChangeText={setDate}
      />
      {/* In a real app, use a DateTimePicker component here */}

      <Text style={styles.label}>Notes (Optional):</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="e.g., Felt good, focus on form"
        value={notes}
        onChangeText={setNotes}
        multiline
      />

      <View style={styles.prContainer}>
        <Text>Personal Record?</Text>
        {/* Basic switch placeholder, use React Native Switch for real */}
        <Button title={isPr ? "Yes" : "No"} onPress={() => setIsPr(!isPr)} />
      </View>

      <Button title="Add Lift" onPress={handleAddLift} />
      <Button title="Go Back" onPress={() => navigation.goBack()} />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 20,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  label: {
    fontSize: 16,
    marginBottom: 5,
    marginTop: 10,
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    marginBottom: 12,
    paddingHorizontal: 10,
    borderRadius: 5,
    backgroundColor: 'white',
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top', // For Android
  },
  prContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 15,
  }
});

export default AddLiftScreen;
