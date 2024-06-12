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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val iconView = splashScreenViewProvider.iconView
            val fadeOut = ObjectAnimator.ofFloat(iconView, "alpha", 1f, 0f)
            fadeOut.duration = 1000
            fadeOut.doOnEnd { splashScreenViewProvider.remove() }
            fadeOut.start()
        }

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
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, database: FirebaseDatabase) {
    val myRef: DatabaseReference = database.getReference("stops")

    val textField1 = remember { mutableStateOf("") }
    val textField2 = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    val options = listOf("Option 1", "Option 2", "Option 3")
    val context = LocalContext.current

    Scaffold(topBar = { KanarFinderTopBar() }, content = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(value = textField1.value,
                onValueChange = { textField1.value = it },
                label = { Text("Numer linii") },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = it }) {
                TextField(value = textField2.value,
                    label = { Text("Nazwa przystanku") },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                    modifier = Modifier
                        .menuAnchor()
                        .clickable { expanded.value = !expanded.value })
                ExposedDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }) {
                    options.forEach { item ->
                        DropdownMenuItem(text = { Text(text = item) }, onClick = {
                            textField2.value = item
                            expanded.value = false
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }

            Button(onClick = {
                val data = HashMap<String, Any>()
                data["lineNumber"] = textField1.value
                data["stopName"] = textField2.value
                data["timestamp"] = ServerValue.TIMESTAMP

                myRef.push().setValue(data).addOnSuccessListener {
                    Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
                    navController.navigate("main")
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Submit")
            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanarFinderTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "KanarFinder",
                color = Color.LightGray,
                fontWeight = FontWeight.ExtraBold,
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanarFinderApp(navController: NavController, stopsListFlow: StateFlow<List<Stop>>) {
    val localDatabase = LocalDatabase.getInstance(LocalContext.current)
    val knownStops = localDatabase.getTramStops()

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

                TramStopsList(tramStops = knownStops)
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
