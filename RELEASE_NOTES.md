# EduReach Release Notes

## Version 1.4.0 (March 2025)

### Major Features

#### EduWise AI Learning Companion
- **Adaptive Learning Styles**: Support for multiple learning styles (Visual, Auditory, Kinesthetic, Reading/Writing, Simple)
- **Session Management**: Create and manage learning sessions with context-aware memory
- **Customizable AI Personality**: Choose between Supportive, Challenging, or Socratic teaching approaches
- **Study Recommendations**: Personalized study suggestions with priority levels

#### User Experience Improvements
- **Streamlined Interface**: Redesigned top navigation with improved menu organization
- **Automatic Session Creation**: Sessions created automatically when needed
- **Enhanced Sharing**: Improved sharing of learning conversations

#### Technical Improvements
- **Room Database Integration**: Enhanced local storage for sessions and messages
- **MVVM Architecture**: Improved separation of concerns
- **Error Handling**: Better error states and user feedback
- **Performance Optimization**: Reduced UI recompositions

## Version 1.3.0 (Feb 2025)

### Major Features

#### Enhanced Task Management
- **Cloud Storage**: Tasks are now securely stored in Firebase Firestore
- **User-specific Tasks**: Each user can only see and manage their own tasks
- **Due Date Selection**: Calendar date picker for task due dates
- **Task Status Indicators**: Clear visual feedback for task completion status

#### Lesson Progression System
- **Sequential Learning**: Lessons must be completed in order
- **Progressive Unlocking**: New lessons unlock as you complete previous ones
- **Level-based Structure**: Content organized into beginner, intermediate, and advanced levels
- **Visual Indicators**: Clear indication of unlocked, locked, and completed lessons

### Technical Improvements
- **Firebase Security Rules**: Secure data access with custom Firestore rules
- **User Authentication**: Task access limited to authenticated users
- **Data Validation**: Improved data integrity checks

## Version 1.2.0 (Feb 2025)

### Major Features

#### Enhanced AI Tutor
- **Gemini 2.0 Flash API Integration**: Upgraded from previous AI solution to Google's Gemini 2.0 Flash API for better, faster responses
- **Chat History Storage**: Added persistent storage of AI Tutor conversations using Room Database
- **Response Length Control**: New options to choose Brief, Summary, or Long response formats
- **Markdown Formatting**: Improved content display with proper formatting of headings, lists, and emphasized text

#### User Experience Improvements
- **History UI**: Added a collapsible history panel for accessing past AI Tutor conversations
- **Form-Based Interface**: Redesigned AI Tutor interface with structured form for better input organization
- **Empty State Improvements**: Added informative empty states when no data is available
- **Enhanced Sharing**: Improved content sharing capabilities

### Technical Improvements
- **Room Database Integration**: Added local database storage for chat history
- **Optimized API Calls**: Improved API request/response handling
- **Error Handling**: Enhanced error states and user feedback
- **Code Maintenance**: Restructured codebase for better maintainability
- **Performance Optimization**: Reduced unnecessary recompositions in Compose UI

### Bug Fixes
- Fixed markdown rendering issues
- Addressed memory leaks in ViewModel
- Fixed UI inconsistencies in dark mode
- Improved scroll behavior in chat history
- Resolved state restoration issues

## Version 1.1.0 (Feb 2025)

- Added offline support
- Improved UI/UX design
- Fixed various bugs and performance issues

## Version 1.0.0 (January 2025)

- Initial release with core features
- Basic AI tutoring functionality
- Quiz, task management and progress tracking features
- Course selection and user profiles 
