import React, { useState, useEffect, useContext } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import auth from '@react-native-firebase/auth'; // Firebase Auth

// Screens
import SignInScreen from '../screens/SignInScreen';
import SignUpScreen from '../screens/SignUpScreen';
import HomeScreen from '../screens/HomeScreen';
import ProfileScreen from '../screens/ProfileScreen';
import AuthLoadingScreen from '../screens/AuthLoadingScreen';
import AddLiftScreen from '../screens/AddLiftScreen';
import LiftHistoryScreen from '../screens/LiftHistoryScreen';
import CreateTemplateScreen from '../screens/CreateTemplateScreen'; // New
import ViewTemplatesScreen from '../screens/ViewTemplatesScreen';   // New
import TemplateDetailScreen from '../screens/TemplateDetailScreen'; // New


// Create a context to pass user state down if needed, or use a global state manager
export const AuthContext = React.createContext(null);

const Stack = createStackNavigator();

// Stack for authentication-related screens
const AuthStack = () => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    <Stack.Screen name="SignIn" component={SignInScreen} />
    <Stack.Screen name="SignUp" component={SignUpScreen} />
  </Stack.Navigator>
);

// Stack for main application screens after login
const AppStack = () => (
  <Stack.Navigator>
    <Stack.Screen name="Home" component={HomeScreen} options={{ title: 'Fitness Home' }} />
    <Stack.Screen name="Profile" component={ProfileScreen} />
    <Stack.Screen name="AddLift" component={AddLiftScreen} options={{ title: 'Add New Lift' }} />
    <Stack.Screen name="LiftHistory" component={LiftHistoryScreen} options={{ title: 'Lift History' }} />
    <Stack.Screen name="CreateTemplate" component={CreateTemplateScreen} options={{ title: 'Create Workout Template' }} />
    <Stack.Screen name="ViewTemplates" component={ViewTemplatesScreen} options={{ title: 'My Workout Templates' }} />
    <Stack.Screen name="TemplateDetail" component={TemplateDetailScreen} options={({ route }) => ({ title: route.params?.templateName || 'Template Details' })}/>
  </Stack.Navigator>
);

const AppNavigator = () => {
  const [initializing, setInitializing] = useState(true);
  const [user, setUser] = useState(null);

  // Handle user state changes
  function onAuthStateChanged(userAuth) {
    setUser(userAuth);
    if (initializing) {
      setInitializing(false);
    }
  }

  useEffect(() => {
    const subscriber = auth().onAuthStateChanged(onAuthStateChanged);
    return subscriber; // unsubscribe on unmount
  }, []);

  if (initializing) {
    return <AuthLoadingScreen />;
  }

  return (
    <AuthContext.Provider value={user}>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {user ? (
            <Stack.Screen name="AppStack" component={AppStack} />
          ) : (
            <Stack.Screen name="AuthStack" component={AuthStack} />
          )}
        </Stack.Navigator>
      </NavigationContainer>
    </AuthContext.Provider>
  );
};

export default AppNavigator;
