@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.sosauce.cutemusic.ui.screens.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.sosauce.cutemusic.R
import com.sosauce.cutemusic.data.actions.PlayerActions
import com.sosauce.cutemusic.domain.model.Artist
import com.sosauce.cutemusic.ui.navigation.Screen
import com.sosauce.cutemusic.ui.screens.main.components.SortingDropdownMenu
import com.sosauce.cutemusic.ui.shared_components.CuteActionButton
import com.sosauce.cutemusic.ui.shared_components.CuteSearchbar
import com.sosauce.cutemusic.ui.shared_components.CuteText
import com.sosauce.cutemusic.utils.SharedTransitionKeys
import com.sosauce.cutemusic.utils.rememberSearchbarAlignment
import com.sosauce.cutemusic.utils.showCuteSearchbar

@Composable
fun SharedTransitionScope.ArtistsScreen(
    artist: List<Artist>,
    currentlyPlaying: String,
    onNavigate: (Screen) -> Unit,
    isPlaying: Boolean,
    onHandlePlayerActions: (PlayerActions) -> Unit,
    isPlayerReady: Boolean,
) {

    var query by remember { mutableStateOf("") }
    var isSortedByASC by rememberSaveable { mutableStateOf(true) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val state = rememberLazyListState()
    val displayArtists by remember(isSortedByASC, artist, query) {
        derivedStateOf {
            if (query.isNotEmpty()) {
                artist.filter {
                    it.name.contains(
                        other = query,
                        ignoreCase = true
                    )
                }
            } else {
                if (isSortedByASC) artist
                else artist.sortedByDescending { it.name }
            }

        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box {
            if (displayArtists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CuteText(
                        text = stringResource(id = R.string.no_artists_found),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = paddingValues,
                    state = state
                ) {
                    items(
                        items = displayArtists,
                        key = { it.id }
                    ) { artist ->
                        Column(
                            modifier = Modifier
                                .animateItem()
                                .padding(
                                    vertical = 2.dp,
                                    horizontal = 4.dp
                                )
                        ) {
                            ArtistInfoList(
                                artist = artist
                            ) { onNavigate(Screen.ArtistsDetails(artist.id)) }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = state.showCuteSearchbar,
                modifier = Modifier.align(rememberSearchbarAlignment()),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                CuteSearchbar(
                    query = query,
                    onQueryChange = { query = it },
                    isScrolling = state.isScrollInProgress,
                    trailingIcon = {
                        IconButton(
                            onClick = { sortMenuExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = stringResource(R.string.sort)
                            )
                        }
                        SortingDropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            isSortedByASC = isSortedByASC,
                            onChangeSorting = { isSortedByASC = it }
                        )
                    },
                    currentlyPlaying = currentlyPlaying,
                    onHandlePlayerActions = onHandlePlayerActions,
                    isPlaying = isPlaying,
                    isPlayerReady = isPlayerReady,
                    onNavigate = onNavigate,
                    fab = {
                        CuteActionButton(
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = SharedTransitionKeys.FAB),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                        ) { onHandlePlayerActions(PlayerActions.PlayRandom) }
                    }
                )
            }
        }
    }

}


@Composable
fun SharedTransitionScope.ArtistInfoList(
    modifier: Modifier = Modifier,
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = artist.id),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    )
                    .size(45.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFAB3AA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.artist_rounded),
                    contentDescription = stringResource(id = R.string.artwork),
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                CuteText(
                    text = artist.name,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = artist.name + artist.id),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                )

            }
        }
    }
}
