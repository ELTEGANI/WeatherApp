package com.example.weatherapp.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.weatherapp.R
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType
import com.example.weatherapp.presentation.model.WeatherUiState
import com.example.weatherapp.presentation.ui.theme.Typography
import com.example.weatherapp.presentation.viewmodel.WeatherViewModel
import com.example.weatherapp.util.WeatherIconMapper


@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherScreenComponents(
        modifier = modifier,
        uiState = uiState,
        onLoadWeather = { viewModel.loadWeatherForecast() },
        onPermissionDenied = { viewModel.onPermissionDenied() }
    )
}


@Composable
fun WeatherScreenComponents(
    modifier: Modifier = Modifier,
    uiState: WeatherUiState,
    onLoadWeather: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    RequestLocationPermission(
        onGranted = onLoadWeather,
        onDenied = onPermissionDenied
    )

    when (uiState) {
        is WeatherUiState.Loading -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = WeatherType.SUNNY,
                isLoading = true,
                error = null,
                forecasts = emptyList()
            )
        }
        is WeatherUiState.Success -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = uiState.weatherType,
                isLoading = false,
                error = null,
                forecasts = uiState.forecasts
            )
        }
        is WeatherUiState.Error -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = WeatherType.SUNNY,
                isLoading = false,
                error = uiState.message,
                forecasts = emptyList()
            )
        }
    }
}

@Composable
private fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = LocalContext.current

    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            onGranted()
        } else {
            onDenied()
        }
    }

    val isPermissionGranted = run {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        fineGranted || coarseGranted
    }

    LaunchedEffect(isPermissionGranted, hasRequestedPermission) {
        when {
            isPermissionGranted -> {
                onGranted()
            }
            !isPermissionGranted && !hasRequestedPermission -> {
                hasRequestedPermission = true
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

@Composable
private fun WeatherScreenContent(
    modifier: Modifier = Modifier,
    weatherType: WeatherType,
    isLoading: Boolean,
    error: String?,
    forecasts: List<WeatherForecast>
) {
    WeatherBackground(
        modifier = modifier,
        weatherType = weatherType
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            ForecastHeader()

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(message = error)
                forecasts.isEmpty() -> EmptyState()
                else -> ForecastList(forecasts = forecasts)
            }
        }
    }
}

@Composable
private fun ForecastHeader() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "5 Day Forecast",
            style = Typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "Loading weather data...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error icon placeholder (using text)
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displayMedium,
                )
                
                Text(
                    text = message.ifBlank { "Something went wrong" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.Black.copy(alpha = 0.87f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "🌤️",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "No forecast data available",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pull to refresh",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ForecastList(forecasts: List<WeatherForecast>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(forecasts) { forecast ->
            WeatherCard(forecast = forecast)
        }
        
        // Bottom padding for last item
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    weatherType: WeatherType,
    content: @Composable () -> Unit
) {
    val backgroundResId = when (weatherType) {
        WeatherType.SUNNY -> R.drawable.sunny
        WeatherType.CLOUDY -> R.drawable.cloudy
        WeatherType.RAINY -> R.drawable.rainy
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        content()
    }
}

@Composable
fun WeatherCard(forecast: WeatherForecast) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weather icon
                Image(
                    painter = painterResource(
                        id = WeatherIconMapper.getWeatherIconResId(forecast.weatherType)
                    ),
                    contentDescription = "${forecast.weatherType} weather icon",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                
                // Day name
                Text(
                    text = forecast.dayName,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Black.copy(alpha = 0.87f)
                )
            }
            
            // Temperature
            Text(
                text = "${forecast.temperature}°",
                style = Typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black.copy(alpha = 0.87f),
                textAlign = TextAlign.End
            )
        }
    }
}
