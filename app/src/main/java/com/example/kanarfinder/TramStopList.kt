package com.example.kanarfinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kanarfinder.data.LocalDatabase
import com.example.kanarfinder.domain.TramStop

@Composable
fun TramStopsList(tramStops: List<TramStop>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tramStops) { tramStop ->
            TramStopListItem(tramStop)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramStopListItem(
    tramStop: TramStop,
) {
    val dbContext = LocalDatabase.getInstance(LocalContext.current)
    var isStarred by rememberSaveable {
        mutableStateOf(dbContext.isStarred(tramStop))
    }

    val handleStarClick = {
        isStarred = !isStarred
        if (isStarred) {
            dbContext.insertStarredStop(tramStop)
        } else {
            dbContext.deleteStarredStop(tramStop)
        }
    }

    Card(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Linia: ${tramStop.lineNumber}", fontWeight = FontWeight.Bold)
                Text(text = "Przystanek: ${tramStop.topName}")
            }
            IconButton(onClick = handleStarClick) {
                val icon = if (isStarred) {
                    Icons.Outlined.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                }
                Icon(icon, contentDescription = "Add to favorites")
            }
        }
    }
}
