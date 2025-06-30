import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, StyleSheet, ActivityIndicator, Button, Alert, TouchableOpacity } from 'react-native';
import templateService from '../services/templateService';
import { useFocusEffect } from '@react-navigation/native'; // To refresh data

const ViewTemplatesScreen = ({ navigation }) => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTemplates = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const userTemplates = await templateService.getUserTemplates();
      setTemplates(userTemplates);
    } catch (err) {
      setError(err.message);
      Alert.alert("Error", "Could not fetch workout templates.");
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      fetchTemplates();
    }, [fetchTemplates])
  );

  const handleDeleteTemplate = async (templateId) => {
    Alert.alert(
      "Confirm Delete",
      "Are you sure you want to delete this workout template? This action cannot be undone.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: async () => {
            try {
              await templateService.deleteTemplate(templateId);
              Alert.alert("Success", "Template deleted.");
              fetchTemplates(); // Refresh list
            } catch (err) {
              Alert.alert("Error", `Failed to delete template: ${err.message}`);
            }
          },
        },
      ]
    );
  };

  const renderItem = ({ item }) => (
    <TouchableOpacity
        style={styles.itemContainer}
        onPress={() => navigation.navigate('TemplateDetail', { templateId: item.id, templateName: item.name })}
        // Long press to delete, or add a button
        onLongPress={() => handleDeleteTemplate(item.id)}
    >
      <Text style={styles.itemTitle}>{item.name}</Text>
      <Text style={styles.itemDescription}>{item.description || 'No description'}</Text>
      <Text style={styles.itemExercisesInfo}>{item.exercises?.length || 0} exercises</Text>
      {/* Simple delete button for now */}
      <TouchableOpacity onPress={() => handleDeleteTemplate(item.id)} style={styles.deleteButton}>
          <Text style={styles.deleteButtonText}>Delete</Text>
      </TouchableOpacity>
    </TouchableOpacity>
  );

  if (loading) {
    return <ActivityIndicator size="large" style={styles.centered} />;
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text>Error fetching templates: {error}</Text>
        <Button title="Retry" onPress={fetchTemplates} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Your Workout Templates</Text>
      {templates.length === 0 ? (
        <View style={styles.centered}>
            <Text>No workout templates created yet.</Text>
        </View>
      ) : (
        <FlatList
          data={templates}
          renderItem={renderItem}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.list}
        />
      )}
      <Button
        title="Create New Template"
        onPress={() => navigation.navigate('CreateTemplate')}
      />
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
    marginBottom: 20,
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
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.41,
  },
  itemTitle: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  itemDescription: {
    fontSize: 14,
    color: '#555',
    marginTop: 4,
  },
  itemExercisesInfo: {
    fontSize: 12,
    color: '#777',
    marginTop: 8,
  },
  deleteButton: {
    alignSelf: 'flex-end',
    marginTop: 5,
    backgroundColor: '#ff4d4d',
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 4,
  },
  deleteButtonText:{
      color: 'white',
      fontSize: 12,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
});

export default ViewTemplatesScreen;
