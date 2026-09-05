package com.example.runts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.runts.ui.navigation.RuntsNavGraph
import com.example.runts.ui.theme.RuntsDarkBackground
import com.example.runts.ui.theme.RuntsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RuntsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RuntsDarkBackground
                ) {
                    RuntsNavGraph()
                }
            }
        }
    }
}
