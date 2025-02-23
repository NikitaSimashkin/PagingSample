package ru.kram.pagingsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.defaultComponentContext
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.navigation.RootComponent
import ru.kram.pagingsample.ui.navigation.RootComponentImpl
import ru.kram.pagingsample.ui.navigation.RootContent

class MainActivity : ComponentActivity() {

    private lateinit var root: RootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        root = RootComponentImpl(defaultComponentContext())
        window.navigationBarColor = resources.getColor(R.color.background, theme)

        setContent {
            PagingSampleTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green)
                ) {
                    RootContent(root)
                }
            }
        }
    }
}
