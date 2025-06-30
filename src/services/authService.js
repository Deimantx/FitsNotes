import auth from '@react-native-firebase/auth';
import firestore from '@react-native-firebase/firestore';

/**
 * Signs up a new user with email and password.
 * Creates a user document in Firestore.
 * @param {string} email - User's email.
 * @param {string} password - User's password.
 * @returns {Promise<FirebaseAuthTypes.UserCredential>}
 */
const signUp = async (email, password) => {
  try {
    const userCredential = await auth().createUserWithEmailAndPassword(email, password);
    if (userCredential.user) {
      // Create a user document in Firestore
      await firestore().collection('users').doc(userCredential.user.uid).set({
        email: userCredential.user.email,
        uid: userCredential.user.uid,
        createdAt: firestore.FieldValue.serverTimestamp(),
        // Add any other default fields you want for a new user
      });
      console.log('User account created & signed in! Firestore document created.');
    }
    return userCredential;
  } catch (error) {
    if (error.code === 'auth/email-already-in-use') {
      throw new Error('That email address is already in use!');
    }
    if (error.code === 'auth/invalid-email') {
      throw new Error('That email address is invalid!');
    }
    console.error("Error signing up: ", error);
    throw error;
  }
};

/**
 * Signs in an existing user with email and password.
 * @param {string} email - User's email.
 * @param {string} password - User's password.
 * @returns {Promise<FirebaseAuthTypes.UserCredential>}
 */
const signIn = async (email, password) => {
  try {
    const userCredential = await auth().signInWithEmailAndPassword(email, password);
    console.log('User signed in!');
    return userCredential;
  } catch (error) {
    if (error.code === 'auth/user-not-found' || error.code === 'auth/wrong-password') {
      throw new Error('Invalid email or password.');
    }
    if (error.code === 'auth/invalid-email') {
      throw new Error('That email address is invalid!');
    }
    console.error("Error signing in: ", error);
    throw error;
  }
};

/**
 * Signs out the current user.
 * @returns {Promise<void>}
 */
const signOut = async () => {
  try {
    await auth().signOut();
    console.log('User signed out!');
  } catch (error) {
    console.error("Error signing out: ", error);
    throw error;
  }
};

/**
 * Subscribes to authentication state changes.
 * @param {function} callback - Function to call with the user object (or null) when auth state changes.
 * @returns {function} - Unsubscribe function.
 */
const onAuthStateChanged = (callback) => {
  return auth().onAuthStateChanged(callback);
};

/**
 * Gets the currently signed-in user.
 * @returns {FirebaseAuthTypes.User | null} - The current user object or null.
 */
const getCurrentUser = () => {
  return auth().currentUser;
};

export default {
  signUp,
  signIn,
  signOut,
  onAuthStateChanged,
  getCurrentUser,
};
