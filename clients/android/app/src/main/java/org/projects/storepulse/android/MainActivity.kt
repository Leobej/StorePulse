package org.projects.storepulse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import org.projects.storepulse.android.ui.StorePulseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember { StorePulseAppGraph.createViewModel(applicationContext) }
            StorePulseApp(viewModel)
        }
    }
}
