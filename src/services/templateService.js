import firestore from '@react-native-firebase/firestore';
import authService from './authService'; // To easily get current user's ID
import { v4 as uuidv4 } from 'uuid'; // For generating unique IDs for exercises within a template

/**
 * Creates a new workout template in Firestore.
 * @param {object} templateData - Object containing template details.
 * Expected fields: name (string), description (string, optional),
 * exercises (array of objects: { exerciseName: string, targetSets: number, targetReps: number }).
 */
const createTemplate = async (templateData) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to create template.");
  }

  if (!templateData.name || !templateData.exercises || templateData.exercises.length === 0) {
    throw new Error("Missing required fields (name, exercises). Template must have at least one exercise.");
  }

  // Add unique IDs to each exercise within the template
  const exercisesWithIds = templateData.exercises.map(ex => ({
    ...ex,
    id: uuidv4(), // Assign a unique ID to each exercise entry in the template
    exerciseName: ex.exerciseName || 'Unnamed Exercise', // Default name if not provided
    targetSets: parseInt(ex.targetSets, 10) || 0,
    targetReps: parseInt(ex.targetReps, 10) || 0,
  }));

  try {
    const templateDocRef = await firestore().collection('workoutTemplates').add({
      userId: currentUser.uid,
      name: templateData.name,
      description: templateData.description || '',
      exercises: exercisesWithIds, // Store exercises with their unique IDs
      createdAt: firestore.FieldValue.serverTimestamp(),
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Workout template created with ID: ', templateDocRef.id);
    return { id: templateDocRef.id, userId: currentUser.uid, ...templateData, exercises: exercisesWithIds };
  } catch (error) {
    console.error("Error creating workout template: ", error);
    throw error;
  }
};

/**
 * Retrieves all workout templates for the current user.
 * @returns {Promise<Array>} - A promise that resolves to an array of template documents.
 */
const getUserTemplates = async () => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    console.warn("No user authenticated to fetch templates.");
    return [];
  }

  try {
    const snapshot = await firestore()
      .collection('workoutTemplates')
      .where('userId', '==', currentUser.uid)
      .orderBy('createdAt', 'desc')
      .get();

    const templates = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    return templates;
  } catch (error) {
    console.error("Error fetching user templates: ", error);
    throw error;
  }
};

/**
 * Retrieves a specific workout template by its ID.
 * @param {string} templateId - The ID of the template document.
 * @returns {Promise<object|null>} - A promise that resolves to the template document or null if not found.
 */
const getTemplateById = async (templateId) => {
  try {
    const doc = await firestore().collection('workoutTemplates').doc(templateId).get();
    if (doc.exists) {
      return { id: doc.id, ...doc.data() };
    } else {
      console.log("No such template document!");
      return null;
    }
  } catch (error) {
    console.error("Error fetching template by ID: ", error);
    throw error;
  }
};

/**
 * Updates an existing workout template's details (name, description).
 * To update exercises within a template, use specific exercise manipulation functions.
 * @param {string} templateId - The ID of the template to update.
 * @param {object} newData - An object containing fields to update (e.g., name, description).
 */
const updateTemplateDetails = async (templateId, newData) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to update template.");
  }
  // Consider adding a check to ensure the template belongs to the user.

  try {
    await firestore().collection('workoutTemplates').doc(templateId).update({
      ...newData, // Should only contain fields like name, description
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Template details updated successfully: ', templateId);
  } catch (error) {
    console.error("Error updating template details: ", error);
    throw error;
  }
};

/**
 * Deletes a workout template from Firestore.
 * @param {string} templateId - The ID of the template to delete.
 */
const deleteTemplate = async (templateId) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error("User not authenticated to delete template.");
  }
  // Consider adding a check to ensure the template belongs to the user.

  try {
    await firestore().collection('workoutTemplates').doc(templateId).delete();
    console.log('Template deleted successfully: ', templateId);
  } catch (error) {
    console.error("Error deleting template: ", error);
    throw error;
  }
};

