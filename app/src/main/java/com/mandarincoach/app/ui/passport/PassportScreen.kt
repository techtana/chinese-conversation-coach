package com.mandarincoach.app.ui.passport

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.model.CompletionRecord
import com.mandarincoach.app.data.model.Scenario
import com.mandarincoach.app.data.model.StreakState
import com.mandarincoach.app.data.model.WeekAggregate
import com.mandarincoach.app.data.preferences.ProgressRepository
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.data.repository.ScenarioRepository
import com.mandarincoach.app.domain.LatencyStats
import com.mandarincoach.app.domain.VocabularyWealthCalculator
import com.mandarincoach.app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PassportScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val progressRepo = remember { ProgressRepository(context) }

    val completions by progressRepo.scenarioCompletions.collectAsState(initial = emptyMap())
    val streak by progressRepo.streakState.collectAsState(initial = StreakState())
    val latency by progressRepo.latencyWeekly.collectAsState(initial = emptyMap())
    val learnedWords by prefs.learnedWords.collectAsState(initial = emptySet())

    val wealth = remember(learnedWords) { VocabularyWealthCalculator.wealth(learnedWords) }
    val today = remember { LocalDate.now() }
    val thisWeek = latency[LatencyStats.weekKey(today)]
    val lastWeek = latency[LatencyStats.weekKey(today.minusWeeks(1))]

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = { Text("Fluency Passport · 语言护照", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWarm)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Passport cover header
            Card(
                colors = CardDefaults.cardColors(containerColor = ChineseRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("护照", fontSize = 36.sp, fontFamily = FontFamily.Serif, color = GoldAccent)
                    Text(
                        "FLUENCY PASSPORT",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        letterSpacing = 3.sp
                    )
                }
            }

            // Stats
            StatCard(title = "Streak · 连续天数") {
                StatRow("🔥 Current streak", "${streak.currentStreak} day${if (streak.currentStreak == 1) "" else "s"}")
                StatRow("🏆 Longest streak", "${streak.longestStreak} day${if (streak.longestStreak == 1) "" else "s"}")
            }

            StatCard(title = "Vocabulary Wealth · 词汇资产") {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$wealth",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChineseRed
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "wealth from ${learnedWords.size} words",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "Words are weighted by difficulty — rarer words are worth more.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            StatCard(title = "Response Speed · 反应速度") {
                LatencyContent(thisWeek, lastWeek)
            }

            // Stamps
            Text(
                text = "Stamps · 印章",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                ScenarioRepository.scenarios.forEach { scenario ->
                    StampCard(
                        scenario = scenario,
                        completion = completions[scenario.id],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LatencyContent(thisWeek: WeekAggregate?, lastWeek: WeekAggregate?) {
    if (thisWeek == null && lastWeek == null) {
        Text(
            text = "Reply to the coach a few times and your week-over-week speed shows up here.",
            style = MaterialTheme.typography.bodySmall,
            color = TextHint
        )
        return
    }

    val thisAvg = thisWeek?.averageMs
    val lastAvg = lastWeek?.averageMs
    thisAvg?.let {
        StatRow("This week (avg reply)", formatMs(it))
    }
    lastAvg?.let {
        StatRow("Last week (avg reply)", formatMs(it))
    }
    if (thisAvg != null && lastAvg != null && lastAvg > 0) {
        val faster = thisAvg < lastAvg
        val deltaMs = if (faster) lastAvg - thisAvg else thisAvg - lastAvg
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (faster) "▼ ${formatMs(deltaMs)} faster than last week — fluency is automating."
            else "▲ ${formatMs(deltaMs)} slower than last week — probably tackling harder ground.",
            style = MaterialTheme.typography.bodySmall,
            color = if (faster) TipColor else TextSecondary
        )
    }
}

private fun formatMs(ms: Long): String =
    if (ms >= 1000) "%.1fs".format(ms / 1000.0) else "${ms}ms"

@Composable
private fun StatCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ChineseRed,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = ChineseRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StampCard(
    scenario: Scenario,
    completion: CompletionRecord?,
    modifier: Modifier = Modifier
) {
    val earned = completion != null
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (earned) SurfaceWhite else SurfaceGray.copy(alpha = 0.6f)
        ),
        border = if (earned) BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)) else null,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (earned) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (earned) scenario.emoji else "🔒",
                fontSize = 28.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = scenario.stampTitle,
                style = MaterialTheme.typography.labelMedium,
                color = if (earned) TextPrimary else TextHint,
                fontWeight = FontWeight.Medium
            )
            if (completion != null) {
                Spacer(Modifier.height(2.dp))
                val date = Instant.ofEpochMilli(completion.epochMillis)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
            }
        }
    }
}
