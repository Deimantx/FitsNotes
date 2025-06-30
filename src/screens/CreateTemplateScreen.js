import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, FlatList, TouchableOpacity, ScrollView, Picker } from 'react-native';
import templateService from '../services/templateService';
import liftService from '../services/liftService'; // To get predefined exercises for selection
import { v4 as uuidv4 } from 'uuid'; // For temporary client-side IDs for list items

// Make sure 'react-native-get-random-values' is imported in your root App.js or index.js
// import 'react-native-get-random-values';


const CreateTemplateScreen = ({ navigation }) => {
  const [templateName, setTemplateName] = useState('');
  const [description, setDescription] = useState('');
  const [exercises, setExercises] = useState([]); // Array of { id, exerciseName, targetSets, targetReps }

  // For the "Add Exercise" form part
  const [currentExerciseName, setCurrentExerciseName] = useState('');
  const [currentSets, setCurrentSets] = useState('');
  const [currentReps, setCurrentReps] = useState('');

  const predefinedExercises = liftService.getPredefinedExercises();

  useState(() => {
    if (predefinedExercises.length > 0) {
        setCurrentExerciseName(predefinedExercises[0].name);
    }
  }, []);


  const handleAddExercise = () => {
    if (!currentExerciseName || !currentSets || !currentReps) {
      Alert.alert('Error', 'Please fill in exercise name, sets, and reps.');
      return;
    }
    setExercises([
      ...exercises,
      {
        id: uuidv4(), // Temporary client-side ID for list key
        exerciseName: currentExerciseName,
        targetSets: parseInt(currentSets, 10),
        targetReps: parseInt(currentReps, 10),
      },
    ]);
    // Clear input fields for next exercise
    if (predefinedExercises.length > 0) {
        setCurrentExerciseName(predefinedExercises[0].name);
    } else {
        setCurrentExerciseName('');
    }
    setCurrentSets('');
    setCurrentReps('');
  };

  const handleRemoveExercise = (id) => {
    setExercises(exercises.filter(ex => ex.id !== id));
  };

  const handleCreateTemplate = async () => {
    if (!templateName.trim()) {
      Alert.alert('Error', 'Please enter a template name.');
      return;
    }
    if (exercises.length === 0) {
      Alert.alert('Error', 'Please add at least one exercise to the template.');
      return;
    }

    const templateData = {
      name: templateName,
      description,
      exercises: exercises.map(({ exerciseName, targetSets, targetReps }) => ({ // Remove temporary id
        exerciseName,
        targetSets,
        targetReps,
      })),
    };

    try {
      await templateService.createTemplate(templateData);
      Alert.alert('Success', 'Workout template created successfully!');
      setTemplateName('');
      setDescription('');
      setExercises([]);
      navigation.goBack(); // Or navigate to ViewTemplatesScreen
    } catch (error) {
      Alert.alert('Error', `Failed to create template: ${error.message}`);
    }
  };

  const renderExerciseItem = ({ item }) => (
    <View style={styles.exerciseItem}>
      <Text style={styles.exerciseText}>{item.exerciseName} - Sets: {item.targetSets}, Reps: {item.targetReps}</Text>
      <TouchableOpacity onPress={() => handleRemoveExercise(item.id)} style={styles.removeButton}>
        <Text style={styles.removeButtonText}>Remove</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <ScrollView style={styles.scrollContainer}>
      <View style={styles.container}>
        <Text style={styles.title}>Create New Workout Template</Text>

        <Text style={styles.label}>Template Name:</Text>
        <TextInput
          style={styles.input}
          placeholder="e.g., Push Day A"
          value={templateName}
          onChangeText={setTemplateName}
        />

        <Text style={styles.label}>Description (Optional):</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="e.g., Focus on chest and triceps"
          value={description}
          onChangeText={setDescription}
          multiline
        />

        <View style={styles.addExerciseSection}>
          <Text style={styles.subtitle}>Add Exercises</Text>

          <Text style={styles.label}>Exercise Name:</Text>
          {predefinedExercises.length > 0 ? (
            <Picker
              selectedValue={currentExerciseName}
              style={styles.input}
              onValueChange={(itemValue) => setCurrentExerciseName(itemValue)}
            >
              {predefinedExercises.map(exercise => (
                <Picker.Item key={exercise.id} label={exercise.name} value={exercise.name} />
              ))}
            </Picker>
          ) : (
            <TextInput
              style={styles.input}
              placeholder="e.g., Bench Press"
              value={currentExerciseName}
              onChangeText={setCurrentExerciseName}
            />
          )}

          <Text style={styles.label}>Target Sets:</Text>
          <TextInput
            style={styles.input}
            placeholder="e.g., 3"
            value={currentSets}
            onChangeText={setCurrentSets}
            keyboardType="numeric"
          />

          <Text style={styles.label}>Target Reps:</Text>
          <TextInput
            style={styles.input}
            placeholder="e.g., 8-12"
            value={currentReps}
            onChangeText={setCurrentReps}
            keyboardType="numeric" // Consider allowing range strings later
          />
          <Button title="Add Exercise to Template" onPress={handleAddExercise} />
        </View>

        {exercises.length > 0 && (
          <View style={styles.exerciseListContainer}>
            <Text style={styles.subtitle}>Current Exercises in Template:</Text>
            <FlatList
              data={exercises}
              renderItem={renderExerciseItem}
              keyExtractor={item => item.id}
            />
          </View>
        )}

        <View style={styles.footerButtons}>
            <Button title="Create Template" onPress={handleCreateTemplate} color="#28a745" />
            <View style={{marginTop: 10}}>
                <Button title="Cancel" onPress={() => navigation.goBack()} color="#6c757d" />
            </View>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  scrollContainer: {
    flex: 1,
  },
  container: {
    padding: 20,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 15,
    marginBottom: 10,
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
    textAlignVertical: 'top',
  },
  addExerciseSection: {
    marginVertical: 20,
    padding: 15,
    borderColor: '#e0e0e0',
    borderWidth: 1,
    borderRadius: 5,
  },
  exerciseListContainer: {
    marginTop: 10,
    marginBottom: 20,
    minHeight: 50, // ensure it's visible even if empty for a bit
  },
  exerciseItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 5,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  exerciseText: {
    flex: 1,
  },
  removeButton: {
    padding: 8,
    backgroundColor: '#ff4d4d',
    borderRadius: 4,
  },
  removeButtonText: {
    color: 'white',
    fontSize: 12,
  },
  footerButtons: {
      marginTop: 20,
      marginBottom: 40, // For scroll room
  }
});

export default CreateTemplateScreen;
