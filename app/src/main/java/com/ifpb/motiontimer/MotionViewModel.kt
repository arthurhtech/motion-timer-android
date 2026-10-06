package com.ifpb.motiontimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MotionViewModel : ViewModel() {
    // Estados da ViewModel
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isMoving = MutableStateFlow(false)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private val _tempoDecorrido = MutableStateFlow(0L)
    val tempoDecorrido: StateFlow<Long> = _tempoDecorrido.asStateFlow()

    private val _ultimoTempo = MutableStateFlow<String?>(null)
    val ultimoTempo: StateFlow<String?> = _ultimoTempo.asStateFlow()

    // Variáveis internas
    private var tempoInicioMovimento = 0L
    private var tempoUltimoMovimento = 0L
    private var timerJob: Job? = null

    private val LIMITE_ACELERACAO = 1.2f
    private val TOLERANCIA_PARADA_MS = 3000L

    fun toggleTracking() {
        _isTracking.value = !_isTracking.value
        if (_isTracking.value) {
            _isMoving.value = false
            _tempoDecorrido.value = 0L
        } else {
            // Parar o timer se estiver em movimento(clicou no botão)
            _isMoving.value = false
            timerJob?.cancel()
        }
    }

    fun processarAceleracao(magnitude: Float, tempoAtual: Long) {
        if (!_isTracking.value) return

        if (magnitude > LIMITE_ACELERACAO) {
            tempoUltimoMovimento = tempoAtual
            if (!_isMoving.value) {
                _isMoving.value = true
                tempoInicioMovimento = tempoAtual
                iniciarTimer()
            }
        } else if (_isMoving.value) {
            if (tempoAtual - tempoUltimoMovimento > TOLERANCIA_PARADA_MS) {
                finalizarMovimento(tempoAtual)
            }
        }
    }

    private fun iniciarTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isMoving.value) {
                _tempoDecorrido.value = System.currentTimeMillis() - tempoInicioMovimento
                delay(100)
            }
        }
    }

    private fun finalizarMovimento(tempoAtual: Long) {
        _isMoving.value = false
        _isTracking.value = false
        timerJob?.cancel()

        val tempoReal = (tempoAtual - tempoInicioMovimento) - TOLERANCIA_PARADA_MS
        val segundos = tempoReal / 1000
        _ultimoTempo.value = "$segundos s"
    }
}