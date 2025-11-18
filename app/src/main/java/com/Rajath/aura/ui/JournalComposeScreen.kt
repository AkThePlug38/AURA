package com.Rajath.aura.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Rajath.aura.vm.JournalViewModel

@Composable
fun JournalComposeScreen(
    uid: String,
    onBack: () -> Unit,
    vm: JournalViewModel = viewModel()
) {
    JournalScreen(
        vm = vm,
        userId = uid
    )
}