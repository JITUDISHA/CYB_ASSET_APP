package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AssetDossier
import com.example.data.DossierLog
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: DossierViewModel,
  modifier: Modifier = Modifier
) {
  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val activeTab by viewModel.currentTab.collectAsState()
  val dossierOpt by viewModel.assetDossier.collectAsState()
  val logList by viewModel.allLogs.collectAsState()
  val activeUser by viewModel.currentUsername.collectAsState()
  val activeClearance by viewModel.currentClearance.collectAsState()

  val dossier = dossierOpt ?: AssetDossier(
    name = "Laser Alignment Rig X-200",
    series = "Industrial Precision Series | OptiCore Systems",
    status = "MAINTENANCE REQUIRED",
    custodianName = "Jameson Dax",
    custodianDivision = "Logistics Division",
    serialReference = "SN-44029-BRV",
    classification = "Precision Optics",
    acquisitionDate = "14 OCT 2023",
    lastLocation = "Bay 4, Sector G",
    uid = "UID-992-XC-2024"
  )

  // Dialog visible states
  var showNewEntryDialog by remember { mutableStateOf(false) }
  var showEditDossierDialog by remember { mutableStateOf(false) }
  var showScheduleDialog by remember { mutableStateOf(false) }

  // Background flicker animation mimicking vintage surveillance system
  val infiniteTransition = rememberInfiniteTransition(label = "surveillance_flicker")
  val systemFlickerAlpha by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 3500
        0.98f at 200
        1.0f at 400
        0.95f at 1600
        0.99f at 1800
        1.0f at 2200
        0.97f at 3000
      },
      repeatMode = RepeatMode.Reverse
    ),
    label = "flicker"
  )

  Scaffold(
    modifier = modifier.alpha(systemFlickerAlpha),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.border(0.dp, Color.Transparent).drawBehind {
          // Bottom border 1px styled
          drawLine(
            color = if (isDarkMode) Color(0xFF222222) else Color(0xFFD6CDC1),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
          )
        },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "ASSETTRACK",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
              ),
              color = MaterialTheme.colorScheme.primary
            )
            // Tiny sub tagline indicator
            Box(
              modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = "ONLINE",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.testTag("logout_back_button")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Logout and Return",
              tint = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.toggleDarkMode() },
            modifier = Modifier.testTag("theme_switcher")
          ) {
            Icon(
              imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Switch Visual System Theme",
              tint = MaterialTheme.colorScheme.secondary
            )
          }

          IconButton(
            onClick = { /* System notification logs info */ },
            modifier = Modifier.testTag("notifications_icon")
          ) {
            Box {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Classified logs alerts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
              // Crimson glowing dot
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(MaterialTheme.colorScheme.primary)
                  .align(Alignment.TopEnd)
              )
            }
          }

          // Small corporate profile avatar thumbnail
          Box(
            modifier = Modifier
              .padding(end = 12.dp)
              .size(32.dp)
              .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            // Hotlinked Avatar of technician from UI spec HTML
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data("https://lh3.googleusercontent.com/aida-public/AB6AXuCn7FjGcTUAx9QtQbggtQbDgZy63L_8Ya3r4xxDrIHVvWK6IvrNLugVaSUjBN965yU4yIv2oLfrk7X83fUmiCVZvkPk5uZMRBMXuQdP2XzN3xKHFuk0FQQxCXyl8mhMSq7Qj2yhHYmUYWzZGo09WLvBh6nHk9D5KBr5vKhH2OhqIwkgsQZjEcfy4QkpKS2Ef8Ck6wMkJP5DnJJArFBDrv7a62rpeqDODZBbdJSWZC4E1BBX6CCam03NnK264qnfj157BcAGxwjv1xVs")
                .crossfade(true)
                .build(),
              contentDescription = "Active Agent Profile",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      )
    },
    bottomBar = {
      // Bottom navigation matching mobile drawer of image
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier
          .border(0.dp, Color.Transparent)
          .drawBehind {
            // Draw top border for aesthetic separation
            drawLine(
              color = if (isDarkMode) Color(0xFF222222) else Color(0xFFD6CDC1),
              start = Offset(0f, 0f),
              end = Offset(size.width, 0f),
              strokeWidth = 1.dp.toPx()
            )
          }
          .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        val tabsList = listOf(
          Triple("ARCHIVE", Icons.Default.GridView, "ARCHIVE"),
          Triple("ASSETS", Icons.Default.Inventory, "ASSETS"),
          Triple("INTEL", Icons.Default.Article, "INTEL"),
          Triple("AGENT", Icons.Default.AccountCircle, "AGENT")
        )

        tabsList.forEach { (tabLabel, icon, id) ->
          val isSelected = activeTab == id
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.selectTab(id) },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = "$tabLabel Tab Button",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            label = {
              Text(
                text = tabLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = Color.Transparent
            )
          )
        }
      }
    }
  ) { paddingValues ->
    // Dynamic tab-focused screen content
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      when (activeTab) {
        "ASSETS" -> {
          AssetsDashboardTab(
            dossier = dossier,
            logList = logList,
            viewModel = viewModel,
            isDarkMode = isDarkMode,
            onTriggerNewEntry = { showNewEntryDialog = true },
            onTriggerEditDossier = { showEditDossierDialog = true },
            onTriggerSchedule = { showScheduleDialog = true }
          )
        }
        "ARCHIVE" -> {
          ArchiveTabContent(isDarkMode = isDarkMode, activeRigName = dossier.name)
        }
        "INTEL" -> {
          IntelTabContent(isDarkMode = isDarkMode)
        }
        "AGENT" -> {
          AgentTabContent(
            username = activeUser ?: "Jameson Dax",
            clearance = activeClearance ?: "UID-992-XC-2024",
            viewModel = viewModel,
            isDarkMode = isDarkMode
          )
        }
      }
    }
  }

  // ==========================================
  // REAL INPUT WORKING POPUPS
  // ==========================================

  if (showNewEntryDialog) {
    NewLogDialog(
      onDismiss = { showNewEntryDialog = false },
      onSubmit = { category, textNotes ->
        viewModel.addNewLog(category, textNotes)
        showNewEntryDialog = false
      }
    )
  }

  if (showEditDossierDialog) {
    EditDossierDialog(
      dossier = dossier,
      onDismiss = { showEditDossierDialog = false },
      onSubmit = { name, series, serial, classification, location ->
        viewModel.updateDossier(name, series, serial, classification, location)
        showEditDossierDialog = false
      }
    )
  }

  if (showScheduleDialog) {
    ScheduleInspectionDialog(
      onDismiss = { showScheduleDialog = false },
      onSubmit = { dateString ->
        viewModel.scheduleInspection(dateString)
        showScheduleDialog = false
      }
    )
  }
}

