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
import com.litroenade.yunjiweather.utils.HomeBlock

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = viewModel()
) {
    val selectedTheme by viewModel.getVisualTheme().observeAsState(viewModel.getCurrentVisualTheme())
    val selectedThemeStyle by viewModel.getVisualThemeStyle().observeAsState(viewModel.getCurrentVisualThemeStyle())
    val homeBlockOrder by viewModel.getHomeBlockOrder().observeAsState(HomeBlock.defaultOrder())
    val homeBlockEnabled by viewModel.getHomeBlockEnabled().observeAsState(emptyMap())
    val message by viewModel.getMessage().observeAsState("")
    val themes = remember(viewModel) { viewModel.getVisualThemes() }
    val themeStyles = remember(viewModel) { viewModel.getVisualThemeStyles() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (message.isNotBlank()) {
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
            InfoCard {
                PersonalizationPanel(
                    themes = themes,
                    themeStyles = themeStyles,
                    selectedTheme = selectedTheme,
                    selectedThemeStyle = selectedThemeStyle,
                    homeBlockOrder = homeBlockOrder,
                    homeBlockEnabled = homeBlockEnabled,
                    onThemeSelected = viewModel::setVisualTheme,
                    onStyleSelected = viewModel::setVisualThemeStyle,
                    onHomeBlockEnabledChange = viewModel::setHomeBlockEnabled,
                    onMoveHomeBlockUp = viewModel::moveHomeBlockUp,
                    onMoveHomeBlockDown = viewModel::moveHomeBlockDown,
                    onResetHomeBlocks = viewModel::resetHomeBlockLayout
                )
            }
        }
    }
}
