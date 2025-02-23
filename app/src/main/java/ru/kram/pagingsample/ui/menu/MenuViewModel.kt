package ru.kram.pagingsample.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.kram.pagingsample.data.db.ServerDatabaseInitializer

class MenuViewModel(
    private val serverDatabaseInitializer: ServerDatabaseInitializer,
): ViewModel() {

    fun startInitServer() {
        viewModelScope.launch {
            serverDatabaseInitializer.init()
        }
    }
}