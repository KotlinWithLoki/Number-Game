package com.loki.bottomnavigationbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.loki.bottomnavigationbar.ui.theme.BottomNavigationBarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomNavigationBarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()
                    ShowBottomNavigation(navController)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowBottomNavigation(navController: NavHostController) {

    val items = listOf(

        BottomNavItem(
            title = "Game",
            selectedIcon = Icons.Filled.PlayArrow,
            unselectedIcon = Icons.Outlined.PlayArrow,
            hashNews = false
        ),
        BottomNavItem(
            title = "Results",
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Outlined.List,
            hashNews = false,
//            badgeCount = 45
        ),
        BottomNavItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hashNews = false
        )

    )

    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            navController.navigate(item.title)
                                  },
                        label = { Text(text = item.title) },
                        alwaysShowLabel = false,
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount!=null){
                                        Badge {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    }else if (item.hashNews){
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = null
                                )
                            }
                        })
                }
            }
        }
    ){
        Navigation(navController = navController)
    }
}

@SuppressLint("RememberReturnType", "UnrememberedMutableState")
@Composable
fun Navigation(navController: NavHostController){

    val context  = LocalContext.current

    val scope = rememberCoroutineScope()

    val dataStore = StoreUserEmail(context)

    val saveData = dataStore.getTime.collectAsState(initial = "")
    val saveName = dataStore.getName.collectAsState(initial = "")

    val mList = mutableStateListOf<modelData>()

    val database = Firebase.database.reference


    NavHost(navController = navController, startDestination = "Game"){
        composable("Game"){
            HomeScreen(database, saveName.value, context)
        }
        composable("Results"){
            ChatScreen(mList, database)
        }
        composable("Settings"){
            SettingScreen(scope, dataStore, saveData.value.toString(), database, saveName.value, context)
        }
    }

}

