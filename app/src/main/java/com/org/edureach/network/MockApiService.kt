package com.org.edureach.network

import com.org.edureach.data.Lesson
import com.org.edureach.data.Question
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Mock API service for development and testing
 */
class MockApiService {
    // Mock data
    private val lessons = listOf(
        Lesson(
            id = "1",
            title = "Introduction to Programming",
            description = "Learn the basics of programming concepts",
            content = "Programming is the process of creating a set of instructions that tell a computer how to perform a task. Programming can be done using a variety of computer programming languages, such as JavaScript, Python, and C++.",
            videoId = "intro_video_1"
        ),
        Lesson(
            id = "2",
            title = "Variables and Data Types",
            description = "Understanding variables and different data types",
            content = "Variables are containers for storing data values. In most programming languages, variables are explicitly declared before they are used. The declaration specifies the variable name and the data type.",
            videoId = "variables_video_1"
        ),
        Lesson(
            id = "3",
            title = "Control Flow",
            description = "Learn about conditional statements and loops",
            content = "Control flow is the order in which individual statements, instructions or function calls are executed or evaluated. The control flow statements in most languages allow the program to make decisions and jump to different parts of the code.",
            videoId = "control_flow_video_1"
        )
    )
    
    private val questions = mapOf(
        "1" to listOf(
            Question(
                questionId = "q1_1",
                lessonId = "1",
                text = "What is programming?",
                options = listOf(
                    "A way to communicate with computers",
                    "A process of creating instructions for computers",
                    "A type of computer hardware",
                    "A programming language"
                ),
                correctAnswerIndex = 1,
                explanation = "Programming is the process of creating a set of instructions that tell a computer how to perform a task."
            ),
            Question(
                questionId = "q1_2",
                lessonId = "1",
                text = "Which of these is a programming language?",
                options = listOf(
                    "HTML",
                    "HTTP",
                    "Python",
                    "Windows"
                ),
                correctAnswerIndex = 2,
                explanation = "Python is a programming language. HTML is a markup language, HTTP is a protocol, and Windows is an operating system."
            )
        ),
        "2" to listOf(
            Question(
                questionId = "q2_1",
                lessonId = "2",
                text = "What is a variable?",
                options = listOf(
                    "A fixed value",
                    "A container for storing data values",
                    "A programming language",
                    "A type of function"
                ),
                correctAnswerIndex = 1,
                explanation = "Variables are containers for storing data values."
            ),
            Question(
                questionId = "q2_2",
                lessonId = "2",
                text = "Which of these is a primitive data type?",
                options = listOf(
                    "Array",
                    "Object",
                    "Integer",
                    "Class"
                ),
                correctAnswerIndex = 2,
                explanation = "Integer is a primitive data type. Arrays, objects, and classes are composite data types."
            )
        ),
        "3" to listOf(
            Question(
                questionId = "q3_1",
                lessonId = "3",
                text = "What is a conditional statement?",
                options = listOf(
                    "A statement that always executes",
                    "A statement that executes based on a condition",
                    "A statement that never executes",
                    "A statement that defines a variable"
                ),
                correctAnswerIndex = 1,
                explanation = "A conditional statement executes different code based on whether a condition is true or false."
            )
        )
    )
    
    /**
     * Get all lessons
     */
    suspend fun getLessons(): List<Lesson> {
        // Simulate network delay
        delay(500)
        return lessons
    }
    
    /**
     * Get a lesson by ID
     */
    suspend fun getLessonById(lessonId: String): Lesson {
        // Simulate network delay
        delay(300)
        return lessons.find { it.id == lessonId } 
            ?: throw Exception("Lesson not found")
    }
    
    /**
     * Get questions for a lesson
     */
    suspend fun getQuestionsForLesson(lessonId: String): List<Question> {
        // Simulate network delay
        delay(300)
        return questions[lessonId] ?: emptyList()
    }
    
    /**
     * Submit user progress to server
     */
    suspend fun submitProgress(userId: String, lessonId: String, completed: Boolean): Boolean {
        // Simulate network delay
        delay(500)
        // Simulate success
        return true
    }
}
