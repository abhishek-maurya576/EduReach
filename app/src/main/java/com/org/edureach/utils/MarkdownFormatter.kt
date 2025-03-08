package com.org.edureach.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple Markdown formatter for Compose UI
 */
object MarkdownFormatter {
    
    @Composable
    fun MarkdownText(
        markdown: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Black,
        fontSize: Int = 16
    ) {
        val lines = markdown.split("\n")
        
        Column(modifier = modifier) {
            lines.forEach { line ->
                when {
                    // Heading 1
                    line.startsWith("# ") -> {
                        Text(
                            text = line.substring(2),
                            fontSize = (fontSize * 1.8).sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    // Heading 2
                    line.startsWith("## ") -> {
                        Text(
                            text = line.substring(3),
                            fontSize = (fontSize * 1.5).sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                        )
                    }
                    // Heading 3
                    line.startsWith("### ") -> {
                        Text(
                            text = line.substring(4),
                            fontSize = (fontSize * 1.3).sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    // Heading 4
                    line.startsWith("#### ") -> {
                        Text(
                            text = line.substring(5),
                            fontSize = (fontSize * 1.1).sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                        )
                    }
                    // Bulleted list
                    line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                        val content = when {
                            line.startsWith("- ") -> line.substring(2)
                            line.startsWith("* ") -> line.substring(2)
                            else -> line.substring(2)
                        }
                        
                        Text(
                            buildAnnotatedString {
                                append("• ")
                                append(formatInlineMarkdown(content))
                            },
                            fontSize = fontSize.sp,
                            color = color,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                    // Numbered list
                    line.matches(Regex("^\\d+\\.\\s+.*$")) -> {
                        val splitIndex = line.indexOf(". ") + 2
                        if (splitIndex > 2 && splitIndex < line.length) {
                            val number = line.substring(0, splitIndex)
                            val content = line.substring(splitIndex)
                            
                            Text(
                                buildAnnotatedString {
                                    append(number)
                                    append(formatInlineMarkdown(content))
                                },
                                fontSize = fontSize.sp,
                                color = color,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                            )
                        } else {
                            // Fallback for malformed numbered list
                            Text(
                                text = formatInlineMarkdown(line),
                                fontSize = fontSize.sp,
                                color = color
                            )
                        }
                    }
                    // Empty line
                    line.isBlank() -> {
                        Text(
                            text = "",
                            fontSize = (fontSize * 0.5).sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }
                    // Regular text with inline formatting
                    else -> {
                        Text(
                            text = formatInlineMarkdown(line),
                            fontSize = fontSize.sp,
                            color = color,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
    
    private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold text
                i + 3 < text.length && text.substring(i, i + 2) == "**" && text.indexOf("**", i + 2) > 0 -> {
                    val endIndex = text.indexOf("**", i + 2)
                    if (endIndex > 0) {
                        val boldText = text.substring(i + 2, endIndex)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(boldText)
                        }
                        i = endIndex + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic text
                i + 2 < text.length && text[i] == '*' && text.indexOf("*", i + 1) > 0 -> {
                    val endIndex = text.indexOf("*", i + 1)
                    if (endIndex > 0) {
                        val italicText = text.substring(i + 1, endIndex)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(italicText)
                        }
                        i = endIndex + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Underline/Link (simplification - just show the text)
                i + 3 < text.length && text.substring(i, i + 2) == "__" && text.indexOf("__", i + 2) > 0 -> {
                    val endIndex = text.indexOf("__", i + 2)
                    if (endIndex > 0) {
                        val underlineText = text.substring(i + 2, endIndex)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(underlineText)
                        }
                        i = endIndex + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Regular text
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
} 