@Composable
fun HomeScreen(databaseRef: DatabaseReference, saveName: String?, context: Context) {


    var numberScore by rememberSaveable {
        mutableStateOf("Loading...")
    }
    databaseRef.child("users").child("$saveName").child("score").get().addOnSuccessListener {
        numberScore = it.value.toString()
    }


    var clicked by rememberSaveable {
        mutableStateOf(true)
    }
    Row {
        Text(modifier = Modifier.padding(all = 18.dp), text = if (numberScore != "Loading..." && numberScore != "null") "Your Score: $numberScore Ball" else  if (numberScore == "Loading...") "Loading..." else "First, you need to complete the Settings", fontSize = 20.sp)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        Arrangement.End
    ) {
        IconButton(modifier = Modifier.padding(all = 18.dp),onClick = { clicked = false }) {
            Icon(Icons.Default.Refresh, "null")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        Arrangement.Center
    ) {
        if (clicked){
            Column(
                modifier = Modifier.fillMaxSize(),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                val random by rememberSaveable {
                    mutableStateOf((2..30).random().toString())
                }

                val randomForBtn by rememberSaveable {
                    mutableStateOf((1..3).random().toString())
                }

                var enableBtn by rememberSaveable {
                    mutableStateOf(true)
                }

                var checker by rememberSaveable {
                    mutableStateOf("first_form")
                }

                val color = if (checker == "first_form") Color.Black else if (checker == "second_form") Color.Green else Color.Red

                var screenNumber by rememberSaveable {
                    mutableStateOf("x")
                }

                val wrongNumber by rememberSaveable {
                    mutableStateOf((2..30).random().toString())
                }
                val wrongNumber2 by rememberSaveable {
                    mutableStateOf((2..30).random().toString())
                }
                val wrongNumber3 by rememberSaveable {
                    mutableStateOf((2..30).random().toString())
                }

                Text(text = screenNumber, color = color, fontSize = 30.sp)

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Row {

                    Button(enabled = enableBtn,modifier = Modifier.padding(horizontal = 3.dp),onClick = {
                        screenNumber = random
                        enableBtn = false
                        if (randomForBtn == "1"){
                            checker = "second_form"
                            if (numberScore != "null" && numberScore != "Loading..." && numberScore != "First, you need to complete the Settings") {
                                databaseRef.child("users").child("$saveName").child("score").setValue("${numberScore.toInt()+1}")
                                databaseRef.child("users").child("$saveName").child("score").get().addOnSuccessListener {
                                    numberScore = it.value.toString()
                                }
                            }else if(numberScore == "null" || numberScore == "Loading..." || numberScore == "First, you need to complete the Settings"){
                                enableBtn = false
                            } else {
                                Toast.makeText(context, "First, you need to complete the Settings", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            checker = "third_form"
                        }
                    }) {
                        if (randomForBtn == "1"){
                            Text(text = random)
                        }else {
                            Text(text = if (wrongNumber != random && wrongNumber2 != wrongNumber) wrongNumber else "${random.toInt() + 1}")
                        }
                    }

                    Button(enabled = enableBtn, modifier = Modifier.padding(horizontal = 3.dp), onClick = {
                        screenNumber = random
                        enableBtn = false
                        if (randomForBtn == "2"){
                            checker = "second_form"
                            if (numberScore != "null" && numberScore != "Loading..." && numberScore != "First, you need to complete the Settings"){
                                databaseRef.child("users").child("$saveName").child("score").setValue("${numberScore.toInt()+1}")
                                databaseRef.child("users").child("$saveName").child("score").get().addOnSuccessListener {
                                    numberScore = it.value.toString()
                                }
                            }else if(numberScore == "null" || numberScore == "Loading..." || numberScore == "First, you need to complete the Settings"){
                                enableBtn = false
                            } else {
                                Toast.makeText(context, "First, you need to complete the Settings", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            checker = "third_form"
                        }

                    }) {
                        if (randomForBtn == "2"){
                            Text(text = random)
                        }else {
                            Text(text = if (wrongNumber2 != random && wrongNumber2 != wrongNumber) wrongNumber2 else "${random.toInt() + 1}")
                        }
                    }

                    Button(enabled = enableBtn, modifier = Modifier.padding(horizontal = 3.dp), onClick = {
                        screenNumber = random
                        enableBtn = false
                        if (randomForBtn == "3"){
                            checker = "second_form"
                            if (numberScore != "null" && numberScore != "Loading..." && numberScore != "First, you need to complete the Settings") {
                                databaseRef.child("users").child("$saveName").child("score").setValue("${numberScore.toInt()+1}")
                                databaseRef.child("users").child("$saveName").child("score").get().addOnSuccessListener {
                                    numberScore = it.value.toString()
                                }
                            }else if(numberScore == "null" || numberScore == "Loading..." || numberScore == "First, you need to complete the Settings"){
                                enableBtn = false
                            } else {
                                Toast.makeText(context, "First, you need to complete the Settings", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            checker = "third_form"
                        }
                    }) {
                        if (randomForBtn == "3"){
                            Text(text = random)
                        }else {
                            Text(text = if (wrongNumber3 != random && wrongNumber2 != wrongNumber3 && wrongNumber != wrongNumber3) wrongNumber3 else "${random.toInt() - 1}")
                        }
                    }

                }
            }
        }else{
            clicked = true
        }

    }
}

@Composable
fun ChatScreen(mList: SnapshotStateList<modelData>, database: DatabaseReference) {

    database.child("users").addValueEventListener(object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            mList.clear()
            for (i in snapshot.children){
                val data = i.getValue(modelData::class.java)
                mList.add(modelData(data?.name, data?.score))
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        this.items(mList.sortedByDescending { it.score?.toInt() }){
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Text(modifier = Modifier.padding(start = 8.dp), text = "${it.name} : ${it.score} Ball")
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    scope: CoroutineScope,
    dataStore: StoreUserEmail,
    value: String,
    database: DatabaseReference,
    nameValue: String?,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {

        if (value == "pass") {
            Text(text = "You can't change it!")
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = nameValue.toString(),
                readOnly = true,
                onValueChange = {},
                maxLines = 1,
                placeholder = { Text(text = "Enter your username")})
        }else{
            var email by rememberSaveable {
                mutableStateOf("")
            }

            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = email,
                onValueChange = { email = it },
                maxLines = 1,
                placeholder = { Text(text = "Enter your username")})

            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            Button(onClick = {

                database.child("users").child(email).child("score").get().addOnSuccessListener {
                    if (it.value != null) {
                        Toast.makeText(
                            context,
                            "\n" +
                                    "You cannot choose this username because it already has an owner!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        database.child("users").child(email).setValue(modelData(email, "0"))
                        scope.launch {
                            dataStore.saveTime("pass")
                            dataStore.saveName(email)
                        }
                    }
                }

            }) {
                Text(text = "Ready!")
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BottomNavigationBarTheme {}
}