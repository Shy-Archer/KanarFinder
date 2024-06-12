package com.example.kanarfinder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.kanarfinder.data.LocalDatabase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanarFinderTopBar() {
    var dropdownExpanded by remember { mutableStateOf(false) }

    TopAppBar(actions = {
        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Show dropdown", tint = MaterialTheme.colorScheme.onPrimary)
        }
        TopBarOptionsList(expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false })
    }, title = {
        Text(
            text = "KanarFinder",
        )
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    )
    )
}

@Composable
fun TopBarOptionsList(expanded: Boolean, onDismissRequest: () -> Unit) {
    val localDatabase = LocalDatabase.getInstance(LocalContext.current)

    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = { Text("Seed database") }, onClick = { localDatabase.seedData() })
        DropdownMenuItem(text = { Text("Nuke database") }, onClick = { localDatabase.nukeData() })
    }
}