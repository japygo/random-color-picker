package com.japygo.study.randomcolorpicker.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.japygo.study.randomcolorpicker.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorScreen(
    viewModel: MainViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ColorHeader()

        ColorDisplaySection(
            color = uiState.currentColor,
            hexCode = uiState.hexCode,
            rgbCode = uiState.rgbCode,
        )

        ActionButtonsSection(
            onNewColor = { viewModel.generateNewColor() },
            onSaveColor = {
                val success = viewModel.bookmarkColor()
                if (success) {
                    Toast.makeText(context, "Color Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Storage Full! Delete some colors.", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onCopyCode = {
                clipboardManager.setText(AnnotatedString(uiState.hexCode))
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            },
        )

        if (uiState.history.isNotEmpty()) {
            ColorListSection(
                title = "Recent Colors",
                colors = uiState.history,
                onColorClick = { viewModel.restoreColor(it) },
            )
        }

        if (uiState.savedColors.isNotEmpty()) {
            SavedColorListSection(
                title = "Saved Colors",
                colors = uiState.savedColors,
                deleteCandidate = uiState.deleteCandidate,
                onColorClick = { viewModel.restoreColor(it) },
                onColorLongClick = { viewModel.setDeleteCandidate(it) },
                onDeleteConfirm = {
                    viewModel.deleteSavedColor(it)
                    Toast.makeText(context, "Color Deleted", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}

@Composable
fun ColorHeader() {
    Text(
        text = "HueQuick",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
fun ColorDisplaySection(
    color: Color,
    hexCode: String,
    rgbCode: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(color)
                .border(2.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = hexCode,
                color = if (color.luminance() > 0.5f) Color.Black else Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = hexCode,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = rgbCode,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onNewColor: () -> Unit,
    onSaveColor: () -> Unit,
    onCopyCode: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onNewColor,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("New")
        }

        Button(
            onClick = onSaveColor,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Save")
        }

        OutlinedButton(
            onClick = onCopyCode,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Copy")
        }
    }
}

@Composable
fun ColorListSection(
    title: String,
    colors: List<Color>,
    onColorClick: (Color) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        .clickable { onColorClick(color) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedColorListSection(
    title: String,
    colors: List<Color>,
    deleteCandidate: Color?,
    onColorClick: (Color) -> Unit,
    onColorLongClick: (Color) -> Unit,
    onDeleteConfirm: (Color) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            colors.forEach { color ->
                Box(modifier = Modifier.size(36.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                            .combinedClickable(
                                onClick = { onColorClick(color) },
                                onLongClick = { onColorLongClick(color) },
                            ),
                    )

                    if (deleteCandidate == color) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { onDeleteConfirm(color) },
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorScreenPreview() {
    ColorScreen()
}
