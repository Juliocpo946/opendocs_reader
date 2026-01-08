package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.core.domain.model.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            SortOptionItem("Por defecto", SortOption.DEFAULT, currentSort, onSortSelected)
            SortOptionItem("Nombre (A-Z)", SortOption.NAME_ASC, currentSort, onSortSelected)
            SortOptionItem("Nombre (Z-A)", SortOption.NAME_DESC, currentSort, onSortSelected)
            SortOptionItem("Fecha (Más reciente)", SortOption.DATE_NEWEST, currentSort, onSortSelected)
            SortOptionItem("Fecha (Más antiguo)", SortOption.DATE_OLDEST, currentSort, onSortSelected)
            SortOptionItem("Tipo de archivo", SortOption.TYPE, currentSort, onSortSelected)
        }
    }
}

@Composable
fun SortOptionItem(
    text: String,
    option: SortOption,
    currentSort: SortOption,
    onSelect: (SortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(option) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (currentSort == option) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}