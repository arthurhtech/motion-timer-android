package com.ifpb.motiontimer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.sqrt

@Composable
fun MotionScreen(viewModel: MotionViewModel = viewModel()) {
    val context = LocalContext.current

    // Estados da ViewModel
    val isTracking by viewModel.isTracking.collectAsState()
    val isMoving by viewModel.isMoving.collectAsState()
    val tempoDecorrido by viewModel.tempoDecorrido.collectAsState()
    val ultimoTempo by viewModel.ultimoTempo.collectAsState()

    // Registro do sensor
    DisposableEffect(isTracking) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Sensor de aceleração linear
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                // Magnitude do vetor de aceleração
                val magnitude = sqrt((event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]).toDouble()).toFloat()

                viewModel.processarAceleracao(magnitude, System.currentTimeMillis())
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (isTracking) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // Desenho da Tela
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        Button(
            onClick = { viewModel.toggleTracking() },
            modifier = Modifier.size(240.dp, 65.dp)
        ) {
            Text(if (isTracking) "Parar Contador" else "Iniciar Rastreador")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isTracking) {
            if (isMoving) {
                Text(
                    text = "Movendo: ${tempoDecorrido / 1000}s",
                    style = MaterialTheme.typography.headlineMedium
                )
            } else {
                Text(
                    text = "Aguardando movimento...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exibe o último tempo registrado
        if (ultimoTempo != null) {
            Text(
                text = "Último tempo: $ultimoTempo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(0.7f))
    }
}