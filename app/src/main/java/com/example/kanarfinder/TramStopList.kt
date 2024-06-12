package com.example.kanarfinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
fun TramLinesList(tramStops: List<String>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tramStops) { tramStop ->
            TramListListItem(tramStop)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramListListItem(
    lineName: String
) {
    val dbContext = LocalDatabase.getInstance(LocalContext.current)
    var isStarred by rememberSaveable {
        mutableStateOf(dbContext.isStarred(lineName))
    }

    val handleStarClick = {
        isStarred = !isStarred
        if (isStarred) {
            dbContext.insertStarredStop(lineName)
        } else {
            dbContext.deleteStarredStop(lineName)
        }
    }

    Card(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Linia: $lineName", fontWeight = FontWeight.Bold)
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
