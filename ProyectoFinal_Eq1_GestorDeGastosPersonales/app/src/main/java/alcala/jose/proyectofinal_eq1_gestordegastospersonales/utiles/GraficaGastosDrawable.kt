package alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

// Data class para pasar los datos a la gráfica
data class GastoCategoria(val categoria: String, val total: Double, val color: Int)

class GraficaGastosDrawable(private val gastos: List<GastoCategoria>) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f // Tamaño del texto para las etiquetas
        textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        val totalGastos = gastos.sumOf { it.total }
        if (totalGastos == 0.0) return // No dibujar nada si no hay gastos

        // 1. Define el grosor del anillo. ¡Puedes cambiar este valor!
        val strokeWidth = 60f
        // 2. Cambia el estilo a STROKE para dibujar solo el contorno.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth

        // 3. Encoge el rectángulo para que el trazo completo quede dentro.
        val rect = RectF(bounds)
        rect.inset(strokeWidth / 2, strokeWidth / 2)

        var startAngle = -90f

        // Dibuja cada segmento del anillo
        for (gasto in gastos) {
            paint.color = gasto.color
            val sweepAngle = (gasto.total / totalGastos * 360).toFloat()
            // 4. Dibuja el arco sin conectarlo al centro (useCenter = false).
            canvas.drawArc(rect, startAngle, sweepAngle, false, paint)

            startAngle += sweepAngle
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        textPaint.alpha = alpha // Aplicar transparencia también al texto
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter // Aplicar filtro de color también al texto
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
