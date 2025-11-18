package com.Rajath.aura.nav

sealed class NavRoutes(val route: String) {
    object Auth : NavRoutes("login")
    object NameEntry : NavRoutes("name_entry")
    object Home : NavRoutes("home")
    object JournalCompose : NavRoutes("journal_compose") {
        fun createRoute(uid: String) = "journal_compose/$uid"
    }
    object JournalList : NavRoutes("journal_list")
    object Analytics : NavRoutes("analytics")
    object Meditate : NavRoutes("meditate")
}