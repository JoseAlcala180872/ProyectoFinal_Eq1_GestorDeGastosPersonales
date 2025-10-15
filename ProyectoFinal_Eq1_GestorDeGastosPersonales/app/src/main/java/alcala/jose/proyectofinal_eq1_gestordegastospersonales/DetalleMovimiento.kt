package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.MetodoPago
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetalleMovimiento : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_movimiento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val movimiento = intent.getSerializableExtra("movimiento") as? Movimiento

        if (movimiento != null) {
            val ivIcono: ImageView = findViewById(R.id.ivIconoCategoria)
            val tvMonto: TextView = findViewById(R.id.tvMonto)
            val tvCategoria: TextView = findViewById(R.id.tvCategoria)
            val tvFecha: TextView = findViewById(R.id.tvFechaMovimiento)
            val tvHora: TextView = findViewById(R.id.tvHoraMovimiento)
            val tvMetodoPago: TextView = findViewById(R.id.tvMetodoPago)
            val tvDescripcion: TextView = findViewById(R.id.tvDescripcion)

            tvMonto.text = movimiento.getMontoFormateado()
            tvCategoria.text = movimiento.categoria
            tvFecha.text = movimiento.fecha
            tvHora.text = movimiento.hora
            tvMetodoPago.text = when (movimiento.metodoPago) {
                MetodoPago.TARJETA -> "Tarjeta"
                MetodoPago.EFECTIVO -> "Efectivo"
            }
            tvDescripcion.text = movimiento.descripcion

            if (movimiento.iconoRes != 0) {
                ivIcono.setImageResource(movimiento.iconoRes)
            }
        }
    }
}