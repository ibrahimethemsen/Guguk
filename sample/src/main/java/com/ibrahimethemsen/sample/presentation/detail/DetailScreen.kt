package com.ibrahimethemsen.sample.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    quoteId: String,
    detailViewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val quoteState = detailViewModel.quote.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        detailViewModel.getQuote(quoteId)
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.clickable { onBackClick() },
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null
        )
        if (quoteState != null) {
            Column {
                Text(
                    text = quoteState.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- ${quoteState.author}",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Text("Loading quote...")
        }
    }
}