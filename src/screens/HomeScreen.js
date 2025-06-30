import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

const HomeScreen = ({ navigation }) => {

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Fitness App Home</Text>
      <Text style={styles.subtitle}>Track your progress and build routines.</Text>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Lift Tracking</Text>
        <View style={styles.buttonWrapper}>
          <Button
            title="Add New Lift"
            onPress={() => navigation.navigate('AddLift')}
            color="#17a2b8"
          />
        </View>
        <View style={styles.buttonWrapper}>
          <Button
            title="View Lift History"
            onPress={() => navigation.navigate('LiftHistory')}
            color="#17a2b8"
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Workout Templates</Text>
        <View style={styles.buttonWrapper}>
          <Button
            title="Create Workout Template"
            onPress={() => navigation.navigate('CreateTemplate')}
            color="#28a745"
          />
        </View>
        <View style={styles.buttonWrapper}>
          <Button
            title="View My Templates"
            onPress={() => navigation.navigate('ViewTemplates')}
            color="#28a745"
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Account</Text>
        <View style={styles.buttonWrapper}>
          <Button
            title="My Profile & Settings"
            onPress={() => navigation.navigate('Profile')}
            color="#6c757d"
          />
        </View>
      </View>

    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    // justifyContent: 'center', // More top-aligned now
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#f4f4f8',
  },
  title: {
    fontSize: 26,
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#333',
  },
  subtitle: {
    fontSize: 16,
    color: '#555',
    marginBottom: 25,
    textAlign: 'center',
  },
  section: {
    width: '90%',
    marginBottom: 25,
    padding: 15,
    backgroundColor: 'white',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600', // Semibold
    marginBottom: 12,
    color: '#0056b3', // A primary color
  },
  buttonWrapper: {
    marginVertical: 6,
  }
});

export default HomeScreen;
