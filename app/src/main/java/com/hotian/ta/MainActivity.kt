package com.hotian.ta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.hotian.ta.data.SettingsRepository
import com.hotian.ta.ui.navigation.AppNavigation
import com.hotian.ta.ui.theme.TaTheme
import com.hotian.ta.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        settingsRepository = SettingsRepository(this)

        setContent {
            val useDynamicColor by settingsRepository.useDynamicColor.collectAsState(initial = false)
            val navController = rememberNavController()

            TaTheme(useDynamicColor = useDynamicColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
