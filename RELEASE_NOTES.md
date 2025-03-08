# EduReach Release Notes

## Version 1.2.0 (June 2024)

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

## Version 1.1.0 (April 2024)

- Added offline support
- Improved UI/UX design
- Fixed various bugs and performance issues

## Version 1.0.0 (February 2024)

- Initial release with core features
- Basic AI tutoring functionality
- Quiz, task management and progress tracking features
- Course selection and user profiles 