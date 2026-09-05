package com.example.runts.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runts.domain.model.User
import com.example.runts.domain.model.UserType
import com.example.runts.ui.components.BottomNavigationBar
import com.example.runts.ui.screens.AddRaceScreen
import com.example.runts.ui.screens.AthleteDashboardScreen
import com.example.runts.ui.screens.AthleteLinkScreen
import com.example.runts.ui.screens.AthletePlanScreen
import com.example.runts.ui.screens.AthleteStatsScreen
import com.example.runts.ui.screens.ForgotPasswordScreen
import com.example.runts.ui.screens.IntegrationsScreen
import com.example.runts.ui.screens.LoginScreen
import com.example.runts.ui.screens.PrescribedVsExecutedScreen
import com.example.runts.ui.screens.ProfileScreen
import com.example.runts.ui.screens.RaceCalendarScreen
import com.example.runts.ui.screens.RegisterAccountScreen
import com.example.runts.ui.screens.WorkoutDetailScreen
import com.example.runts.ui.screens.WorkoutLogScreen
import com.example.runts.ui.viewmodel.AthleteDashboardViewModel
import com.example.runts.ui.viewmodel.AthleteStatsViewModel
import com.example.runts.ui.viewmodel.AuthViewModel
import com.example.runts.ui.viewmodel.RaceCalendarViewModel
import com.example.runts.ui.viewmodel.WorkoutDetailViewModel
import com.example.runts.ui.viewmodel.WorkoutLogViewModel

@Composable
fun RuntsNavGraph(
    navController: NavHostController = rememberNavController()
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentUserId by remember { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.AthleteDashboard.route,
        Screen.AthletePlan.route,
        Screen.RaceCalendar.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.AthleteDashboard.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { user ->
                        currentUser = user
                        currentUserId = user.id
                        navController.navigate(Screen.AthleteDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.RegisterAccount.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.RegisterAccount.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                RegisterAccountScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = { user ->
                        currentUser = user
                        currentUserId = user.id
                        navController.navigate(Screen.AthleteDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AthleteDashboard.route) {
                val dashboardViewModel: AthleteDashboardViewModel = hiltViewModel()
                LaunchedEffect(currentUserId) {
                    dashboardViewModel.loadDashboard(currentUserId)
                }
                AthleteDashboardScreen(
                    viewModel = dashboardViewModel,
                    currentUser = currentUser,
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                    }
                )
            }

            composable(Screen.WorkoutDetail.route) { backStackEntry ->
                val workoutId = requireNotNull(backStackEntry.arguments?.getString("workoutId"))
                val workoutDetailViewModel: WorkoutDetailViewModel = hiltViewModel()
                LaunchedEffect(workoutId) {
                    workoutDetailViewModel.loadWorkoutDetail(workoutId)
                }
                WorkoutDetailScreen(
                    viewModel = workoutDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onMarkCompletedClick = {
                        navController.navigate(Screen.WorkoutLog.createRoute(workoutId))
                    },
                    onViewComparisonClick = {
                        navController.navigate(Screen.PrescribedVsExecuted.createRoute(workoutId))
                    }
                )
            }

            composable(Screen.WorkoutLog.route) { backStackEntry ->
                val workoutId = requireNotNull(backStackEntry.arguments?.getString("workoutId"))
                val workoutLogViewModel: WorkoutLogViewModel = hiltViewModel()
                WorkoutLogScreen(
                    viewModel = workoutLogViewModel,
                    athleteId = currentUserId,
                    prescribedWorkoutId = workoutId,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.navigate(Screen.PrescribedVsExecuted.createRoute(workoutId))
                    }
                )
            }

            composable(Screen.PrescribedVsExecuted.route) { backStackEntry ->
                val workoutId = requireNotNull(backStackEntry.arguments?.getString("workoutId"))
                val comparisonViewModel: WorkoutDetailViewModel = hiltViewModel()
                LaunchedEffect(workoutId) { comparisonViewModel.loadWorkoutDetail(workoutId) }
                PrescribedVsExecutedScreen(
                    viewModel = comparisonViewModel,
                    onBack = { navController.navigate(Screen.AthleteDashboard.route) }
                )
            }

            composable(Screen.RaceCalendar.route) {
                val raceViewModel: RaceCalendarViewModel = hiltViewModel()
                LaunchedEffect(currentUserId) {
                    raceViewModel.loadRaces(currentUserId)
                }
                RaceCalendarScreen(
                    viewModel = raceViewModel,
                    onAddRaceClick = { navController.navigate(Screen.AddRace.createRoute()) },
                    onRaceClick = { raceId -> navController.navigate(Screen.AddRace.createRoute(raceId)) }
                )
            }

            composable(Screen.AthletePlan.route) {
                val planViewModel: AthleteDashboardViewModel = hiltViewModel()
                LaunchedEffect(currentUserId) {
                    planViewModel.loadDashboard(currentUserId)
                }
                AthletePlanScreen(
                    viewModel = planViewModel,
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                    }
                )
            }

            composable(Screen.AddRace.route) { backStackEntry ->
                val raceViewModel: RaceCalendarViewModel = hiltViewModel()
                val raceId = backStackEntry.arguments?.getString("raceId")?.takeUnless { it == "new" }
                AddRaceScreen(
                    viewModel = raceViewModel,
                    athleteId = currentUserId,
                    raceId = raceId,
                    onClose = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(Screen.AthleteLink.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                AthleteLinkScreen(
                    authViewModel = authViewModel,
                    athleteId = currentUserId,
                    onBack = { navController.popBackStack() },
                    onLinkSuccess = { navController.popBackStack() }
                )
            }

            composable(Screen.AthleteStats.route) {
                val statsViewModel: AthleteStatsViewModel = hiltViewModel()
                LaunchedEffect(currentUserId) {
                    statsViewModel.loadStats(currentUserId)
                }
                AthleteStatsScreen(
                    viewModel = statsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Integrations.route) {
                IntegrationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    currentUser = currentUser,
                    userType = UserType.ATHLETE,
                    onNavigateToStats = { navController.navigate(Screen.AthleteStats.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.RaceCalendar.route) },
                    onNavigateToIntegrations = { navController.navigate(Screen.Integrations.route) },
                    onNavigateToLinkCoach = { navController.navigate(Screen.AthleteLink.route) },
                    onLogout = {
                        currentUser = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
