package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.LocalAnimatedSurface
import com.example.opendocs_reader.LocalAnimatedOnSurface

@Composable
fun OpenDocsTopBar(title: String) {
    val isHome = title == "OpenDocs"

    val containerColor = LocalAnimatedSurface.current
    val contentColor = LocalAnimatedOnSurface.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
        }

        if (isHome) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Acción Premium */ }) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Premium",
                        tint = Color(0xFF00BCD4)
                    )
                }
                IconButton(onClick = { /* Acción Buscar */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = contentColor
                    )
                }
            }
        }
    }
}