/**
 * Adds an exercise to an existing workout template.
 * @param {string} templateId - The ID of the template.
 * @param {object} exerciseData - Object for the new exercise { exerciseName, targetSets, targetReps }.
 */
const addExerciseToTemplate = async (templateId, exerciseData) => {
  if (!exerciseData.exerciseName || typeof exerciseData.targetSets !== 'number' || typeof exerciseData.targetReps !== 'number') {
    throw new Error("Invalid exercise data. Must include exerciseName, targetSets, and targetReps.");
  }
  const newExercise = {
    ...exerciseData,
    id: uuidv4(), // Unique ID for this exercise instance in the template
    targetSets: parseInt(exerciseData.targetSets, 10),
    targetReps: parseInt(exerciseData.targetReps, 10),
  };
  try {
    await firestore().collection('workoutTemplates').doc(templateId).update({
      exercises: firestore.FieldValue.arrayUnion(newExercise),
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Exercise added to template: ', templateId);
    return newExercise; // Return the added exercise with its new ID
  } catch (error) {
    console.error("Error adding exercise to template: ", error);
    throw error;
  }
};

/**
 * Removes an exercise from a workout template using its unique ID.
 * @param {string} templateId - The ID of the template.
 * @param {string} exerciseEntryId - The unique ID of the exercise entry within the template's exercises array.
 */
const removeExerciseFromTemplate = async (templateId, exerciseEntryId) => {
  try {
    const templateRef = firestore().collection('workoutTemplates').doc(templateId);
    const templateDoc = await templateRef.get();
    if (!templateDoc.exists) {
      throw new Error("Template not found.");
    }
    const template = templateDoc.data();
    const updatedExercises = template.exercises.filter(ex => ex.id !== exerciseEntryId);

    await templateRef.update({
      exercises: updatedExercises,
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Exercise removed from template: ', templateId);
  } catch (error) {
    console.error("Error removing exercise from template: ", error);
    throw error;
  }
};

/**
 * Updates an existing exercise within a workout template.
 * @param {string} templateId - The ID of the template.
 * @param {string} exerciseEntryId - The unique ID of the exercise entry to update.
 * @param {object} newExerciseData - New data for the exercise { exerciseName, targetSets, targetReps }.
 */
const updateExerciseInTemplate = async (templateId, exerciseEntryId, newExerciseData) => {
   if (!newExerciseData.exerciseName || typeof newExerciseData.targetSets !== 'number' || typeof newExerciseData.targetReps !== 'number') {
    throw new Error("Invalid new exercise data.");
  }
  try {
    const templateRef = firestore().collection('workoutTemplates').doc(templateId);
    const templateDoc = await templateRef.get();
    if (!templateDoc.exists) {
      throw new Error("Template not found.");
    }
    const template = templateDoc.data();
    const updatedExercises = template.exercises.map(ex => {
      if (ex.id === exerciseEntryId) {
        return {
          ...ex, // Keep the original ID and any other properties
          exerciseName: newExerciseData.exerciseName,
          targetSets: parseInt(newExerciseData.targetSets, 10),
          targetReps: parseInt(newExerciseData.targetReps, 10),
        };
      }
      return ex;
    });

    await templateRef.update({
      exercises: updatedExercises,
      updatedAt: firestore.FieldValue.serverTimestamp(),
    });
    console.log('Exercise updated in template: ', templateId);
  } catch (error) {
    console.error("Error updating exercise in template: ", error);
    throw error;
  }
};


export default {
  createTemplate,
  getUserTemplates,
  getTemplateById,
  updateTemplateDetails,
  deleteTemplate,
  addExerciseToTemplate,
  removeExerciseFromTemplate,
  updateExerciseInTemplate,
};

// For uuid library, you would run: npm install uuid
// And for React Native, if it's not already available: react-native-get-random-values
// Then import at the top: import 'react-native-get-random-values';
// This is because uuid v4 needs a crypto source.
// For this conceptual step, we assume uuidv4 works.
