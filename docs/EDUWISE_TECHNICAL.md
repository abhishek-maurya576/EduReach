# EduWise Technical Documentation

This technical documentation provides developers with the necessary information to understand, modify, and extend the EduWise feature in the EduReach application.

## Architecture Overview

EduWise follows the MVVM (Model-View-ViewModel) architecture pattern:

```
                 ┌───────────────┐
                 │     View      │
                 │ (Composables) │
                 └───────┬───────┘
                         │
                         ▼
┌─────────────┐  ┌───────────────┐  ┌─────────────┐
│  Repository  │◄─┤   ViewModel   │◄─┤    State    │
└──────┬──────┘  └───────────────┘  └─────────────┘
       │                 ▲
       │                 │
       ▼                 │
┌─────────────┐  ┌───────────────┐
│  Database   │  │     Models    │
└─────────────┘  └───────────────┘
```

## Key Components

### Models
- `EduWiseSession`: Represents a learning session with metadata
- `EduWiseMessage`: Individual messages within a session
- `LearningStyle`: Enum defining supported learning styles
- `EduWisePersonality`: Enum defining AI personality types
- `StudyRecommendation`: Structured study suggestions

### Database
- `EduWiseDatabase`: Room database that stores all EduWise data
- `EduWiseSessionDao`: Data access object for sessions
- `EduWiseMessageDao`: Data access object for messages
- `EnumConverters`: TypeConverters for Room to handle enums

### Repository
- `EduWiseRepository`: Single source of truth for EduWise data
- Handles communication between ViewModel and data sources
- Processes AI prompts and responses
- Manages session and message persistence

### ViewModel
- `EduWiseViewModel`: Manages UI state and business logic
- Handles user actions and updates state accordingly
- Communicates with repository for data operations
- Provides StateFlow for the UI to observe

### UI Components
- `EduWiseScreen`: Main container composable for the feature
- `ChatArea`: Displays message history
- `MessageInputArea`: Handles user input
- `SidePanel`: Shows settings and preferences
- `LearningStyleAssessment`: Dialog for style selection
- `SessionListOverlay`: Manages session history

## Database Schema

### EduWiseSession
```sql
CREATE TABLE eduwise_sessions (
    id TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    title TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    lastQuery TEXT,
    lastResponse TEXT,
    learningStyle TEXT,
    personality TEXT NOT NULL DEFAULT 'SUPPORTIVE'
)
```

### EduWiseMessage
```sql
CREATE TABLE eduwise_messages (
    id TEXT PRIMARY KEY,
    sessionId TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    isUserMessage INTEGER NOT NULL,
    emotionalTone TEXT,
    FOREIGN KEY (sessionId) REFERENCES eduwise_sessions(id) ON DELETE CASCADE
)
```

### StudyRecommendation
```sql
CREATE TABLE eduwise_recommendations (
    id TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    description TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    dueDate INTEGER,
    priority INTEGER NOT NULL DEFAULT 2,
    isCompleted INTEGER NOT NULL DEFAULT 0,
    category TEXT NOT NULL
)
```

## Key Workflows

### Session Creation
1. User triggers session creation (or happens automatically)
2. ViewModel calls repository's `createSession` method
3. Repository generates UUID and creates session entity
4. Session is stored in Room database
5. ViewModel updates state with new session ID
6. UI reflects the new session

### Message Exchange
1. User enters message in input area
2. ViewModel receives message and calls repository's `sendMessage`
3. Repository:
   - Creates and stores user message
   - Detects emotional tone (if enabled)
   - Generates AI prompt based on learning style and personality
   - Processes response
   - Creates and stores AI response
4. Messages are observed through Flow from the database
5. UI updates to show new messages

### Learning Style Selection
1. User opens learning style dialog
2. User selects preferred learning style
3. ViewModel calls repository's `updateLearningStyle` method
4. Repository updates user's learning style preference
5. Future AI responses are tailored to this style

## AI Prompt Engineering

The AI responses are customized based on:
1. **Learning Style**: Visual, Auditory, Kinesthetic, Reading/Writing, or Simple
2. **AI Personality**: Supportive, Challenging, or Socratic
3. **Emotional Context**: Detected from user messages

Example prompt template:
```
You are EduWise, an educational AI coach focused on helping students learn effectively.

[PERSONALITY_GUIDANCE]

[STYLE_GUIDANCE]

[EMOTIONAL_RESPONSE]

Focus on improving metacognitive abilities and learning strategies, not just providing answers.

Student's message: [MESSAGE]
```

Where:
- `[PERSONALITY_GUIDANCE]` adapts tone based on selected personality
- `[STYLE_GUIDANCE]` tailors content format based on learning style
- `[EMOTIONAL_RESPONSE]` adjusts approach based on detected emotion

## Extension Points

### Adding New Learning Styles
1. Add new enum value to `LearningStyle`
2. Update UI in `LearningStyleAssessment`
3. Add style guidance in `createEduWisePrompt`

### Adding New AI Personalities
1. Add new enum value to `EduWisePersonality`
2. Update UI in `PersonalitySelector`
3. Add personality guidance in `createEduWisePrompt`

### Enhancing Study Recommendations
1. Update `StudyRecommendation` model
2. Modify `generateStudyRecommendations` algorithm
3. Update UI components for recommendation display

## Performance Considerations

- Large chat histories may impact performance
- Consider pagination for message loading
- Use proper indexing for database queries
- Implement caching for frequently accessed data
- Consider background processing for AI responses

## Testing

### Unit Tests
- Test repository methods with mock data sources
- Test ViewModel state transitions
- Test database operations

### UI Tests
- Test composable rendering with different states
- Test user interactions and state updates
- Test navigation between screens

### Integration Tests
- Test end-to-end workflows with fake repositories
- Test database migrations

## Security Considerations

- Encrypt sensitive user data
- Implement proper authentication for cloud storage
- Sanitize inputs before processing
- Handle API keys securely
- Implement proper error handling to prevent data leaks

---

This documentation is meant to provide a high-level overview of the EduWise feature's technical implementation. For detailed code documentation, refer to the inline code comments. 