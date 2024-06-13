package com.example.kanarfinder

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kanarfinder.data.LocalDatabase
import com.example.kanarfinder.domain.TramStop
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit
@Composable
fun TramLinesList(tramStops: List<String>,navController: NavController) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tramStops) { tramStop ->
            TramListListItem(tramStop,navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramListListItem(
    lineName: String,
    navController: NavController
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

    Card(onClick = { navController.navigate("tramDetails/$lineName") }, modifier = Modifier.fillMaxWidth()) {
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



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramLinePage(navController: NavController, lineName: String, database: FirebaseDatabase) {
    val stopsForLineFlow = getStopsForLine(database, lineName)
    val stopsForLine by stopsForLineFlow.collectAsState()

    // Filtrowanie przystanków z ostatnich 30 minut
    val filteredStopsForLine = stopsForLine.filter {
        val currentTime = System.currentTimeMillis()
        val thirtyMinutesAgo = currentTime - TimeUnit.MINUTES.toMillis(30)
        it.timestamp?.let { timestamp -> timestamp >= thirtyMinutesAgo } ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zgłoszenia dla Linii $lineName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredStopsForLine) { stop ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Przystanek: ${stop.stopName}")
                            Text(text = "Numer linii: ${stop.lineNumber}")
                            Text(text = "Czas: ${stop.getFormattedTimestamp()}")
                        }
                    }
                }
            }
        }
    )
}
