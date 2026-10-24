package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun StatusBadge(status: StudyStatus) {
    val (bgColor, textColor, text) = when (status) {
        StudyStatus.NEW -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "New")
        StudyStatus.LEARNING -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Learning")
        StudyStatus.REVIEWING -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Reviewing")
        StudyStatus.MASTERED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Mastered")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun VerificationBadge(status: UserVerificationStatus) {
    val (bgColor, textColor, text) = when (status) {
        UserVerificationStatus.UNVERIFIED -> Triple(Color(0xFF64748B).copy(alpha = 0.15f), Color(0xFF64748B), "Unverified")
        UserVerificationStatus.VERIFIED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Verified")
        UserVerificationStatus.QUESTIONABLE -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Questionable")
        UserVerificationStatus.CORRECTED -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Corrected")
        UserVerificationStatus.NEEDS_EXPLANATION -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "Needs Info")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AnomalyBadge(count: Int = 1) {
    Surface(
        color = ErrorRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "PDF Extraction Flag",
                tint = ErrorRed,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " Source Flag",
                color = ErrorRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EntryNumberBadge(number: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = "#$number",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PageBadge(page: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = "Page $page / 46",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun AudioSpeakButton(
    textToSpeak: String,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onSpeak(textToSpeak) },
        modifier = modifier.testTag("audio_speak_button")
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Pronounce Word",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DifficultToggleButton(
    isDifficult: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.testTag("difficult_toggle_button")
    ) {
        Icon(
            imageVector = if (isDifficult) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isDifficult) "Remove from Difficult" else "Mark as Difficult",
            tint = if (isDifficult) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BookmarkToggleButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.testTag("bookmark_toggle_button")
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark Entry",
            tint = if (isBookmarked) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
