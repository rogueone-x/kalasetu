package com.example.kalasetu.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalasetu.theme.SelectedPurple
import com.example.kalasetu.theme.SubtitleGray
import com.example.kalasetu.theme.UnselectedBorder

private data class OrganizerSection(val title: String, val options: List<String>)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerIntentScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val selectedPills = remember { mutableStateMapOf<String, Boolean>() }
    val scrollState = rememberScrollState()

    val sections = listOf(
        OrganizerSection("Hiring & Talent", listOf("Hire Artists", "Find Performers", "Book Creative Professionals", "Hire for Projects")),
        OrganizerSection("Events & Activities", listOf("Host Events", "Organize Shows", "Plan Workshops", "Run Competitions")),
        OrganizerSection("Collaborations", listOf("Collaborate with Artists", "Creative Partners", "Co-Create Projects", "Join Existing Projects")),
        OrganizerSection("Promotion & Growth", listOf("Promote a Brand", "Market an event", "Gain Visibility", "Build an Audience")),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What are you looking for?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Goal? We'll find your expert.",
                fontSize = 24.sp,
                color = SubtitleGray,
            )

            Spacer(modifier = Modifier.height(32.dp))

            sections.forEach { section ->
                Text(
                    text = section.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    section.options.forEach { pill ->
                        val isSelected = selectedPills[pill] ?: false
                        OutlinedButton(
                            onClick = {
                                selectedPills[pill] = !isSelected
                            },
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SelectedPurple
                                else UnselectedBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected)
                                    SelectedPurple.copy(alpha = 0.1f)
                                else Color.Transparent,
                                contentColor = if (isSelected) SelectedPurple
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Text(pill)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
