package com.example.labthreejetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Calc()
        }
    }
}

@Composable
fun Calc() {
    var pC by remember { mutableStateOf("") }
    var sigma1 by remember { mutableStateOf("") }
    var sigma2 by remember { mutableStateOf("") }
    var costB by remember { mutableStateOf("") }
    var firstCheckProfit by remember { mutableStateOf(0.0) }
    var secondCheckProfit by remember { mutableStateOf(0.0) }

    // Функція нормального закону розподілу потужності
    fun normalDistribution(p: Double, pC: Double, sigma: Double): Double {
        return (1 / (sigma * sqrt(2 * PI))) * exp(-((p - pC) / sigma).pow(2) / 2)
    }

    // Інтегрування методом трапецій
    fun integrateNormalDistribution(a: Double, b: Double, mean: Double, stdDev: Double, steps: Int): Double {
        val stepSize = (b - a) / steps  // Крок інтегрування
        var area = 0.0

        for (i in 0 until steps) {
            val x1 = a + i * stepSize
            val x2 = a + (i + 1) * stepSize
            val y1 = normalDistribution(x1, mean, stdDev)
            val y2 = normalDistribution(x2, mean, stdDev)
            area += (y1 + y2) / 2 * stepSize  // Площа трапеції
        }
        return area
    }

    fun calculate() {
        val formattedPC = pC.toDoubleOrNull() ?: 0.0
        val formattedSigma1 = sigma1.toDoubleOrNull() ?: 0.0
        val formattedSigma2 = sigma2.toDoubleOrNull() ?: 0.0
        val formattedCostB = costB.toDoubleOrNull() ?: 0.0

        val lowerBound = formattedPC - 0.25 // Найменше значення діапазону інтегрування
        val upperBound = formattedPC + 0.25  // Найбільше значення діапазону інтегрування
        val steps = 10000  // Кількість кроків

        // Інтегрування нормального розподілу до вдосконалення системи небалансів
        val probability = BigDecimal(
            integrateNormalDistribution(lowerBound, upperBound, formattedPC, formattedSigma1, steps)
        ).setScale(2, RoundingMode.HALF_UP).toDouble()
        // Прибуток і штраф до вдосконалення системи прогнозу
        val power1 = formattedPC * 24 * probability
        val profit = power1 * formattedCostB

        val power2 = formattedPC * 24 * (1-probability)
        val penalty = power2 * formattedCostB
        firstCheckProfit = profit - penalty
        // Інтегрування нормального розподілу до вдосконалення системи небалансів
        val probability2 = BigDecimal(
            integrateNormalDistribution(lowerBound, upperBound, formattedPC, formattedSigma2, steps)
        ).setScale(2, RoundingMode.HALF_UP).toDouble()
        // Прибуток і штраф після вдосконалення системи прогнозу
        val power3 = formattedPC * 24 * probability2
        val profit2 = power3 * formattedCostB

        val power4 = formattedPC * 24 * (1-probability2)
        val penalty2 = power4 * formattedCostB
        secondCheckProfit = profit2 - penalty2
    }

    Column(modifier = Modifier.padding(15.dp)) {
        TextField(
            value = pC,
            onValueChange = { pC = it },
            label = { Text("Середньодобова потужність, МВт") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(8.dp))

        TextField(
            value = sigma1,
            onValueChange = { sigma1 = it },
            label = { Text("Середньоквадратичне відхилення 1, МВт") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(8.dp))

        TextField(
            value = sigma2,
            onValueChange = { sigma2 = it },
            label = { Text("Середньоквадратичне відхилення 2, МВт") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(8.dp))

        TextField(
            value = costB,
            onValueChange = { costB = it },
            label = { Text("Вартість електроенергії, кВт*год") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(8.dp))

        Button(onClick = { calculate() }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.size(16.dp))

        if (firstCheckProfit != 0.0 && secondCheckProfit != 0.0) {
            Text("Результати:")
            Text("Прибуток до вдосконалення: %.2f тис. грн".format(firstCheckProfit))
            Text("Прибуток після вдосконалення: %.2f тис. грн".format(secondCheckProfit))
        }
    }
}