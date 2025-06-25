package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animación de confetti clásico para celebración
 * Para celebrar la finalización de una caminata
 */
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
    duration: Int = 3000
) {
    val density = LocalDensity.current
    
    // Crear partículas con propiedades random - se regeneran cuando cambia particleCount
    val particles = remember(particleCount) {
        generateConfettiParticles(particleCount)
    }
    
    // Animaciones para cada partícula
    val animatedParticles = particles.map { particle ->
        val animatable = remember { Animatable(0f) }
        
        LaunchedEffect(Unit) {
            // Delay random para efecto de explosión
            kotlinx.coroutines.delay(particle.delayMs)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )
        }
        
        animatable
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasHeight = size.height
        val canvasWidth = size.width
        
        particles.forEachIndexed { index, particle ->
            val progress = animatedParticles[index].value
            
            if (progress > 0f) {
                drawConfettiParticle(
                    particle = particle,
                    progress = progress,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }
    }
}

/**
 * Dibuja una partícula individual de confetti
 */
private fun DrawScope.drawConfettiParticle(
    particle: ConfettiParticle,
    progress: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    // Posición Y con gravedad
    val currentY = particle.startY + (canvasHeight + 200) * progress
    
    // Posición X con viento lateral - usar el ancho real del canvas
    val baseX = particle.startXRatio * canvasWidth
    val windEffect = sin(progress * particle.windFrequency) * particle.windAmplitude
    val currentX = baseX + windEffect
    
    // Rotación
    val rotation = progress * particle.rotationSpeed * 360f
    
    // Fade out al final
    val alpha = if (progress > 0.8f) (1f - (progress - 0.8f) / 0.2f) else 1f
    val color = particle.color.copy(alpha = alpha)
    
    // Dibujar partícula con rotación
    rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
        when (particle.shape) {
            ConfettiShape.CIRCLE -> {
                drawCircle(
                    color = color,
                    radius = particle.size,
                    center = Offset(currentX, currentY)
                )
            }
            ConfettiShape.RECTANGLE -> {
                // Papelitos realistas - más largos y finos
                val width = particle.size * 1.2f
                val height = particle.size * 0.4f
                drawRect(
                    color = color,
                    topLeft = Offset(currentX - width/2, currentY - height/2),
                    size = Size(width, height)
                )
            }
        }
    }
}

/**
 * Genera partículas de confetti con propiedades aleatorias
 */
private fun generateConfettiParticles(count: Int): List<ConfettiParticle> {
    val colors = listOf(
        Color(0xFFE91E63), // Rosa
        Color(0xFF2196F3), // Azul
        Color(0xFF4CAF50), // Verde
        Color(0xFFFF9800), // Naranja
        Color(0xFF9C27B0), // Púrpura
        Color(0xFFFFC107), // Amarillo
        Color(0xFFF44336), // Rojo
        Color(0xFF00BCD4), // Cian
        Color(0xFF673AB7), // Púrpura más oscuro
        Color(0xFF8BC34A), // Verde lima
    )
    
    return (0 until count).map { index ->
        val isRectangle = (index % 5) != 0 // Solo 1 de cada 5 será círculo
        
        ConfettiParticle(
            startXRatio = Random.nextFloat(), // 0.0 a 1.0 del ancho
            startY = Random.nextFloat() * 300f - 150f, // Empezar bien arriba
            color = colors.random(),
            size = Random.nextFloat() * 15f + 8f,
            shape = if (isRectangle) ConfettiShape.RECTANGLE else ConfettiShape.CIRCLE,
            windAmplitude = Random.nextFloat() * 100f + 50f, // Mucho más movimiento
            windFrequency = Random.nextFloat() * 6f + 2f,
            rotationSpeed = if (isRectangle) {
                Random.nextFloat() * 12f + 4f // Papelitos giran más rápido
            } else {
                Random.nextFloat() * 6f + 2f  // Círculos giran más suave
            },
            delayMs = Random.nextLong(0, 800) // Explosión más prolongada y escalonada
        )
    }
}

/**
 * Datos de una partícula individual
 */
private data class ConfettiParticle(
    val startXRatio: Float, // Posición X como ratio del ancho (0.0-1.0)
    val startY: Float,
    val color: Color,
    val size: Float,
    val shape: ConfettiShape,
    val windAmplitude: Float,
    val windFrequency: Float,
    val rotationSpeed: Float,
    val delayMs: Long
)

/**
 * Formas disponibles para las partículas
 */
private enum class ConfettiShape {
    CIRCLE,
    RECTANGLE
}
