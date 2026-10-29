package com.billbot.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.billbot.app.BillBotApplication
import com.billbot.app.ui.screens.billing.CreateInvoiceScreen
import com.billbot.app.ui.screens.billing.CreateInvoiceViewModel
import com.billbot.app.ui.screens.customers.CustomerListScreen
import com.billbot.app.ui.screens.customers.CustomerListViewModel
import com.billbot.app.ui.screens.dashboard.DashboardScreen
import com.billbot.app.ui.screens.dashboard.DashboardViewModel
import com.billbot.app.ui.screens.invoices.InvoiceDetailScreen
import com.billbot.app.ui.screens.invoices.InvoiceDetailViewModel
import com.billbot.app.ui.screens.invoices.InvoiceListScreen
import com.billbot.app.ui.screens.invoices.InvoiceListViewModel
import com.billbot.app.ui.screens.products.ProductListScreen
import com.billbot.app.ui.screens.products.ProductListViewModel
import com.billbot.app.ui.screens.reports.ReportsScreen
import com.billbot.app.ui.screens.reports.ReportsViewModel
import com.billbot.app.ui.screens.settings.SettingsScreen
import com.billbot.app.ui.screens.settings.SettingsViewModel

@Composable
fun BillBotNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val app = context.applicationContext as BillBotApplication

    // Hide bottom bar on detail screen
    val showBottomBar = Screen.bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(
                        app.invoiceRepository,
                        app.productRepository,
                        app.settingsRepository
                    )
                )
                DashboardScreen(
                    viewModel = viewModel,
                    onCreateBillClick = { navController.navigate(Screen.Billing.route) },
                    onInvoiceClick = { invoiceId ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                    },
                    onViewAllInvoicesClick = { navController.navigate(Screen.Invoices.route) },
                    onManageInventoryClick = { navController.navigate(Screen.Products.route) }
                )
            }

            // Billing / POS
            composable(Screen.Billing.route) {
                val viewModel: CreateInvoiceViewModel = viewModel(
                    factory = CreateInvoiceViewModel.Factory(
                        app.invoiceRepository,
                        app.productRepository,
                        app.customerRepository,
                        app.settingsRepository
                    )
                )
                CreateInvoiceScreen(
                    viewModel = viewModel,
                    onInvoiceCreated = { invoiceId ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId)) {
                            popUpTo(Screen.Billing.route) { inclusive = true }
                        }
                    }
                )
            }

            // Invoices History
            composable(Screen.Invoices.route) {
                val viewModel: InvoiceListViewModel = viewModel(
                    factory = InvoiceListViewModel.Factory(
                        app.invoiceRepository,
                        app.settingsRepository
                    )
                )
                InvoiceListScreen(
                    viewModel = viewModel,
                    onInvoiceClick = { invoiceId ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                    },
                    onCreateBillClick = { navController.navigate(Screen.Billing.route) }
                )
            }

            // Products
            composable(Screen.Products.route) {
                val viewModel: ProductListViewModel = viewModel(
                    factory = ProductListViewModel.Factory(
                        app.productRepository,
                        app.settingsRepository
                    )
                )
                ProductListScreen(viewModel = viewModel)
            }

            // Customers
            composable(Screen.Customers.route) {
                val viewModel: CustomerListViewModel = viewModel(
                    factory = CustomerListViewModel.Factory(
                        app.customerRepository,
                        app.settingsRepository
                    )
                )
                CustomerListScreen(
                    viewModel = viewModel,
                    onInvoiceClick = { invoiceId ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                    }
                )
            }

            // Reports
            composable(Screen.Reports.route) {
                val viewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(
                        app.invoiceRepository,
                        app.settingsRepository
                    )
                )
                ReportsScreen(viewModel = viewModel)
            }

            // Settings
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(app.settingsRepository)
                )
                SettingsScreen(viewModel = viewModel)
            }

            // Invoice Details
            composable(
                route = Screen.InvoiceDetail.route,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
                val viewModel: InvoiceDetailViewModel = viewModel(
                    factory = InvoiceDetailViewModel.Factory(
                        invoiceId,
                        app.invoiceRepository,
                        app.settingsRepository,
                        app.pdfGenerator
                    )
                )
                InvoiceDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
