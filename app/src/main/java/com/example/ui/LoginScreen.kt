package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
  viewModel: DossierViewModel,
  modifier: Modifier = Modifier
) {
  var username by remember { mutableStateOf("Jameson Dax") }
  var clearanceCode by remember { mutableStateOf("992-XC-2024") }
  var showError by remember { mutableStateOf(false) }

  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val focusManager = LocalFocusManager.current

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp)
          .verticalScroll(rememberScrollState())
          .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RectangleShape)
          .background(MaterialTheme.colorScheme.surface)
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Aesthetic Top Header Decoration
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "AGENT AUTHENTICATION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Security Status Indicator",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Large Display Heading (Sharp Serifs)
        Text(
          text = "AssetTrack",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )

        Text(
          text = "SECURE PROTOCOL DOSSIER SYSTEM",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "CLEARANCE DECLARATION REQUIRED BELOW",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username / Agent Name Input Fields (Sharp outlines, custom focus indicator)
        OutlinedTextField(
          value = username,
          onValueChange = {
            username = it
            showError = false
          },
          label = { Text("AGENT CUSTODIAN NAME", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          shape = RectangleShape,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("username_input"),
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Key Passcode / Clearance ID
        OutlinedTextField(
          value = clearanceCode,
          onValueChange = {
            clearanceCode = it
            showError = false
          },
          label = { Text("CLEARANCE CODE IDENTIFIER", style = MaterialTheme.typography.labelSmall) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          textStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
          shape = RectangleShape,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clearance_input"),
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              if (username.isBlank() || clearanceCode.isBlank()) {
                showError = true
              } else {
                viewModel.login(username, clearanceCode)
              }
            }
          )
        )

        if (showError) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "CRITICAL: CREDENTIAL DECLARATION CANNOT BE EMPTY",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Interactive sharp-edged Authentication Action Button
        Button(
          onClick = {
            focusManager.clearFocus()
            if (username.isBlank() || clearanceCode.isBlank()) {
              showError = true
            } else {
              viewModel.login(username, clearanceCode)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          shape = RectangleShape,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("login_button"),
          contentPadding = PaddingValues(0.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = "Authorize footprint",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "INITIALIZE RECORD SESSION",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme switch support embedded right on login screen!
        TextButton(
          onClick = { viewModel.toggleDarkMode() },
          shape = RectangleShape,
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary
          ),
          modifier = Modifier.height(48.dp)
        ) {
          Text(
            text = if (isDarkMode) "SWITCH TO IVORY PAPYRUS PROTOCOL" else "SWITCH TO MIDNIGHT COAL PROTOCOL",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Encrypted Connection icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "LOCAL DECRYPTED LOG DATA SYSTEM v3.5",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 9.sp
          )
        }
      }
    }
  }
}
