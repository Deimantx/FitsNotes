// TODO: Replace with your actual Firebase project configuration snippet
export const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_AUTH_DOMAIN",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_STORAGE_BUCKET",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID",
  measurementId: "YOUR_MEASUREMENT_ID" // Optional, for Google Analytics
};

// It's also good practice to check if Firebase has already been initialized
// though with React Native Firebase, explicit initialization in JS is often less direct
// as it's handled natively during the build process after setup.
// However, having the config centralized is good.

/*
How to get this configuration:
1. Go to the Firebase console (console.firebase.google.com).
2. If you haven't already, create a new Firebase project.
3. In your project dashboard, click on "Project settings" (the gear icon).
4. Under the "General" tab, scroll down to "Your apps".
5. If you haven't added an app yet, click on the web icon (</>) to "Add app" (even for React Native, the web app config is what you often use for the JS SDK parts, or Firebase will provide specific guidance for iOS/Android native setup which @react-native-firebase handles).
6. Firebase will provide you with a configuration object similar to the one above. Copy these values into this file.
7. For React Native Firebase, you also need to add platform-specific configuration files (google-services.json for Android and GoogleService-Info.plist for iOS) to your native project folders. The `@react-native-firebase/app` package documentation provides detailed instructions for this.
*/
