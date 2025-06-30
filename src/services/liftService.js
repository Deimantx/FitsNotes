import firestore from '@react-native-firebase/firestore';
import authService from './authService'; // To easily get current user's ID

/**
 * Adds a new lift record to Firestore.
 * @param {object} liftData - Object containing lift details.
 * Expected fields: exerciseName (string), weight (number), reps (number), sets (number),
 * date (Timestamp or Date object), notes (string, optional), isPr (boolean, optional).
 * We will also add userId automatically.
 */
const addLift = async (liftData) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to add lift.");
  }

  if (!liftData.exerciseName || typeof liftData.weight !== 'number' || typeof liftData.reps !== 'number' || typeof liftData.sets !== 'number' || !liftData.date) {
    throw new Error("Missing required lift data fields (exerciseName, weight, reps, sets, date).");
  }

  try {
    const liftDocRef = await firestore().collection('userLifts').add({
      userId: currentUser.uid,
      exerciseName: liftData.exerciseName, // Storing name directly for easier display, could also be exerciseId
      weight: liftData.weight,
      reps: liftData.reps,
      sets: liftData.sets, // Assuming one document per set might be too granular, this stores the summary of sets.
                           // If each set is logged individually, the model changes.
                           // For now, let's assume this entry represents 'X sets of Y reps at Z weight'.
      date: firestore.Timestamp.fromDate(new Date(liftData.date)), // Ensure it's a Firestore Timestamp
      notes: liftData.notes || '',
      isPr: liftData.isPr || false,
      createdAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Lift added with ID: ', liftDocRef.id);
    return { id: liftDocRef.id, ...liftData, userId: currentUser.uid };
  } catch (error) {
    console.error("Error adding lift to Firestore: ", error);
    throw error;
  }
};

/**
 * Retrieves all lifts for the current user, optionally filtered by exercise name.
 * @param {string} [exerciseName] - Optional. The name of the exercise to filter by.
 * @returns {Promise<Array>} - A promise that resolves to an array of lift documents.
 */
const getLiftsForUser = async (exerciseName) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    console.warn("No user authenticated to fetch lifts.");
    return [];
  }

  try {
    let query = firestore().collection('userLifts').where('userId', '==', currentUser.uid);

    if (exerciseName) {
      query = query.where('exerciseName', '==', exerciseName);
    }

    // Order by date, most recent first
    query = query.orderBy('date', 'desc');

    const snapshot = await query.get();
    const lifts = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    return lifts;
  } catch (error) {
    console.error("Error fetching lifts: ", error);
    throw error;
  }
};


/**
 * Retrieves a specific lift by its ID.
 * @param {string} liftId - The ID of the lift document.
 * @returns {Promise<object|null>} - A promise that resolves to the lift document or null if not found.
 */
const getLiftById = async (liftId) => {
  try {
    const doc = await firestore().collection('userLifts').doc(liftId).get();
    if (doc.exists) {
      return { id: doc.id, ...doc.data() };
    } else {
      console.log("No such lift document!");
      return null;
    }
  } catch (error) {
    console.error("Error fetching lift by ID: ", error);
    throw error;
  }
};

/**
 * Updates an existing lift record in Firestore.
 * @param {string} liftId - The ID of the lift to update.
 * @param {object} newData - An object containing the fields to update.
 */
const updateLift = async (liftId, newData) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to update lift.");
  }
  // Optional: Check if the lift actually belongs to the user before updating, for security.

  try {
    // Ensure date is a Firestore Timestamp if it's being updated
    if (newData.date && !(newData.date instanceof firestore.Timestamp)) {
        newData.date = firestore.Timestamp.fromDate(new Date(newData.date));
    }
    await firestore().collection('userLifts').doc(liftId).update({
      ...newData,
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Lift updated successfully: ', liftId);
    return { id: liftId, ...newData };
  } catch (error) {
    console.error("Error updating lift: ", error);
    throw error;
  }
};

/**
 * Deletes a lift record from Firestore.
 * @param {string} liftId - The ID of the lift to delete.
 */
const deleteLift = async (liftId) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to delete lift.");
  }
  // Optional: Check if the lift actually belongs to the user before deleting.

  try {
    await firestore().collection('userLifts').doc(liftId).delete();
    console.log('Lift deleted successfully: ', liftId);
  } catch (error) {
    console.error("Error deleting lift: ", error);
    throw error;
  }
};

// --- Predefined Exercises (Simple Example) ---
// In a real app, this might come from a Firestore collection or a more robust config.
const PREDEFINED_EXERCISES = [
  { id: 'bench_press', name: 'Bench Press', muscleGroup: 'Chest' },
  { id: 'squat', name: 'Squat', muscleGroup: 'Legs' },
  { id: 'deadlift', name: 'Deadlift', muscleGroup: 'Back/Legs' },
  { id: 'overhead_press', name: 'Overhead Press', muscleGroup: 'Shoulders' },
  { id: 'barbell_row', name: 'Barbell Row', muscleGroup: 'Back' },
  { id: 'pull_ups', name: 'Pull Ups', muscleGroup: 'Back/Biceps' },
  { id: 'bicep_curls', name: 'Bicep Curls', muscleGroup: 'Biceps' },
  { id: 'tricep_dips', name: 'Tricep Dips', muscleGroup: 'Triceps' },
  { id: 'leg_press', name: 'Leg Press', muscleGroup: 'Legs' },
  { id: 'lat_pulldown', name: 'Lat Pulldown', muscleGroup: 'Back' },
];

const getPredefinedExercises = () => {
  return PREDEFINED_EXERCISES;
};


export default {
  addLift,
  getLiftsForUser,
  getLiftById,
  updateLift,
  deleteLift,
  getPredefinedExercises,
};
