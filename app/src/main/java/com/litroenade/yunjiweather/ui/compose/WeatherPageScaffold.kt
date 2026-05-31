package com.litroenade.yunjiweather.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens

@Composable
internal fun WeatherPageScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    immersive: Boolean = false,
    backIconResId: Int = R.drawable.ic_arrow_back_24,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    action: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val resolvedTitleColor = titleColor ?: MaterialTheme.colorScheme.onBackground
    val resolvedSubtitleColor = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    if (immersive) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(visualTheme.background)
                .navigationBarsPadding()
        ) {
            content(Modifier.fillMaxSize())
            WeatherPageHeader(
                title = title,
                subtitle = subtitle,
                titleColor = resolvedTitleColor,
                subtitleColor = resolvedSubtitleColor,
                backIconResId = backIconResId,
                onBack = onBack,
                action = action,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(visualTheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            WeatherPageHeader(
                title = title,
                subtitle = subtitle,
                titleColor = resolvedTitleColor,
                subtitleColor = resolvedSubtitleColor,
                backIconResId = backIconResId,
                onBack = onBack,
                action = action
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                content(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun WeatherPageHeader(
    title: String,
    subtitle: String?,
    titleColor: Color,
    subtitleColor: Color,
    backIconResId: Int,
    onBack: () -> Unit,
    action: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier
) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = YunJiUiTokens.PageHeaderHorizontalPadding,
                    vertical = YunJiUiTokens.PageHeaderVerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.size(YunJiUiTokens.PageHeaderIconButtonSize),
                onClick = onBack
            ) {
                Icon(
                    modifier = Modifier.size(YunJiUiTokens.PageHeaderIconSize),
                    painter = painterResource(backIconResId),
                    contentDescription = "返回",
                    tint = titleColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = YunJiUiTokens.PageHeaderTitleSize,
                    lineHeight = YunJiUiTokens.PageHeaderTitleLineHeight,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            action?.invoke()
        }
}
