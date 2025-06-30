import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, StyleSheet, ActivityIndicator, Button, Alert, TextInput, TouchableOpacity, Picker } from 'react-native';
import templateService from '../services/templateService';
import liftService from '../services/liftService'; // For predefined exercises
import { useFocusEffect, useRoute } from '@react-navigation/native';
import { v4 as uuidv4 } from 'uuid';


const TemplateDetailScreen = ({ navigation }) => {
  const route = useRoute();
  const { templateId, templateName } = route.params;

  const [template, setTemplate] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // For editing template name/description
  const [editingDetails, setEditingDetails] = useState(false);
  const [newName, setNewName] = useState(templateName);
  const [newDescription, setNewDescription] = useState('');

  // For adding/editing exercises within the template
  const [isAddingExercise, setIsAddingExercise] = useState(false);
  const [editingExercise, setEditingExercise] = useState(null); // { id, exerciseName, targetSets, targetReps }
  const [currentExerciseName, setCurrentExerciseName] = useState('');
  const [currentSets, setCurrentSets] = useState('');
  const [currentReps, setCurrentReps] = useState('');

  const predefinedExercises = liftService.getPredefinedExercises();


  const fetchTemplateDetails = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const details = await templateService.getTemplateById(templateId);
      setTemplate(details);
      if (details) {
        setNewName(details.name);
        setNewDescription(details.description || '');
        // Set initial exercise name for picker if adding/editing
        if (predefinedExercises.length > 0 && !currentExerciseName) {
            setCurrentExerciseName(predefinedExercises[0].name);
        }
      }
    } catch (err) {
      setError(err.message);
      Alert.alert("Error", "Could not fetch template details.");
    } finally {
      setLoading(false);
    }
  }, [templateId, predefinedExercises, currentExerciseName]);

  useFocusEffect(fetchTemplateDetails); // Refetch when screen is focused

  useEffect(() => {
    navigation.setOptions({ title: template ? template.name : 'Template Details' });
  }, [navigation, template]);

  const handleUpdateDetails = async () => {
    if(!newName.trim()) {
        Alert.alert("Error", "Template name cannot be empty.");
        return;
    }
    try {
      await templateService.updateTemplateDetails(templateId, { name: newName, description: newDescription });
      Alert.alert("Success", "Template details updated.");
      setEditingDetails(false);
      fetchTemplateDetails(); // Refresh
    } catch (err) {
      Alert.alert("Error", `Failed to update details: ${err.message}`);
    }
  };

  const handleSaveExercise = async () => {
    if (!currentExerciseName || !currentSets || !currentReps) {
      Alert.alert("Error", "Exercise name, sets, and reps are required.");
      return;
    }
    const exerciseData = {
      exerciseName: currentExerciseName,
      targetSets: parseInt(currentSets, 10),
      targetReps: parseInt(currentReps, 10),
    };

    try {
      if (editingExercise) { // Updating existing exercise
        await templateService.updateExerciseInTemplate(templateId, editingExercise.id, exerciseData);
        Alert.alert("Success", "Exercise updated in template.");
      } else { // Adding new exercise
        await templateService.addExerciseToTemplate(templateId, exerciseData);
        Alert.alert("Success", "Exercise added to template.");
      }
      setEditingExercise(null);
      setIsAddingExercise(false);
      fetchTemplateDetails(); // Refresh
      // Clear form
      if (predefinedExercises.length > 0) setCurrentExerciseName(predefinedExercises[0].name); else setCurrentExerciseName('');
      setCurrentSets('');
      setCurrentReps('');

    } catch (err) {
      Alert.alert("Error", `Failed to save exercise: ${err.message}`);
    }
  };

  const openEditExerciseForm = (exercise) => {
    setEditingExercise(exercise);
    setCurrentExerciseName(exercise.exerciseName);
    setCurrentSets(String(exercise.targetSets));
    setCurrentReps(String(exercise.targetReps));
    setIsAddingExercise(false); // Ensure not in "add new" mode
  };

  const openAddExerciseForm = () => {
    setEditingExercise(null); // Clear any editing state
    if (predefinedExercises.length > 0) setCurrentExerciseName(predefinedExercises[0].name); else setCurrentExerciseName('');
    setCurrentSets('');
    setCurrentReps('');
    setIsAddingExercise(true);
  };


  const handleRemoveExercise = async (exerciseEntryId) => {
     Alert.alert("Confirm", "Remove this exercise from the template?", [
      { text: "Cancel" },
      { text: "OK", onPress: async () => {
          try {
            await templateService.removeExerciseFromTemplate(templateId, exerciseEntryId);
            Alert.alert("Success", "Exercise removed.");
            fetchTemplateDetails(); // Refresh
          } catch (err) {
            Alert.alert("Error", `Failed to remove exercise: ${err.message}`);
          }
        }
      }
    ]);
  };


  const renderExerciseItem = ({ item }) => (
    <View style={styles.exerciseItem}>
      <View style={styles.exerciseInfo}>
        <Text style={styles.exerciseName}>{item.exerciseName}</Text>
        <Text>Sets: {item.targetSets}, Reps: {item.targetReps}</Text>
      </View>
      <View style={styles.exerciseActions}>
        <TouchableOpacity onPress={() => openEditExerciseForm(item)} style={[styles.actionButton, styles.editButton]}>
            <Text style={styles.actionButtonText}>Edit</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => handleRemoveExercise(item.id)} style={[styles.actionButton, styles.removeButton]}>
            <Text style={styles.actionButtonText}>Remove</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  if (loading) return <ActivityIndicator size="large" style={styles.centered} />;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text><Button title="Retry" onPress={fetchTemplateDetails} /></View>;
  if (!template) return <View style={styles.centered}><Text>Template not found.</Text></View>;

  return (
    <ScrollView style={styles.container}>
      {editingDetails ? (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Edit Template Details</Text>
          <TextInput style={styles.input} value={newName} onChangeText={setNewName} placeholder="Template Name" />
          <TextInput style={[styles.input, styles.textArea]} value={newDescription} onChangeText={setNewDescription} placeholder="Description" multiline />
          <Button title="Save Details" onPress={handleUpdateDetails} />
          <Button title="Cancel Edit" onPress={() => { setEditingDetails(false); setNewName(template.name); setNewDescription(template.description);}} />
        </View>
      ) : (
        <View style={styles.section}>
          <Text style={styles.templateName}>{template.name}</Text>
          <Text style={styles.templateDescription}>{template.description || 'No description.'}</Text>
          <Button title="Edit Name/Description" onPress={() => setEditingDetails(true)} />
        </View>
      )}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Exercises</Text>
        <FlatList
          data={template.exercises || []}
          renderItem={renderExerciseItem}
          keyExtractor={item => item.id || uuidv4()} // Fallback key
          ListEmptyComponent={<Text>No exercises in this template yet.</Text>}
        />
      </View>

      {(isAddingExercise || editingExercise) && (
        <View style={[styles.section, styles.addExerciseForm]}>
          <Text style={styles.sectionTitle}>{editingExercise ? 'Edit Exercise' : 'Add New Exercise'}</Text>
          <Picker
            selectedValue={currentExerciseName}
            style={styles.input}
            onValueChange={(itemValue) => setCurrentExerciseName(itemValue)}
          >
            {predefinedExercises.map(ex => <Picker.Item key={ex.id} label={ex.name} value={ex.name} />)}
          </Picker>
          <TextInput style={styles.input} value={currentSets} onChangeText={setCurrentSets} placeholder="Target Sets" keyboardType="numeric" />
          <TextInput style={styles.input} value={currentReps} onChangeText={setCurrentReps} placeholder="Target Reps" keyboardType="numeric" />
          <Button title={editingExercise ? "Update Exercise" : "Add Exercise"} onPress={handleSaveExercise} />
          <Button title="Cancel" onPress={() => { setIsAddingExercise(false); setEditingExercise(null); }} color="gray" />
        </View>
      )}

      {!isAddingExercise && !editingExercise && (
         <Button title="Add Exercise to Template" onPress={openAddExerciseForm} />
      )}
      <View style={{height: 50}} />{/* Spacer */}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 15 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  section: { marginBottom: 20, padding: 10, backgroundColor: '#fff', borderRadius: 5, shadowColor:"#000", shadowOffset:{width:0,height:1}, shadowOpacity:0.1, shadowRadius:2, elevation:2},
  sectionTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 10 },
  templateName: { fontSize: 24, fontWeight: 'bold', marginBottom: 5 },
  templateDescription: { fontSize: 16, color: 'gray', marginBottom: 10 },
  input: { height: 40, borderColor: 'gray', borderWidth: 1, marginBottom: 10, paddingHorizontal: 10, borderRadius: 3, backgroundColor:'white' },
  textArea: { height: 80, textAlignVertical: 'top' },
  exerciseItem: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#eee' },
  exerciseInfo: { flex: 3 },
  exerciseName: { fontSize: 16, fontWeight: '500' },
  exerciseActions: { flexDirection: 'row', flex: 1, justifyContent: 'flex-end'},
  actionButton: { paddingVertical: 5, paddingHorizontal:8, borderRadius:3, marginLeft:5},
  actionButtonText: {color:'white', fontSize:12},
  editButton: { backgroundColor: '#007bff'},
  removeButton: { backgroundColor: '#dc3545'},
  addExerciseForm: { borderColor: '#ccc', borderWidth: 1, padding: 15, borderRadius: 5, marginTop:10 },
});

export default TemplateDetailScreen;
