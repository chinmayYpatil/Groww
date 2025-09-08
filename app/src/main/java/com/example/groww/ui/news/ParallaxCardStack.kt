package com.example.groww.ui.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.groww.data.model.network.Article

@Composable
fun ParallaxNewsList(
    modifier: Modifier = Modifier,
    news: List<Article>,
    scrollState: LazyListState,
    onNewsClick: (String) -> Unit,
    newsCardContent: @Composable (Article, () -> Unit, Modifier) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(news) { index, article ->
            val parallaxModifier = calculateParallaxModifier(
                index = index,
                scrollState = scrollState
            )

            newsCardContent(
                article,
                { onNewsClick(article.url) },
                Modifier
                    .fillMaxWidth()
                    .then(parallaxModifier)
            )
        }
    }
}

@Composable
private fun calculateParallaxModifier(
    index: Int,
    scrollState: LazyListState
): Modifier {
    // Get the current first visible item index
    val firstVisibleItemIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    // Check if this is the currently visible item
    val isCurrentItem by remember {
        derivedStateOf { index == firstVisibleItemIndex }
    }

    // Calculate the parallax offset based on scroll position
    val offset by remember {
        derivedStateOf {
            if (isCurrentItem) {
                scrollState.firstVisibleItemScrollOffset.toFloat()
            } else {
                0f
            }
        }
    }

    // Calculate the scale effect based on scroll progress
    val scale by remember {
        derivedStateOf {
            val visibleItemInfo = scrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            val itemSize = visibleItemInfo?.size?.toFloat() ?: 1f

            if (isCurrentItem && itemSize > 0) {
                val progress = offset / itemSize
                // Scale down slightly as the item scrolls up
                1f - (progress * 0.05f).coerceIn(0f, 0.1f)
            } else {
                1f
            }
        }
    }

    // Calculate translation Y for parallax effect
    val translationY by remember {
        derivedStateOf {
            if (isCurrentItem) {
                // Create a subtle parallax effect by moving the item slower than scroll
                offset * 0.3f
            } else {
                0f
            }
        }
    }

    return Modifier.graphicsLayer {
        this.translationY = translationY
        this.scaleX = scale
        this.scaleY = scale
        // Add a subtle alpha effect for items going out of view
        this.alpha = if (isCurrentItem) {
            val visibleItemInfo = scrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            val itemSize = visibleItemInfo?.size?.toFloat() ?: 1f
            if (itemSize > 0) {
                val progress = offset / itemSize
                (1f - progress * 0.3f).coerceIn(0.7f, 1f)
            } else {
                1f
            }
        } else {
            1f
        }
    }
}

// Alternative simpler parallax effect for better performance
@Composable
fun SimpleParallaxNewsList(
    modifier: Modifier = Modifier,
    news: List<Article>,
    scrollState: LazyListState,
    onNewsClick: (String) -> Unit,
    newsCardContent: @Composable (Article, () -> Unit, Modifier) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(news) { index, article ->
            val parallaxModifier = remember(index, scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
                if (index == scrollState.firstVisibleItemIndex) {
                    val offset = scrollState.firstVisibleItemScrollOffset.toFloat()
                    Modifier.graphicsLayer {
                        translationY = offset * 0.2f
                        scaleX = 1f - (offset * 0.00005f).coerceIn(0f, 0.05f)
                        scaleY = 1f - (offset * 0.00005f).coerceIn(0f, 0.05f)
                    }
                } else {
                    Modifier
                }
            }

            newsCardContent(
                article,
                { onNewsClick(article.url) },
                Modifier
                    .fillMaxWidth()
                    .then(parallaxModifier)
            )
        }
    }
}