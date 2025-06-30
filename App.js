import React from 'react';
import AppNavigator from './src/navigation/AppNavigator';
import { StatusBar } from 'react-native';

// It's important to ensure native Firebase is initialized.
// For @react-native-firebase, this primarily involves having the
// google-services.json (Android) and GoogleService-Info.plist (iOS)
// correctly placed and linked in the native projects.
// The JavaScript import of @react-native-firebase/app is usually enough
// to get things running if the native setup is done.

// Example: Check if Firebase app is initialized (optional, good for debugging)
// import firebase from '@react-native-firebase/app';
// if (!firebase.apps.length) {
//   console.warn("Firebase app not initialized at App.js entry. Check native setup!");
// } else {
//   console.log("Firebase app seems initialized at App.js entry.");
// }


const App = () => {
  return (
    <>
      <StatusBar barStyle="default" />
      {/*
        The AppNavigator now handles everything related to auth state and
        displaying the correct screens (Auth or Main App).
      */}
      <AppNavigator />
    </>
  );
};

export default App;
