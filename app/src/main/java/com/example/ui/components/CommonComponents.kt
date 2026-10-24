package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyStatus
import com.example.data.model.UserVerificationStatus
import com.example.data.model.VocabEntry
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun StatusBadge(status: StudyStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        StudyStatus.NEW -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "NEW")
        StudyStatus.LEARNING -> Triple(AccentGold.copy(alpha = 0.15f), AccentGold, "LEARNING")
        StudyStatus.REVIEWING -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "REVIEWING")
        StudyStatus.MASTERED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "MASTERED")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VerificationBadge(status: UserVerificationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        UserVerificationStatus.VERIFIED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Verified")
        UserVerificationStatus.UNVERIFIED -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, "Unverified")
        UserVerificationStatus.QUESTIONABLE -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Questionable")
        UserVerificationStatus.CORRECTED -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Corrected")
        UserVerificationStatus.NEEDS_EXPLANATION -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "Needs Review")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnomalyBadge(notes: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(WarningOrange.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "PDF Source Anomaly",
            tint = WarningOrange,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Flag: $notes",
            color = WarningOrange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TtsButton(
    text: String,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 20
) {
    IconButton(
        onClick = { onSpeak(text) },
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Listen to pronunciation",
            tint = PrimaryBlue,
            modifier = Modifier.size(size.dp)
        )
    }
}
