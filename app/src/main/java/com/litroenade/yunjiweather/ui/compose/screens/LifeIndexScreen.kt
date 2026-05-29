package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.model.LifeIndexItem
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.MessageCard
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.index.LifeIndexViewModel
import com.litroenade.yunjiweather.utils.LunarCalendarUtils

@Composable
fun LifeIndexScreen(
    modifier: Modifier = Modifier,
    respectStatusBar: Boolean = true,
    viewModel: LifeIndexViewModel = viewModel()
) {
    val indexItems by viewModel.getIndexItems().observeAsState(emptyList())
    val stateText by viewModel.getStateText().observeAsState("正在读取生活建议")
    val todayCalendar = remember { LunarCalendarUtils.today() }

    LazyColumn(
        modifier = modifier
            .then(if (respectStatusBar) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "生活建议",
                subtitle = stateText,
                action = {
                    OutlinedButton(onClick = viewModel::refresh) {
                        Text("刷新")
                    }
                }
            )
        }
        item {
            TodayCalendarCard(todayCalendar)
        }
        if (indexItems.isEmpty()) {
            item {
                MessageCard("暂无建议", "当前没有可展示的本地生活建议。", "刷新", viewModel::refresh)
            }
        } else {
            items(indexItems) { item ->
                LifeIndexCard(item)
            }
        }
    }
}

@Composable
private fun TodayCalendarCard(todayCalendar: LunarCalendarUtils.LunarDayInfo) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = todayCalendar.gregorianText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${todayCalendar.weekdayText} · ${todayCalendar.lunarText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = todayCalendar.festivalOrDefaultText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LifeIndexCard(item: LifeIndexItem) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = item.level,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = item.advice,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = item.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
