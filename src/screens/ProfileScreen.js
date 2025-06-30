import React from 'react';
import { View, Text, Button, StyleSheet, Alert } from 'react-native';
import authService from '../services/authService'; // We'll use this for sign out

const ProfileScreen = ({ navigation }) => { // navigation prop is passed by React Navigation

  const handleSignOut = async () => {
    try {
      await authService.signOut();
      // The root navigator should automatically switch to the Auth stack
      // as it listens to auth state changes.
      Alert.alert("Signed Out", "You have been signed out.");
    } catch (error) {
      Alert.alert("Error", "Failed to sign out. Please try again.");
      console.error('Failed to sign out', error);
    }
  };

  const currentUser = authService.getCurrentUser();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Profile</Text>
      {currentUser ? (
        <Text style={styles.emailText}>Email: {currentUser.email}</Text>
      ) : (
        <Text style={styles.emailText}>Not logged in</Text>
      )}
      <View style={styles.buttonContainer}>
        <Button
          title="Go Home"
          onPress={() => navigation.navigate('Home')} // Assumes 'Home' is a route name
        />
        <Button
            title="Sign Out"
            onPress={handleSignOut}
            color="#841584"
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  emailText: {
    fontSize: 16,
    marginBottom: 30,
  },
  buttonContainer: {
    marginTop: 20,
    width: '80%',
  },
});

export default ProfileScreen;
