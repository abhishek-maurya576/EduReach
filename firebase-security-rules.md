# Firebase Security Rules for EduReach

The following security rules should be set up in your Firebase Firestore to properly handle tasks and other collections in the EduReach app.

## Firestore Security Rules

Go to the Firebase Console > Firestore Database > Rules and update them with the following:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read and write only their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow users to read and write only their own tasks
    match /tasks/{taskId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Allow users to read and write their progress
    match /progress/{progressId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Allow read access to course content for all authenticated users
    match /lessons/{lessonId} {
      allow read: if request.auth != null;
    }
    
    match /quizzes/{quizId} {
      allow read: if request.auth != null;
    }
    
    match /questions/{questionId} {
      allow read: if request.auth != null;
    }
  }
}
```

## Important Notes

1. These rules ensure that users can only read, update, and delete their own tasks.
2. The `userId` field is crucial for security - it must be included in every task document.
3. When creating a task, the app must set the `userId` field to the current user's UID.
4. The error "PERMISSION_DENIED: Missing or insufficient permissions" indicates that the security rules are working correctly but the code is trying to access data it doesn't have permission for.

## Common Issues

1. **Not setting userId field**: Ensure that the TaskRepository always includes the userId field when creating or updating tasks.
2. **User not authenticated**: Make sure the user is logged in before attempting to work with tasks.
3. **Missing fields**: All required fields in the security rules must be present in your documents.
4. **Wrong userId**: Ensure the userId in the document exactly matches the authenticated user's UID.

## Testing Rules

You can test your security rules in the Firebase Console:
1. Go to Firestore Database > Rules
2. Click "Publish" to apply your rules
3. Click on the "Rules Playground" tab to test different operations with different authentication states

When properly configured, these rules will provide security while allowing the Task feature to work correctly. 