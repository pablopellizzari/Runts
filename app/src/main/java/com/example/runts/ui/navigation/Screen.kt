package com.example.runts.ui.navigation

sealed class Screen(val route: String, val title: String = "") {
    object Login : Screen("login", "Login")
    object RegisterAccount : Screen("register_account", "Criar Conta")
    object ForgotPassword : Screen("forgot_password", "Recuperar Senha")
    object AthleteDashboard : Screen("athlete_dashboard", "Início")
    object WorkoutDetail : Screen("workout_detail/{workoutId}", "Treinos") {
        fun createRoute(workoutId: String) = "workout_detail/$workoutId"
    }
    object PrescribedVsExecuted : Screen("prescribed_vs_executed/{workoutId}", "Treinos") {
        fun createRoute(workoutId: String) = "prescribed_vs_executed/$workoutId"
    }
    object RaceCalendar : Screen("race_calendar", "Provas")
    object AddRace : Screen("add_race/{raceId}", "Prova") {
        fun createRoute(raceId: String? = null) = "add_race/${raceId ?: "new"}"
    }
    object AthleteLink : Screen("athlete_link", "Vincular")
    object WorkoutLog : Screen("workout_log/{workoutId}", "Registrar") {
        fun createRoute(workoutId: String) = "workout_log/$workoutId"
    }
    object AthletePlan : Screen("athlete_plan", "Planilhas")
    object AthleteStats : Screen("athlete_stats", "Estatísticas")
    object Integrations : Screen("integrations", "Integrações")
    object Profile : Screen("profile", "Perfil")
}
