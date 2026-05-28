package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.mine.MineViewModel

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = viewModel()
) {
    val selectedTheme by viewModel.getVisualTheme().observeAsState(viewModel.getCurrentVisualTheme())
    val message by viewModel.getMessage().observeAsState("")
    val themes = remember(viewModel) { viewModel.getVisualThemes() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (message.isNotBlank() && !message.startsWith("主题/个性化")) {
            item {
                InfoCard {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            PersonalizationPanel(
                themes = themes,
                selectedTheme = selectedTheme,
                onThemeSelected = viewModel::setVisualTheme
            )
        }
    }
}
