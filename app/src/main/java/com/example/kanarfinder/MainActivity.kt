package com.example.kanarfinder

import java.util.HashMap
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kanarfinder.data.LocalDatabase
import com.example.kanarfinder.domain.TramStop
import com.example.kanarfinder.ui.theme.KanarFinderTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ViewModelDelay>()
    private val stopsListFlow = MutableStateFlow<List<Stop>>(emptyList())

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase

        val databaseUrl =
            "https://kanarfinder-3f4ea-default-rtdb.europe-west1.firebasedatabase.app/"
        val database = Firebase.database(databaseUrl) // Initialize Firebase Database with URL

        val splashScreen = installSplashScreen()

        val myRef: DatabaseReference = database.getReference("stops")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newStopsList = mutableListOf<Stop>()
                for (snapshot in dataSnapshot.children) {
                    val stop = snapshot.getValue(Stop::class.java)
                    if (stop != null) {
                        newStopsList.add(stop)
                    }
                }
                stopsListFlow.value = newStopsList
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext, "Failed to read data: ${error.message}", Toast.LENGTH_SHORT
                ).show()
            }
        })

        setContent {
            KanarFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "main") {
                        composable("main") { KanarFinderApp(navController, stopsListFlow) }
                        composable("form") { FormScreen(navController, database) }
                        composable("tramDetails/{lineName}") { backStackEntry ->
                            val lineName = backStackEntry.arguments?.getString("lineName")
                            lineName?.let {
                                TramLinePage(navController, it, database)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ReadonlyTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {

    Box {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = onClick),
        )
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, database: FirebaseDatabase) {
    val localDatabase = LocalDatabase.getInstance(LocalContext.current)
    val context = LocalContext.current

    val tramLines = remember { mutableStateOf(listOf<String>()) }
    val stopNames = remember { mutableStateOf(listOf<String>()) }
    val selectedLine = remember { mutableStateOf("") }
    val selectedStop = remember { mutableStateOf("") }
    val lineDropdownExpanded = remember { mutableStateOf(false) }
    val stopDropdownExpanded = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        tramLines.value = localDatabase.getTramLines()
    }

    Scaffold(
        topBar = { KanarFinderTopBar() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ExposedDropdownMenuBox(
                    expanded = lineDropdownExpanded.value,
                    onExpandedChange = { lineDropdownExpanded.value = it }
                ) {
                    TextField(
                        value = selectedLine.value,
                        onValueChange = {},
                        label = { Text("Numer linii") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineDropdownExpanded.value) },
                        modifier = Modifier
                            .menuAnchor()
                            .clickable {
                                focusManager.clearFocus()
                                lineDropdownExpanded.value = !lineDropdownExpanded.value
                            }
                    )
                    ExposedDropdownMenu(
                        expanded = lineDropdownExpanded.value,
                        onDismissRequest = { lineDropdownExpanded.value = false }
                    ) {
                        tramLines.value.forEach { line ->
                            DropdownMenuItem(
                                text = { Text(text = line) },
                                onClick = {
                                    selectedLine.value = line
                                    lineDropdownExpanded.value = false
                                    // Update stop names when a new line is selected
                                    val stops = localDatabase.getStopNamesForLine(line)
                                    stopNames.value = stops.distinct() // Remove duplicates
                                    selectedStop.value = "" // Reset selected stop
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = stopDropdownExpanded.value,
                    onExpandedChange = { stopDropdownExpanded.value = it }
                ) {
                    TextField(
                        value = selectedStop.value,
                        onValueChange = {},
                        label = { Text("Nazwa przystanku") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stopDropdownExpanded.value) },
                        modifier = Modifier
                            .menuAnchor()
                            .clickable {
                                focusManager.clearFocus()
                                stopDropdownExpanded.value = !stopDropdownExpanded.value
                            }
                    )
                    ExposedDropdownMenu(
                        expanded = stopDropdownExpanded.value,
                        onDismissRequest = { stopDropdownExpanded.value = false }
                    ) {
                        stopNames.value.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text(text = stop) },
                                onClick = {
                                    selectedStop.value = stop
                                    stopDropdownExpanded.value = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val data = hashMapOf<String, Any>(
                        "lineNumber" to selectedLine.value,
                        "stopName" to selectedStop.value,
                        "timestamp" to ServerValue.TIMESTAMP
                    )

                    database.getReference("stops").push().setValue(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
                            navController.navigate("main")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }) {
                    Text("Submit")
                }
            }
        }
    )
}





fun filterStopsFromLast20Minutes(stops: List<Stop>): List<Stop> {
    val currentTime = System.currentTimeMillis()
    val twentyMinutesAgo = currentTime - TimeUnit.MINUTES.toMillis(20)

    return stops.filter { stop ->
        stop.timestamp?.let {
            it >= twentyMinutesAgo
        } ?: false
    }
}
fun getStopsForLine(database: FirebaseDatabase, lineName: String): StateFlow<List<Stop>> {
    val stopsForLineFlow = MutableStateFlow<List<Stop>>(emptyList())
    val myRef = database.getReference("stops")
    myRef.orderByChild("lineNumber").equalTo(lineName).addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val stopsList = mutableListOf<Stop>()
            for (snapshot in dataSnapshot.children) {
                val stop = snapshot.getValue(Stop::class.java)
                if (stop != null) {
                    stopsList.add(stop)
                }
            }
            stopsForLineFlow.value = stopsList
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
    return stopsForLineFlow
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanarFinderApp(navController: NavController, stopsListFlow: StateFlow<List<Stop>>) {
    val localDatabase = LocalDatabase.getInstance(LocalContext.current)
    val tramLines = localDatabase.getTramLines()

    val stopsList by stopsListFlow.collectAsState()
    val filteredStopsList = filterStopsFromLast20Minutes(stopsList)
    val textFieldValue = remember { mutableStateOf("") }
    Scaffold(topBar = { KanarFinderTopBar() }, content = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                TramLinesList(tramStops = tramLines,navController)
            }

            FloatingActionButton(
                onClick = { navController.navigate("form") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    })
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KanarFinderTheme {
        val navController = rememberNavController()
        // KanarFinderApp(navController)
    }
}