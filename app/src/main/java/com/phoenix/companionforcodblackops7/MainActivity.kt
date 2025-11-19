package com.phoenix.companionforcodblackops7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phoenix.companionforcodblackops7.core.domain.repository.IconsRepository
import com.phoenix.companionforcodblackops7.core.ui.theme.BlackOps7Theme
import com.phoenix.companionforcodblackops7.core.ui.components.NoInternetDialog
import com.phoenix.companionforcodblackops7.core.util.NetworkMonitor
import com.phoenix.companionforcodblackops7.core.util.rememberNetworkState
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.presentation.CategoryChecklistScreen
import com.phoenix.companionforcodblackops7.feature.checklist.presentation.ChecklistOverviewScreen
import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.model.GameMode
import com.phoenix.companionforcodblackops7.feature.gamemodes.presentation.GameModeDetailScreen
import com.phoenix.companionforcodblackops7.feature.gamemodes.presentation.GameModesListScreen
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.presentation.MapCategoriesScreen
import com.phoenix.companionforcodblackops7.feature.maps.presentation.MapDetailScreen
import com.phoenix.companionforcodblackops7.feature.maps.presentation.MapListScreen
import com.phoenix.companionforcodblackops7.feature.maps.presentation.MapViewerScreen
import com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator
import com.phoenix.companionforcodblackops7.feature.operators.presentation.OperatorDetailsScreen
import com.phoenix.companionforcodblackops7.feature.operators.presentation.OperatorsScreen
import com.phoenix.companionforcodblackops7.feature.sync.presentation.SyncScreen
import com.phoenix.companionforcodblackops7.feature.campaignmultiplayer.presentation.CampaignMultiplayerHubScreen
import com.phoenix.companionforcodblackops7.feature.combatspecialties.presentation.CombatSpecialtiesListScreen
import com.phoenix.companionforcodblackops7.feature.perks.domain.model.Perk
import com.phoenix.companionforcodblackops7.feature.perks.presentation.PerkDetailScreen
import com.phoenix.companionforcodblackops7.feature.perks.presentation.PerksListScreen
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.model.Scorestreak
import com.phoenix.companionforcodblackops7.feature.scorestreaks.presentation.ScorestreakDetailScreen
import com.phoenix.companionforcodblackops7.feature.scorestreaks.presentation.ScorestreaksListScreen
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.model.Tactical
import com.phoenix.companionforcodblackops7.feature.tacticals.presentation.TacticalDetailScreen
import com.phoenix.companionforcodblackops7.feature.tacticals.presentation.TacticalsListScreen
import com.phoenix.companionforcodblackops7.feature.lethals.domain.model.Lethal
import com.phoenix.companionforcodblackops7.feature.lethals.presentation.LethalDetailScreen
import com.phoenix.companionforcodblackops7.feature.lethals.presentation.LethalsListScreen
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.model.FieldUpgrade
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.presentation.FieldUpgradeDetailScreen
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.presentation.FieldUpgradesListScreen
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.model.PerkACola
import com.phoenix.companionforcodblackops7.feature.perkacola.presentation.PerkAColaDetailScreen
import com.phoenix.companionforcodblackops7.feature.perkacola.presentation.PerkAColaListScreen
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.model.AmmoMod
import com.phoenix.companionforcodblackops7.feature.ammomods.presentation.AmmoModDetailScreen
import com.phoenix.companionforcodblackops7.feature.ammomods.presentation.AmmoModsListScreen
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model.FieldUpgradeZM
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.presentation.FieldUpgradeZMDetailScreen
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.presentation.FieldUpgradesZMListScreen
import com.phoenix.companionforcodblackops7.feature.powerups.presentation.PowerUpsScreen
import com.phoenix.companionforcodblackops7.feature.zombiehub.presentation.ZombieHubScreen
import com.phoenix.companionforcodblackops7.feature.wildcards.presentation.WildcardsListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var iconsRepository: IconsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlackOps7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConnectivityWrapper(
                        networkMonitor = networkMonitor,
                        onExit = {
                            finish()
                            exitProcess(0)
                        }
                    ) {
                        AppNavigation(
                            networkMonitor = networkMonitor,
                            iconsRepository = iconsRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectivityWrapper(
    networkMonitor: NetworkMonitor,
    onExit: () -> Unit,
    content: @Composable () -> Unit
) {
    val isConnected by rememberNetworkState(networkMonitor)
    var showDialog by remember { mutableStateOf(!isConnected) }

    LaunchedEffect(isConnected) {
        showDialog = !isConnected
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showDialog) {
            NoInternetDialog(
                onRetry = {
                    if (networkMonitor.isConnected()) {
                        showDialog = false
                    }
                },
                onExit = onExit
            )
        }
    }
}

@Composable
fun AppNavigation(
    networkMonitor: NetworkMonitor,
    iconsRepository: IconsRepository
) {
    val navController = rememberNavController()

    // State to hold selected operator and iconMap for navigation to details
    var selectedOperator by remember { mutableStateOf<Operator?>(null) }
    var iconMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // State to hold selected map category and map for navigation
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedMap by remember { mutableStateOf<GameMap?>(null) }

    // State to hold selected game mode for navigation
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }

    // State to hold selected perk for navigation
    var selectedPerk by remember { mutableStateOf<Perk?>(null) }

    // State to hold selected scorestreak for navigation
    var selectedScorestreak by remember { mutableStateOf<Scorestreak?>(null) }

    // State to hold selected tactical for navigation
    var selectedTactical by remember { mutableStateOf<Tactical?>(null) }

    // State to hold selected lethal for navigation
    var selectedLethal by remember { mutableStateOf<Lethal?>(null) }

    // State to hold selected field upgrade for navigation
    var selectedFieldUpgrade by remember { mutableStateOf<FieldUpgrade?>(null) }

    // State to hold selected Perk-a-Cola for navigation
    var selectedPerkACola by remember { mutableStateOf<PerkACola?>(null) }

    // State to hold selected Ammo Mod for navigation
    var selectedAmmoMod by remember { mutableStateOf<AmmoMod?>(null) }

    // State to hold selected Field Upgrade (ZM) for navigation
    var selectedFieldUpgradeZM by remember { mutableStateOf<FieldUpgradeZM?>(null) }

    NavHost(
        navController = navController,
        startDestination = "sync"
    ) {
        composable("sync") {
            SyncScreen(
                networkMonitor = networkMonitor,
                onSyncComplete = {
                    navController.navigate("home") {
                        popUpTo("sync") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToOperators = {
                    navController.navigate("operators")
                },
                onNavigateToChecklists = {
                    navController.navigate("checklists")
                },
                onNavigateToMaps = {
                    navController.navigate("maps")
                },
                onNavigateToGameModes = {
                    navController.navigate("gameModes")
                },
                onNavigateToCampaignMultiplayer = {
                    navController.navigate("campaignMultiplayer")
                },
                onNavigateToZombie = {
                    navController.navigate("zombie")
                }
            )
        }

        composable("operators") {
            OperatorsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOperatorClick = { operator, icons ->
                    selectedOperator = operator
                    iconMap = icons
                    navController.navigate("operatorDetails")
                }
            )
        }

        composable("operatorDetails") {
            selectedOperator?.let { operator ->
                OperatorDetailsScreen(
                    operator = operator,
                    iconMap = iconMap,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("checklists") {
            ChecklistOverviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCategory = { category ->
                    navController.navigate("checklist/${category.name}")
                }
            )
        }

        composable("checklist/{category}") { backStackEntry ->
            CategoryChecklistScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("maps") {
            MapCategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryClick = { category ->
                    selectedCategory = category
                    navController.navigate("mapList")
                }
            )
        }

        composable("mapList") {
            selectedCategory?.let { category ->
                MapListScreen(
                    category = category,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onMapClick = { map ->
                        selectedMap = map
                        navController.navigate("mapDetail")
                    }
                )
            }
        }

        composable("mapDetail") {
            selectedMap?.let { map ->
                MapDetailScreen(
                    map = map,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onViewMap = {
                        navController.navigate("mapViewer")
                    },
                    iconsRepository = iconsRepository
                )
            }
        }

        composable("mapViewer") {
            selectedMap?.let { map ->
                MapViewerScreen(
                    map = map,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("gameModes") {
            GameModesListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGameModeClick = { gameMode ->
                    selectedGameMode = gameMode
                    navController.navigate("gameModeDetail")
                }
            )
        }

        composable("gameModeDetail") {
            selectedGameMode?.let { gameMode ->
                GameModeDetailScreen(
                    gameMode = gameMode,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("campaignMultiplayer") {
            CampaignMultiplayerHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPerks = {
                    navController.navigate("perks")
                },
                onNavigateToCombatSpecialties = {
                    navController.navigate("combatSpecialties")
                },
                onNavigateToWildcards = {
                    navController.navigate("wildcards")
                },
                onNavigateToScorestreaks = {
                    navController.navigate("scorestreaks")
                },
                onNavigateToTacticals = {
                    navController.navigate("tacticals")
                },
                onNavigateToLethals = {
                    navController.navigate("lethals")
                },
                onNavigateToFieldUpgrades = {
                    navController.navigate("fieldUpgrades")
                }
            )
        }

        composable("perks") {
            PerksListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPerkClick = { perk ->
                    selectedPerk = perk
                    navController.navigate("perkDetail")
                }
            )
        }

        composable("combatSpecialties") {
            CombatSpecialtiesListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("wildcards") {
            WildcardsListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("perkDetail") {
            selectedPerk?.let { perk ->
                PerkDetailScreen(
                    perk = perk,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("scorestreaks") {
            ScorestreaksListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScorestreakClick = { scorestreak ->
                    selectedScorestreak = scorestreak
                    navController.navigate("scorestreakDetail")
                }
            )
        }

        composable("scorestreakDetail") {
            selectedScorestreak?.let { scorestreak ->
                ScorestreakDetailScreen(
                    scorestreak = scorestreak,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("tacticals") {
            TacticalsListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTacticalClick = { tactical ->
                    selectedTactical = tactical
                    navController.navigate("tacticalDetail")
                }
            )
        }

        composable("tacticalDetail") {
            selectedTactical?.let { tactical ->
                TacticalDetailScreen(
                    tactical = tactical,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("lethals") {
            LethalsListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLethalClick = { lethal ->
                    selectedLethal = lethal
                    navController.navigate("lethalDetail")
                }
            )
        }

        composable("lethalDetail") {
            selectedLethal?.let { lethal ->
                LethalDetailScreen(
                    lethal = lethal,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("fieldUpgrades") {
            FieldUpgradesListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFieldUpgradeClick = { fieldUpgrade ->
                    selectedFieldUpgrade = fieldUpgrade
                    navController.navigate("fieldUpgradeDetail")
                }
            )
        }

        composable("fieldUpgradeDetail") {
            selectedFieldUpgrade?.let { fieldUpgrade ->
                FieldUpgradeDetailScreen(
                    fieldUpgrade = fieldUpgrade,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("perkAColas") {
            PerkAColaListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPerkClick = { perkACola ->
                    selectedPerkACola = perkACola
                    navController.navigate("perkAColaDetail")
                }
            )
        }

        composable("perkAColaDetail") {
            selectedPerkACola?.let { perkACola ->
                PerkAColaDetailScreen(
                    perk = perkACola,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("ammoMods") {
            AmmoModsListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAmmoModClick = { ammoMod ->
                    selectedAmmoMod = ammoMod
                    navController.navigate("ammoModDetail")
                }
            )
        }

        composable("ammoModDetail") {
            selectedAmmoMod?.let { ammoMod ->
                AmmoModDetailScreen(
                    ammoMod = ammoMod,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("fieldUpgradesZM") {
            FieldUpgradesZMListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFieldUpgradeClick = { fieldUpgrade ->
                    selectedFieldUpgradeZM = fieldUpgrade
                    navController.navigate("fieldUpgradeZMDetail")
                }
            )
        }

        composable("fieldUpgradeZMDetail") {
            selectedFieldUpgradeZM?.let { fieldUpgrade ->
                FieldUpgradeZMDetailScreen(
                    fieldUpgrade = fieldUpgrade,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("powerUps") {
            PowerUpsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("zombie") {
            ZombieHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPerkAColas = {
                    navController.navigate("perkAColas")
                },
                onNavigateToAmmoMods = {
                    navController.navigate("ammoMods")
                },
                onNavigateToFieldUpgradesZM = {
                    navController.navigate("fieldUpgradesZM")
                },
                onNavigateToPowerUps = {
                    navController.navigate("powerUps")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(onNavigateToDashboard: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "homePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        kotlinx.coroutines.delay(2000)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f * borderGlow),
                                            Color.Transparent
                                        ),
                                        radius = 200f
                                    ),
                                    shape = MaterialTheme.shapes.extraLarge
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .border(
                                    width = 3.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = borderGlow),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f * borderGlow),
                                            MaterialTheme.colorScheme.primary.copy(alpha = borderGlow)
                                        )
                                    ),
                                    shape = MaterialTheme.shapes.extraLarge
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    shape = MaterialTheme.shapes.extraLarge
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Black Ops 7 Logo",
                                modifier = Modifier
                                    .size(110.dp)
                                    .alpha(pulseAlpha)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "BLACK OPS 7",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )

                        Text(
                            text = "COMPANION",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 6.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(
                            modifier = Modifier.width(120.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            thickness = 2.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your tactical companion for Call of Duty Black OPS 7 is getting ready..",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    onNavigateToOperators: () -> Unit,
    onNavigateToChecklists: () -> Unit,
    onNavigateToMaps: () -> Unit = {},
    onNavigateToGameModes: () -> Unit = {},
    onNavigateToCampaignMultiplayer: () -> Unit = {},
    onNavigateToZombie: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dashboardBorder")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Scrollable Main Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Operators Card
            item {
                DashboardCard(
                    title = "OPERATORS",
                    tagline = "View all characters",
                    onClick = onNavigateToOperators,
                    borderColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    gradientColor = MaterialTheme.colorScheme.primary,
                    buttonColor = MaterialTheme.colorScheme.primary,
                    buttonLabel = "EXPLORE",
                    borderGlow = borderGlow
                )
            }

            // Collection Tracker Card
            item {
                DashboardCard(
                    title = "COLLECTION TRACKER",
                    tagline = "Track your stats locally",
                    onClick = onNavigateToChecklists,
                    borderColor = MaterialTheme.colorScheme.secondary,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    gradientColor = MaterialTheme.colorScheme.secondary,
                    buttonColor = MaterialTheme.colorScheme.secondaryContainer,
                    buttonTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    buttonLabel = "TRACK",
                    borderGlow = borderGlow
                )
            }

            // Maps Card
            item {
                DashboardCard(
                    title = "MAPS",
                    tagline = "Explore maps for all modes",
                    onClick = onNavigateToMaps,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    gradientColor = MaterialTheme.colorScheme.tertiary,
                    buttonColor = MaterialTheme.colorScheme.tertiaryContainer,
                    buttonTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    buttonLabel = "EXPLORE",
                    borderGlow = borderGlow
                )
            }

            // Game Modes Card
            item {
                DashboardCard(
                    title = "GAME MODES",
                    tagline = "Browse all game modes",
                    onClick = onNavigateToGameModes,
                    borderColor = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    gradientColor = MaterialTheme.colorScheme.error,
                    buttonColor = MaterialTheme.colorScheme.errorContainer,
                    buttonTextColor = MaterialTheme.colorScheme.onErrorContainer,
                    buttonLabel = "EXPLORE",
                    borderGlow = borderGlow
                )
            }

            // Multiplayer Card
            item {
                DashboardCard(
                    title = "MULTIPLAYER",
                    tagline = "Perks, combat specialty & more",
                    onClick = onNavigateToCampaignMultiplayer,
                    borderColor = Color(0xFF00BCD4), // Cyan color
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    gradientColor = Color(0xFF00BCD4),
                    buttonColor = Color(0xFF00BCD4).copy(alpha = 0.3f),
                    buttonTextColor = Color(0xFF00BCD4),
                    buttonLabel = "EXPLORE",
                    borderGlow = borderGlow
                )
            }

            // Zombie Card
            item {
                DashboardCard(
                    title = "ZOMBIE",
                    tagline = "Zombie perks, items & gear",
                    onClick = onNavigateToZombie,
                    borderColor = Color(0xFF76FF03), // Bright green
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    gradientColor = Color(0xFF76FF03),
                    buttonColor = Color(0xFF76FF03).copy(alpha = 0.3f),
                    buttonTextColor = Color(0xFF76FF03),
                    buttonLabel = "EXPLORE",
                    borderGlow = borderGlow
                )
            }

            // Scroll indicator spacer
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Banner Ad Space at Bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Banner Ad Space (320x90)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DashboardCard(
    title: String,
    tagline: String,
    onClick: () -> Unit,
    borderColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color,
    gradientColor: androidx.compose.ui.graphics.Color,
    buttonColor: androidx.compose.ui.graphics.Color,
    buttonTextColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary,
    buttonLabel: String,
    borderGlow: Float
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = borderGlow),
                        borderColor.copy(alpha = 0.3f),
                        borderColor.copy(alpha = borderGlow)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gradientColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset(100f, 100f),
                            radius = 400f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title with accent line and tagline
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Accent indicator
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                borderColor,
                                shape = MaterialTheme.shapes.small
                            )
                    )

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buttonLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = buttonTextColor
                            )
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = buttonTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}
