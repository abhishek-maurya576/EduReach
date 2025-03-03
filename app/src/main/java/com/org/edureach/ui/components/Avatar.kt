package com.org.edureach.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.org.edureach.R

@Composable
fun Avatar(
    avatarId: Int,
    size: Dp = 40.dp,
    onClick: () -> Unit = {}
) {
    Image(
        painter = painterResource(
            id = if (avatarId == 1) R.drawable.avatar_1 else R.drawable.avatar_2
        ),
        contentDescription = "Profile Avatar",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, Color(0xFFDBA84F), CircleShape)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
} 