package com.example.kanarfinder

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kanarfinder.ui.theme.KanarFinderTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ViewModelDelay>()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val iconView = splashScreenViewProvider.iconView
            val fadeOut = ObjectAnimator.ofFloat(iconView, "alpha", 1f, 0f)
            fadeOut.duration = 1000
            fadeOut.doOnEnd { splashScreenViewProvider.remove() }
            fadeOut.start()
        }

        setContent {
            KanarFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "main") {
                        composable("main") { KanarFinderApp(navController) }
                        composable("form") { FormScreen(navController) }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController) {
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
                val textField1 = remember { mutableStateOf("") }
                val textField2 = remember { mutableStateOf("") }
                val expanded = remember { mutableStateOf(false) }
                val options = listOf("Option 1", "Option 2", "Option 3")
                val context = LocalContext.current

                TextField(
                    value = textField1.value,
                    onValueChange = { textField1.value = it },
                    label = { Text("Numer linii") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded.value,
                    onExpandedChange = { expanded.value = it }
                ) {
                    TextField(
                        value = textField2.value,
                        label = { Text("Nazwa przystanku") },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { expanded.value = !expanded.value }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        options.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    textField2.value = item
                                    expanded.value = false
                                    Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                Button(onClick = { navController.navigate("main") }) {
                    Text("Submit")
                }
            }
        }
    )
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanarFinderApp(navController: NavController) {
    val items = List(20) { "Item ${it + 1}" }
    val textFieldValue = remember { mutableStateOf("") }

    Scaffold(
        topBar = { KanarFinderTopBar() },
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Spacer to push the TextField down
                    Spacer(modifier = Modifier.height(60.dp))

                    TextField(
                        value = textFieldValue.value,
                        onValueChange = { textFieldValue.value = it },
                        label = { Text("Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { navController.navigate("main") }) {
                            Text("Submit")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp) // Padding from top to leave space
                            .height((LocalContext.current.resources.displayMetrics.heightPixels / 3.5).dp)

                    ) {
                        items(items) { item ->
                            Column {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()

                                        .clickable { /* Handle item click */ }
                                ) {
                                    Text(
                                        text = item,
                                        modifier = Modifier.padding(10.dp),

                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,


                                    )
                                    Text(
                                        text = "Subtext",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color.Gray,
                                        modifier = Modifier.absoluteOffset(x = 250.dp, y = 15.dp),
                                        style = MaterialTheme.typography.bodyLarge,

                                    )
                                    Text(
                                        text = "Subtext",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color.Gray,
                                        modifier = Modifier.absoluteOffset(x = 10.dp, y = -18.dp),
                                        style = MaterialTheme.typography.bodyLarge,

                                    )
                                }
                                Divider(
                                    color = Color.Gray,
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
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
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KanarFinderTheme {
        val navController = rememberNavController()
        KanarFinderApp(navController)
    }
}
