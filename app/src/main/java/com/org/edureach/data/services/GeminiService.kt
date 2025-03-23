package com.org.edureach.data.services

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.org.edureach.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.org.edureach.data.Lesson

/**
 * Service for interacting with the Gemini API to fetch Python tutorial content
 */
class GeminiService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                topK = 32
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
    }

    /**
     * Fetch Python content from Gemini
     */
    suspend fun fetchPythonContent(lessonId: String, topic: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Check if API key is valid before making request
                if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                    Log.e("GeminiService", "Invalid API key detected. Using fallback content.")
                    return@withContext getFallbackLessonContent(lessonId)
                }
                
                val prompt = buildPrompt(lessonId, topic)
                val response = generativeModel.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error fetching Python content", e)
            getFallbackLessonContent(lessonId)
        }
    }
    
    /**
     * Build a prompt for Gemini
     */
    private fun buildPrompt(lessonId: String, topic: String): String {
        val (lessonType, difficulty, goals) = getContentParameters(lessonId)
        
        return """
            Act as an expert Python instructor creating educational content about $lessonType, focusing on $topic.
            Structure the content as follows:
            1. A concise explanation of the concept (2-3 paragraphs)
            2. Key points to remember (3-5 bullet points)
            3. Common pitfalls to avoid
            4. At least one practical example with code and explanation
            
            The content should be at a $difficulty level.
            Learning goals: $goals
            
            Keep the language simple and beginner-friendly. Use clear examples that illustrate practical usage.
        """.trimIndent()
    }
    
    /**
     * Get content parameters for lessons
     */
    private fun getContentParameters(lessonId: String): Triple<String, String, String> {
        return when (lessonId) {
            "python-basics" -> Triple(
                "Python basics (syntax, variables, data types, and operators)",
                "beginner",
                "Understand Python syntax, learn how variables work, and become familiar with basic data types"
            )
            "python-control-flow" -> Triple(
                "Python control flow structures (conditionals and loops)",
                "beginner",
                "Master decision making with if-else statements and iteration with Python loops"
            )
            "python-functions" -> Triple(
                "Python functions, parameters, return values, and scope",
                "intermediate",
                "Learn to create reusable code blocks, understand parameter passing, and function return values"
            )
            "python-data-structures" -> Triple(
                "Python data structures (lists, dictionaries, sets, tuples)",
                "intermediate",
                "Understand how to use Python's built-in data structures effectively"
            )
            "python-oop" -> Triple(
                "object-oriented programming in Python with classes and objects",
                "advanced",
                "Learn the principles of OOP including classes, objects, inheritance, and polymorphism"
            )
            "python-advanced" -> Triple(
                "advanced Python concepts like file handling, modules, and exception handling",
                "advanced",
                "Master reading/writing files, importing modules, and handling errors properly"
            )
            else -> Triple(
                "Python programming fundamentals",
                "beginner",
                "Understand core Python concepts and syntax"
            )
        }
    }
    
    /**
     * Generate a complete Python lesson
     */
    suspend fun generateCompletePythonLesson(lessonId: String): Lesson? {
        try {
            val topic = getLessonTopic(lessonId)
            val mainContent = fetchPythonContent(lessonId, topic) ?: return null
            
            return Lesson(
                id = lessonId,
                title = getTitle(lessonId),
                description = getDescription(lessonId),
                content = mainContent,
                videoId = "dQw4w9WgXcQ", // Placeholder video ID
                isCompleted = false,
                duration = 20
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating complete Python lesson", e)
            return getFallbackLesson(lessonId)
        }
    }
    
    /**
     * Fetch code examples for a lesson
     */
    suspend fun fetchPythonCodeExamples(topic: String, difficulty: String = "beginner"): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Check if API key is valid before making request
                if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                    Log.e("GeminiService", "Invalid API key detected. Using fallback content.")
                    return@withContext getPythonCodeExamplesFallback()
                }
                
                val prompt = """
                    Generate 2-3 practical Python code examples about $topic at a $difficulty level.
                    
                    For each example:
                    1. Include a brief description of what the code demonstrates
                    2. Add comments explaining key concepts within the code
                    3. Show expected output with # Output: comments
                    
                    Format the code properly for readability.
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error fetching Python code examples", e)
            getPythonCodeExamplesFallback()
        }
    }
    
    /**
     * Fetch exercises for a lesson
     */
    suspend fun fetchPythonExercises(topic: String, difficulty: String = "beginner", count: Int = 2): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Check if API key is valid before making request
                if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                    Log.e("GeminiService", "Invalid API key detected. Using fallback content.")
                    return@withContext getPythonExercisesFallback()
                }
                
                val prompt = """
                    Generate $count Python programming exercises about $topic at a $difficulty level.
                    
                    For each exercise:
                    1. Write a clear problem statement that tests understanding of $topic
                    2. Include 1-2 hints that guide without giving away the solution
                    3. Provide starter code when appropriate
                    4. Describe what the expected output or behavior should be
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error fetching Python exercises", e)
            getPythonExercisesFallback()
        }
    }
    
    /**
     * Fetch quiz questions for a lesson
     */
    suspend fun fetchPythonQuizQuestions(topic: String, count: Int = 3): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Check if API key is valid before making request
                if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                    Log.e("GeminiService", "Invalid API key detected. Using fallback content.")
                    return@withContext getPythonQuizQuestionsFallback()
                }
                
                val prompt = """
                    Generate $count multiple-choice quiz questions about $topic for Python learners.
                    
                    For each question:
                    1. Write a clear question about an important concept related to $topic
                    2. Provide 4 options (A, B, C, D)
                    3. Mark the correct answer
                    4. Include a brief explanation of why the answer is correct
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error fetching Python quiz questions", e)
            getPythonQuizQuestionsFallback()
        }
    }
    
    /**
     * Fetch W3Schools references
     */
    suspend fun fetchW3SchoolsReferences(topic: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Check if API key is valid before making request
                if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                    Log.e("GeminiService", "Invalid API key detected. Using fallback content.")
                    return@withContext getW3SchoolsReferencesFallback()
                }
                
                val prompt = """
                    Based on W3Schools Python tutorials (https://www.w3schools.com/python/), 
                    provide a summary of key concepts related to $topic in Python programming.
                    
                    Include:
                    1. The main concepts covered in W3Schools' section on this topic
                    2. Any important syntax or usage patterns mentioned
                    3. Reference to specific W3Schools examples if relevant
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error fetching W3Schools references", e)
            getW3SchoolsReferencesFallback()
        }
    }
    
    /**
     * Get topic for a lesson ID
     */
    private fun getLessonTopic(lessonId: String): String {
        return when (lessonId) {
            "python-basics" -> "Python basics, variables, data types, and operators"
            "python-control-flow" -> "Python control flow with if-else statements and loops"
            "python-functions" -> "Python functions, parameters, return values, and scope"
            "python-data-structures" -> "Python data structures including lists, dictionaries, sets, and tuples"
            "python-oop" -> "Object-oriented programming in Python with classes and objects"
            else -> "Python programming fundamentals"
        }
    }
    
    /**
     * Get title for a lesson ID
     */
    private fun getTitle(lessonId: String): String {
        return when (lessonId) {
            "python-basics" -> "Python Programming Fundamentals"
            "python-control-flow" -> "Python Control Flow Structures"
            "python-functions" -> "Functions in Python"
            "python-data-structures" -> "Python Data Structures"
            "python-oop" -> "Object-Oriented Programming in Python"
            else -> "Python Programming"
        }
    }
    
    /**
     * Get description for a lesson ID
     */
    private fun getDescription(lessonId: String): String {
        return when (lessonId) {
            "python-basics" -> "Learn Python's basic syntax, variables, data types, and simple operations."
            "python-control-flow" -> "Master decision making with if-else statements and iteration with Python loops."
            "python-functions" -> "Understand how to create reusable code blocks with Python functions."
            "python-data-structures" -> "Explore Python's built-in data structures to organize and manipulate information."
            "python-oop" -> "Learn object-oriented programming principles with Python classes and objects."
            else -> "Explore fundamental concepts in Python programming."
        }
    }

    /**
     * Get fallback lesson
     */
    private fun getFallbackLesson(lessonId: String): Lesson {
        val content = getFallbackLessonContent(lessonId)
        return Lesson(
            id = lessonId,
            title = getTitle(lessonId),
            description = getDescription(lessonId),
            content = content,
            videoId = "dQw4w9WgXcQ", // Placeholder video ID
            isCompleted = false,
            duration = 20
        )
    }
    
    /**
     * Fallback content for lessons
     */
    private fun getFallbackLessonContent(lessonId: String): String {
        return when (lessonId) {
            "python-basics" -> """
                Python is a high-level, interpreted programming language that is easy to learn and use. It's designed to be readable with simple, straightforward syntax.
                
                Python variables are created when you assign a value to them. Unlike some languages, you don't need to declare variables before using them or specify their type. Python automatically determines the variable type based on the assigned value.
                
                Key Points to Remember:
                • Python is case-sensitive: myVariable and myvariable are different variables
                • Variable names can contain letters, numbers, and underscores, but cannot start with a number
                • Python uses dynamic typing, meaning the type can change if you assign a new value
                • Common data types include: strings, integers, floats, booleans, lists, tuples, and dictionaries
                
                Common Pitfalls:
                1. Forgetting that Python uses indentation for code blocks instead of braces
                2. Mixing tabs and spaces for indentation
                3. Confusing = (assignment) with == (equality comparison)
                
                Example:
                ```python
                # Creating different types of variables
                name = "John"          # string
                age = 25               # integer
                height = 1.85          # float
                is_student = True      # boolean
                
                # Using variables
                print("Name:", name)
                print("Age:", age)
                print("Height:", height, "meters")
                print("Student:", is_student)
                
                # You can easily change variable types
                age = "twenty-five"    # age is now a string
                print("Age:", age)
                ```
            """.trimIndent()
            "python-control-flow" -> """
                Control flow in Python determines how a program executes statements in a specific order. Two fundamental control flow concepts are conditional statements and loops.
                
                Conditional statements (if, elif, else) allow the program to make decisions based on whether conditions are True or False. Loops (for, while) enable the program to repeat code multiple times.
                
                Key Points to Remember:
                • Python uses indentation to define blocks of code in control statements
                • Comparison operators (==, !=, >, <, >=, <=) are used to form conditions
                • Logical operators (and, or, not) combine conditions
                • The for loop is typically used for iterating over a sequence (like a list or string)
                • The while loop repeats as long as a condition remains True
                
                Common Pitfalls:
                1. Infinite loops (forgetting to update the condition in a while loop)
                2. Off-by-one errors when working with ranges and indices
                3. Forgetting that indentation defines the scope of if/else and loop blocks
                
                Example:
                ```python
                # If-elif-else example
                age = 25
                
                if age < 18:
                    print("You are a minor")
                elif age >= 18 and age < 65:
                    print("You are an adult")
                else:
                    print("You are a senior")
                
                # For loop example
                fruits = ["apple", "banana", "cherry"]
                for fruit in fruits:
                    print(f"I like {fruit}s")
                ```
            """.trimIndent()
            else -> """
                Python is a versatile, high-level programming language that emphasizes code readability and simplicity. Created by Guido van Rossum and first released in 1991, Python has become one of the most popular programming languages in the world.
                
                What makes Python special is its straightforward syntax that uses indentation to define code blocks, making it more readable compared to other languages that use braces or keywords. Python also follows the philosophy that there should be one obvious way to do things, reducing confusion for programmers.
                
                Key Points to Remember:
                • Python is an interpreted language, meaning code is executed line by line
                • Python uses dynamic typing, so you don't need to declare variable types
                • Python has a vast standard library and community-created packages
                • It supports multiple programming paradigms: procedural, object-oriented, and functional
                
                Common Pitfalls:
                1. Indentation errors (Python uses indentation to define code blocks)
                2. Mixing tabs and spaces for indentation
                3. Forgetting that Python is zero-indexed (first element is at position 0)
                
                Example:
                ```python
                # This is a simple Python program
                print("Welcome to Python!")
                
                # Create a list of numbers
                numbers = [1, 2, 3, 4, 5]
                
                # Calculate the sum of the numbers
                total = sum(numbers)
                print(f"The sum of {numbers} is {total}")
                ```
            """.trimIndent()
        }
    }
    
    /**
     * Fallback code examples
     */
    private fun getPythonCodeExamplesFallback(): String {
        return """
            # Example 1: Basic Python Program
            # This demonstrates the structure of a simple Python program
            
            def main():
                # Print a welcome message
                print("Welcome to Python!")
                
                # Create and use a variable
                name = "Programmer"
                print(f"Hello, {name}!")
                
            # Call the main function
            if __name__ == "__main__":
                main()
            # Output:
            # Welcome to Python!
            # Hello, Programmer!
            
            # Example 2: Working with Numbers
            # This shows basic arithmetic operations in Python
            
            a = 10
            b = 3
            
            # Addition
            sum_result = a + b
            print(f"{a} + {b} = {sum_result}")
            
            # Subtraction
            difference = a - b
            print(f"{a} - {b} = {difference}")
            
            # Multiplication
            product = a * b
            print(f"{a} * {b} = {product}")
            
            # Division (returns float)
            quotient = a / b
            print(f"{a} / {b} = {quotient}")
        """.trimIndent()
    }
    
    /**
     * Fallback exercises
     */
    private fun getPythonExercisesFallback(): String {
        return """
            1. String Reversal (Easy)
               
               Problem: Write a function that takes a string and returns the reversed string.
               
               Hint 1: You can access string characters using indexing.
               Hint 2: Python has built-in ways to reverse a string.
               
               Starter code:
               ```
               def reverse_string(text):
                   # Your code here
                   pass
               
               print(reverse_string("Python"))
               ```
               
               Expected output: nohtyP
            
            2. Number Guessing Game (Medium)
               
               Problem: Create a simple number guessing game where the computer generates a random number 
               between 1 and 100, and the user has to guess it.
               
               Hint 1: Use the random module to generate a random number.
               Hint 2: Provide feedback to guide the user's guesses (too high, too low).
               
               Starter code:
               ```
               import random
               
               # Generate a random number between 1 and 100
               target_number = random.randint(1, 100)
               
               # Your code here
               ```
        """.trimIndent()
    }
    
    /**
     * Fallback quiz questions
     */
    private fun getPythonQuizQuestionsFallback(): String {
        return """
            1. Which of the following data types is mutable in Python?
               A) String
               B) Tuple
               C) List
               D) Integer
               
               Correct Answer: C
               Explanation: Lists are mutable, meaning their elements can be changed after creation. Strings, tuples, and integers are immutable in Python.
            
            2. What does the len() function do in Python?
               A) Returns the largest item in an iterable
               B) Returns the length of an object
               C) Returns the smallest item in an iterable
               D) Returns the average length of multiple objects
               
               Correct Answer: B
               Explanation: The len() function returns the number of items in an object. For strings, it returns the number of characters.
            
            3. What will be the output of print(2 ** 3)?
               A) 6
               B) 8
               C) 5
               D) Error
               
               Correct Answer: B
               Explanation: The ** operator in Python represents exponentiation. 2 ** 3 calculates 2 raised to the power of 3, which is 8.
        """.trimIndent()
    }
    
    /**
     * Fallback W3Schools references
     */
    private fun getW3SchoolsReferencesFallback(): String {
        return """
            According to W3Schools Python Tutorial:
            
            Python is a popular programming language created by Guido van Rossum and released in 1991. It's used for web development, software development, mathematics, and system scripting.
            
            Key aspects of Python from W3Schools:
            
            1. Syntax and Readability:
               - Python uses indentation to indicate code blocks
               - Python's syntax allows programmers to express concepts in fewer lines of code
            
            2. Versatility:
               - Python works on different platforms (Windows, Mac, Linux, etc.)
               - Python is both object-oriented and procedural
            
            3. Beginner-Friendly:
               - Python has simple syntax similar to the English language
               - Python uses new lines to complete commands
            
            For more detailed tutorials and examples, visit the Python section of W3Schools:
            https://www.w3schools.com/python/
        """.trimIndent()
    }

    /**
     * Update lesson progress
     */
    suspend fun updateLessonProgress(userId: String, lessonId: String, completed: Boolean) {
        // Currently a mock implementation
        Log.d("GeminiService", "Progress updated for user $userId, lesson $lessonId, completed: $completed")
        // Simulate a delay
        withContext(Dispatchers.IO) {
            kotlinx.coroutines.delay(300)
        }
    }
} 