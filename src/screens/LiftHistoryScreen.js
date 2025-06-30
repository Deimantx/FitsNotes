import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, StyleSheet, ActivityIndicator, Button, Alert, TouchableOpacity } from 'react-native';
import liftService from '../services/liftService';
import { useFocusEffect } from '@react-navigation/native'; // To refresh data when screen is focused

const LiftHistoryScreen = ({ navigation }) => {
  const [lifts, setLifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedExercise, setSelectedExercise] = useState(null); // To filter by exercise

  const predefinedExercises = liftService.getPredefinedExercises();

  const fetchLifts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const userLifts = await liftService.getLiftsForUser(selectedExercise);
      setLifts(userLifts);
    } catch (err) {
      setError(err.message);
      Alert.alert("Error", "Could not fetch lift history.");
    } finally {
      setLoading(false);
    }
  }, [selectedExercise]); // Dependency: re-fetch if selectedExercise changes

  // Fetch lifts when the screen comes into focus or selectedExercise changes
  useFocusEffect(
    useCallback(() => {
      fetchLifts();
    }, [fetchLifts])
  );

  const handleDeleteLift = async (liftId) => {
    Alert.alert(
      "Confirm Delete",
      "Are you sure you want to delete this lift entry?",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: async () => {
            try {
              await liftService.deleteLift(liftId);
              Alert.alert("Success", "Lift deleted.");
              fetchLifts(); // Refresh the list
            } catch (err) {
              Alert.alert("Error", `Failed to delete lift: ${err.message}`);
            }
          },
        },
      ]
    );
  };

  const renderItem = ({ item }) => (
    <View style={styles.itemContainer}>
      <Text style={styles.itemTitle}>{item.exerciseName} - {new Date(item.date.toDate()).toLocaleDateString()}</Text>
      <Text>Weight: {item.weight} | Reps: {item.reps} | Sets: {item.sets}</Text>
      {item.notes ? <Text>Notes: {item.notes}</Text> : null}
      {item.isPr ? <Text style={styles.prText}>🎉 Personal Record!</Text> : null}
      <View style={styles.actionsContainer}>
        {/* <Button title="Edit" onPress={() => navigation.navigate('EditLiftScreen', { liftId: item.id })} /> */}
        <TouchableOpacity onPress={() => handleDeleteLift(item.id)} style={styles.deleteButton}>
            <Text style={styles.deleteButtonText}>Delete</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  if (loading) {
    return <ActivityIndicator size="large" style={styles.centered} />;
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text>Error fetching lifts: {error}</Text>
        <Button title="Retry" onPress={fetchLifts} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Lift History</Text>
      {/* Basic Filter (Can be improved with a Picker) */}
      <View style={styles.filterContainer}>
        <Button title="All Exercises" onPress={() => setSelectedExercise(null)} disabled={!selectedExercise} />
        {predefinedExercises.slice(0, 3).map(ex => ( // Show first 3 as example
             <Button key={ex.id} title={ex.name} onPress={() => setSelectedExercise(ex.name)} disabled={selectedExercise === ex.name}/>
        ))}
      </View>

      {lifts.length === 0 ? (
        <Text style={styles.centered}>No lifts recorded yet for {selectedExercise || 'any exercise'}.</Text>
      ) : (
        <FlatList
          data={lifts}
          renderItem={renderItem}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.list}
        />
      )}
       <Button title="Add New Lift" onPress={() => navigation.navigate('AddLift')} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  list: {
    paddingBottom: 20,
  },
  itemContainer: {
    backgroundColor: 'white',
    padding: 15,
    marginVertical: 8,
    borderRadius: 5,
    elevation: 2, // for Android shadow
    shadowColor: '#000', // for iOS shadow
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.41,
  },
  itemTitle: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  prText: {
    color: 'green',
    fontWeight: 'bold',
    marginTop: 5,
  },
  actionsContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 10,
  },
  deleteButton: {
    marginLeft:10,
    backgroundColor: '#ff4d4d',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 3,
  },
  deleteButtonText:{
      color: 'white',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    textAlign: 'center',
  },
  filterContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 10,
  }
});

export default LiftHistoryScreen;
