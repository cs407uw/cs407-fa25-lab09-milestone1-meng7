package com.cs407.lab09

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BallViewModel : ViewModel() {

    private var ball: Ball? = null
    private var lastTimestamp: Long = 0L

    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    // !!! 新增：像素/米 的比例系数 !!!
    // 如果觉得还是慢，就把这个数改大 (例如 500, 800)
    // 如果觉得太快控制不住，就把这个数改小 (例如 200)
    private val SIMULATION_RATE = 400f

    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            ball = Ball(fieldWidth, fieldHeight, ballSizePx)
            ball?.let {
                _ballPosition.value = Offset(it.posX, it.posY)
            }
        }
    }

    fun onSensorDataChanged(event: SensorEvent) {
        val currentBall = ball ?: return

        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            if (lastTimestamp != 0L) {
                val dT = (event.timestamp - lastTimestamp) / 1_000_000_000.0f

                val xAcc = -event.values[0] * SIMULATION_RATE
                val yAcc = event.values[1] * SIMULATION_RATE

                currentBall.updatePositionAndVelocity(xAcc, yAcc, dT)

                _ballPosition.update { Offset(currentBall.posX, currentBall.posY) }
            }

            lastTimestamp = event.timestamp
        }
    }

    fun reset() {
        ball?.reset()
        ball?.let { b ->
            _ballPosition.update { Offset(b.posX, b.posY) }
        }
        lastTimestamp = 0L
    }
}