// ==========================================
// ACTIVE DASHBOARD COMPONENT (Main Page view)
// ==========================================
@Composable
fun AssetsDashboardTab(
  dossier: AssetDossier,
  logList: List<DossierLog>,
  viewModel: DossierViewModel,
  isDarkMode: Boolean,
  onTriggerNewEntry: () -> Unit,
  onTriggerEditDossier: () -> Unit,
  onTriggerSchedule: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("assets_lazy_column"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Classified Image Hero Banner Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp)
          .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data("https://lh3.googleusercontent.com/aida/ADBb0uhmD3I703dEj1AsqvftncnH_ndEmMhAeS_eEAkRdXMBKriEkUAr4_w3u9CA7KzBzJvR5OafUgCp986jcBu8pCnTls36gMFqqgMLXyxbTJRnSDZMxO-_rZHt2CRWYMB4KUSymaQHi85ej51ejR86vzpUB7y-vF_yQlMTaERRygmOn9HjkbrbVjgY1mxhnXCMdIX1EYNEHS1IFfcmyo0RiRwVWAOjvqDJv5UbGFi0wFXpmyvSheGe9d9ijocg")
            .crossfade(true)
            .build(),
          contentDescription = "Laser alignment calibration device",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Custom Overlay gradient like cinematic film vignette
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                  MaterialTheme.colorScheme.background
                )
              )
            )
        )

        // Text Badge Info
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Box(
            modifier = Modifier
              .background(MaterialTheme.colorScheme.primary)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "CLASSIFIED ASSET",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = dossier.name,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = dossier.series,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Light
          )
        }
      }
    }

    // 2. Bento details (UID and Access QR Scanner key)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // QR Container
        Box(
          modifier = Modifier
            .weight(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          // Custom ID Scanning Moving Red line laser
          val infiniteScanTransition = rememberInfiniteTransition(label = "laser_scans")
          val scanOffsetFraction by infiniteScanTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              animation = tween(2500, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            ),
            label = "scan_laser"
          )

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(110.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, RectangleShape)
                .background(Color.White)
                .padding(8.dp)
            ) {
              // Custom dots texture mimicking high-spec grid structure
              Canvas(modifier = Modifier.fillMaxSize()) {
                val dotsSpace = 4.dp.toPx()
                for (x in 0..size.width.toInt() step dotsSpace.toInt()) {
                  for (y in 0..size.height.toInt() step dotsSpace.toInt()) {
                    drawCircle(Color.LightGray, radius = 1.dp.toPx(), center = Offset(x.toFloat(), y.toFloat()))
                  }
                }
                
                // Draw decorative center QR symbol
                val rectSize = size.width / 3f
                drawRect(
                  color = Color.Black,
                  topLeft = Offset((size.width - rectSize) / 2f, (size.height - rectSize) / 2f),
                  size = androidx.compose.ui.geometry.Size(rectSize, rectSize)
                )
                
                // Draw laser scanning bar overlay physically
                val scanY = size.height * scanOffsetFraction
                drawLine(
                  color = Color.Red,
                  start = Offset(0f, scanY),
                  end = Offset(size.width, scanY),
                  strokeWidth = 2.dp.toPx()
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "ACCESS KEY",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
              text = dossier.uid,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Status Card Column
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Status Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
              .background(MaterialTheme.colorScheme.surface)
              .padding(16.dp)
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "ASSET STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(6.dp))

              // Glow flashing dot effect
              val animatedGlow = rememberInfiniteTransition(label = "pulse_alert")
              val glowAlpha by animatedGlow.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                  animation = tween(800, easing = LinearEasing),
                  repeatMode = RepeatMode.Reverse
                ),
                label = "glow_flicker"
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Glow status light
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .alpha(glowAlpha)
                    .background(
                      if (dossier.status == "SECURED") MaterialTheme.colorScheme.secondary
                      else MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                  text = dossier.status,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (dossier.status == "SECURED") MaterialTheme.colorScheme.secondary
                         else MaterialTheme.colorScheme.primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Custodian segment
              Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "CUSTODIAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Circle Badge of initials
                val initials = dossier.custodianName.split(" ")
                  .mapNotNull { it.firstOrNull()?.toString() }
                  .take(2)
                  .joinToString("")
                  .uppercase()

                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                  )
                }

                Column {
                  Text(
                    text = dossier.custodianName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = dossier.custodianDivision,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Bento Detailed Tile Rows (Grid of 4 attributes)
    item {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val attributeSpecs = listOf(
          Triple("SERIAL REFERENCE", dossier.serialReference, Icons.Default.Fingerprint),
          Triple("CLASSIFICATION", dossier.classification, Icons.Default.Category),
          Triple("ACQUISITION DATE", dossier.acquisitionDate, Icons.Default.History),
          Triple("LAST KNOWN LOCATION", dossier.lastLocation, Icons.Default.LocationSearching)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          attributeSpecs.take(2).forEach { (lbl, data, icon) ->
            Box(
              modifier = Modifier
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp)
            ) {
              Column {
                Icon(
                  imageVector = icon,
                  contentDescription = "$lbl Icon Symbol",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = lbl,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = data,
                  style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          attributeSpecs.takeLast(2).forEach { (lbl, data, icon) ->
            Box(
              modifier = Modifier
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp)
            ) {
              Column {
                Icon(
                  imageVector = icon,
                  contentDescription = "$lbl Icon Symbol",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = lbl,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = data,
                  style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // 4. Incident Reports & History Area
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
          .background(MaterialTheme.colorScheme.surface)
          .padding(16.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Incident Reports & Notes",
              style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )

            // Direct active button linking to adding logs
            TextButton(
              onClick = onTriggerNewEntry,
              shape = RectangleShape,
              colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
              ),
              modifier = Modifier.minimumInteractiveComponentSize().testTag("add_log_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.PostAdd,
                  contentDescription = "Create log entry button",
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "NEW ENTRY",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

          Spacer(modifier = Modifier.height(14.dp))

          // List details of report logs with left margins or red border markings
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (logList.isEmpty()) {
              Text(
                text = "NO HISTORIC DOSSIER NOTES CAPTURED FOR THIS COMPONENT UNIT.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
              )
            } else {
              logList.forEach { report ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                      // Draw Crimson Left Border Line mimicking physical folder tab grouping
                      drawLine(
                        color = if (report.isCritical) Color(0xFFB91C1C) else Color(0xFF444444),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx()
                      )
                    }
                    .background(
                      if (report.isCritical) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .padding(vertical = 10.dp, horizontal = 12.dp)
                ) {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "${report.logNumber} - ${report.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = if (report.isCritical) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurface
                        )
                      )

                      Text(
                        text = report.dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                      )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                      text = report.notes,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Light
                      ),
                      color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = "AUTH: ${report.author}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                      ),
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RectangleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 5. Bottom Quick Action Drawer/Control Panel
    item {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 24.dp)
      ) {
        // Edit button
        Button(
          onClick = onTriggerEditDossier,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
          ),
          shape = RectangleShape,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("edit_dossier_action"),
          contentPadding = PaddingValues(0.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "EDIT DOSSIER",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Folder details"
            )
          }
        }

        // Schedule Inspection Button
        Button(
          onClick = onTriggerSchedule,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
          ),
          shape = RectangleShape,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("schedule_inspection_action"),
          contentPadding = PaddingValues(0.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SCHEDULE INSPECTION",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = "Create diagnostic schedule entry"
            )
          }
        }

        // Toggle state / Mark as missing action
        val isMissing = dossier.status == "MISSING"
        Button(
          onClick = {
            if (isMissing) {
              viewModel.toggleAssetStatus() // Set back to maintenance
            } else {
              viewModel.markAsMissing()
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isMissing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            contentColor = if (isMissing) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
          ),
          shape = RectangleShape,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("mark_missing_action"),
          contentPadding = PaddingValues(0.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isMissing) "MARK AS SECURED & RESPONDED" else "MARK AS MISSING PROTOCOL",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
              imageVector = if (isMissing) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = "Safety Alert change Trigger"
            )
          }
        }
      }
    }
  }
}

// ==========================================
// ARCHIVE TAB LAYOUT CONTENT
// ==========================================
@Composable
fun ArchiveTabContent(isDarkMode: Boolean, activeRigName: String) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "INTEL CASE ARCHIVES",
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "AUTHORIZED INVESTIGATOR ACCESS DIRECTORY",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Divider(color = MaterialTheme.colorScheme.outline)
    }

    val itemsList = listOf(
      Triple("RIG-XC-2024 (ACTIVE)", activeRigName, "Precision Laser alignment devices deployed in Subsector-G."),
      Triple("OP-BA-2025 (SECURED)", "Quantum Refractor Prism", "High fidelity light prism configured for surveillance optics."),
      Triple("SE-77-2026 (STANDBY)", "Magneto-Resonator Reactor", "Sub-fusion operational energy generation testbed core.")
    )

    items(itemsList) { (caseId, title, desc) ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
          .background(MaterialTheme.colorScheme.surface)
          .padding(16.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = caseId,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = "Classified Archive Folder",
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

// ==========================================
// INTEL TAB LAYOUT CONTENT (Historical documents)
// ==========================================
@Composable
fun IntelTabContent(isDarkMode: Boolean) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "FIELD INTEL TRANSCRIPTS",
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "DECRYPTED TELEGRAPHS AND RECONNAISSANCE PAPERS",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Divider(color = MaterialTheme.colorScheme.outline)
    }

    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .border(
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            RectangleShape
          )
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
          .padding(16.dp)
      ) {
        Column {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TRANSCRIPT #661-A",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            Box(
              modifier = Modifier
                .background(Color.Red)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = "REDACTED",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Color.White)
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "TRANSCRIPT OF INTERCEPTS",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "...\n" +
              "OPERATOR CODE B3: \"Rig alignment calibration is drifting. Variance recorded at 0.05mm.\"\n" +
              "STATIONMASTER: \"Dax Jameson notified. Logistics must deploy replacement optics Cabinet-9 immediately. Ensure surveillance coverage remains strictly dark and unbroken.\"\n" +
              "...",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

// ==========================================
// CONTEXT AGENT PROFILE TAB LAYOUT
// ==========================================
@Composable
fun AgentTabContent(
  username: String,
  clearance: String,
  viewModel: DossierViewModel,
  isDarkMode: Boolean
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Profiling container badge
    Box(
      modifier = Modifier
        .size(110.dp)
        .border(2.dp, MaterialTheme.colorScheme.secondary, RectangleShape)
        .padding(4.dp)
        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data("https://lh3.googleusercontent.com/aida-public/AB6AXuCn7FjGcTUAx9QtQbggtQbDgZy63L_8Ya3r4xxDrIHVvWK6IvrNLugVaSUjBN965yU4yIv2oLfrk7X83fUmiCVZvkPk5uZMRBMXuQdP2XzN3xKHFuk0FQQxCXyl8mhMSq7Qj2yhHYmUYWzZGo09WLvBh6nHk9D5KBr5vKhH2OhqIwkgsQZjEcfy4QkpKS2Ef8Ck6wMkJP5DnJJArFBDrv7a62rpeqDODZBbdJSWZC4E1BBX6CCam03NnK264qnfj157BcAGxwjv1xVs")
          .crossfade(true)
          .build(),
        contentDescription = "Technician portrait metadata",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = username.uppercase(),
      style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = "AUTHORIZED SPECIALIST AGENCY AGENT",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.secondary,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(24.dp))

    Divider(color = MaterialTheme.colorScheme.outline)

    Spacer(modifier = Modifier.height(24.dp))

    // Profile specifics cards
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      AgentDetailRow("CLEARANCE ENCRYPT CODE", clearance, Icons.Default.Security)
      AgentDetailRow("LOCAL RECORD TIMEx", "MAY 22, 2026 - 17:50Z", Icons.Default.AccessTime)
      AgentDetailRow("FOSSIL ARCHIVE LINK", "AES-256 INTERNAL PROTOCOLS", Icons.Default.Pin)
    }

    Spacer(modifier = Modifier.height(40.dp))

    // Switch theme override
    Button(
      onClick = { viewModel.toggleDarkMode() },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
      ),
      shape = RectangleShape,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = "Toggle screen color override icon"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isDarkMode) "ENGAGE LIGHT ENVELOPE MODE" else "ENGAGE MIDNIGHT COVER MODE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Logout session
    Button(
      onClick = { viewModel.logout() },
      colors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary
      ),
      shape = RectangleShape,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("agent_logout_button")
    ) {
      Text(
        text = "TERMINATE DOSSIER LOCK-SESSION",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun AgentDetailRow(label: String, valName: String, icon: ImageVector) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
      .background(MaterialTheme.colorScheme.surface)
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = "$label icon decoration",
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(16.dp)
    )
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = valName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

// ==========================================
// SHARP LOG DIALOGS POPUPS (NO AD-HOC POPUPS)
// ==========================================

@Composable
fun NewLogDialog(
  onDismiss: () -> Unit,
  onSubmit: (category: String, notes: String) -> Unit
) {
  var notes by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("MAINTENANCE") } // Default status choice

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFFB91C1C), RectangleShape)
        .background(MaterialTheme.colorScheme.background)
        .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = "LOG NEW FIELD EVENT REPORT",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )

        Divider(color = MaterialTheme.colorScheme.outline)

        // Dropdown status picker style selectors
        Column {
          Text(text = "EVENT CATEGORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("MAINTENANCE", "GENERAL", "CRITICAL").forEach { cat ->
              val selected = category == cat
              OutlinedButton(
                onClick = { category = cat },
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                  width = 1.dp,
                  color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp)
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }

        // Details report message text entry
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("NOTES AND FIELD OBSERVATIONS", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          textStyle = MaterialTheme.typography.bodyMedium,
          shape = RectangleShape,
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .testTag("dialog_notes_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "CANCEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              if (notes.isNotBlank()) {
                onSubmit(category, notes)
              }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.weight(1f).testTag("dialog_submit_button")
          ) {
            Text(text = "COMMIT LOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun EditDossierDialog(
  dossier: AssetDossier,
  onDismiss: () -> Unit,
  onSubmit: (name: String, series: String, serial: String, classification: String, location: String) -> Unit
) {
  var name by remember { mutableStateOf(dossier.name) }
  var series by remember { mutableStateOf(dossier.series) }
  var serial by remember { mutableStateOf(dossier.serialReference) }
  var classification by remember { mutableStateOf(dossier.classification) }
  var location by remember { mutableStateOf(dossier.lastLocation) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.primary, RectangleShape)
        .background(MaterialTheme.colorScheme.background)
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "RECONFIGURE DOSSIER DATA",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )

        Divider(color = MaterialTheme.colorScheme.outline)

        // Asset Name Input
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("ASSET IDENTIFIER NAME", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
          singleLine = true
        )

        // Asset Series Input
        OutlinedTextField(
          value = series,
          onValueChange = { series = it },
          label = { Text("INDUSTRIAL SERIES LABEL", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.bodyMedium,
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("edit_series_input"),
          singleLine = true
        )

        // Serial REFERENCE
        OutlinedTextField(
          value = serial,
          onValueChange = { serial = it },
          label = { Text("SERIAL REFERENCE KEY", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.labelMedium,
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("edit_serial_input"),
          singleLine = true
        )

        // Classification details info
        OutlinedTextField(
          value = classification,
          onValueChange = { classification = it },
          label = { Text("CLASSIFICATION DOMAIN", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.bodyMedium,
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("edit_class_input"),
          singleLine = true
        )

        // Location Info Details
        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("LAST REPORTED LOCATION POINT", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.bodyMedium,
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("edit_location_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "CANCEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              if (name.isNotBlank()) {
                onSubmit(name, series, serial, classification, location)
              }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.weight(1f).testTag("edit_submit_button")
          ) {
            Text(text = "UPDATE DATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun ScheduleInspectionDialog(
  onDismiss: () -> Unit,
  onSubmit: (date: String) -> Unit
) {
  var inspectionDate by remember { mutableStateOf("28 MAY 2026") }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.primary, RectangleShape)
        .background(MaterialTheme.colorScheme.background)
        .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = "SCHEDULE COMPONENT DIAGNOSTICS",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )

        Divider(color = MaterialTheme.colorScheme.outline)

        Text(
          text = "Declare target test timestamp below in standard military layout:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = inspectionDate,
          onValueChange = { inspectionDate = it },
          label = { Text("SCHEDULED DATE", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          textStyle = MaterialTheme.typography.labelLarge,
          shape = RectangleShape,
          modifier = Modifier.fillMaxWidth().testTag("schedule_date_input"),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "CANCEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              if (inspectionDate.isNotBlank()) {
                onSubmit(inspectionDate)
              }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.weight(1f).testTag("schedule_submit_button")
          ) {
            Text(text = "SCHEDULE INSP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
