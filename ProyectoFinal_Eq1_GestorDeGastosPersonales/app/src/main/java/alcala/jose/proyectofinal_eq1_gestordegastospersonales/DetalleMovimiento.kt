package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.MetodoPago
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetalleMovimiento : AppCompatActivity() {
    private var movimientoActual: Movimiento? = null
    private val auth = FirebaseAuth.getInstance()
    private lateinit var ivComprobante: ImageView
    private val dbMovimientos by lazy {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario sin permisos.")
        FirebaseDatabase.getInstance().getReference("movimientos").child(userId)
    }
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

        ivComprobante = findViewById(R.id.ivComprobante)

        if (movimiento != null) {
            movimientoActual = movimiento

            val ivIcono: ImageView = findViewById(R.id.ivIconoCategoria)
            val tvMonto: TextView = findViewById(R.id.tvMonto)
            val tvCategoria: TextView = findViewById(R.id.tvCategoria)
            val tvFecha: TextView = findViewById(R.id.tvFechaMovimiento)
            val tvHora: TextView = findViewById(R.id.tvHoraMovimiento)
            val tvMetodoPago: TextView = findViewById(R.id.tvMetodoPago)
            val tvDescripcion: TextView = findViewById(R.id.tvDescripcion)
            val btnEditar: LinearLayout = findViewById(R.id.btnEditar)
            val btnEliminar: LinearLayout = findViewById(R.id.btnEliminar)

            val llMetodoPago: LinearLayout = findViewById(R.id.llMetodoPago)
            val vLineaMetodoPago: View = findViewById(R.id.vLineaMetodoPago)

            if (movimiento.esIngreso()) {
                llMetodoPago.visibility = View.GONE
                vLineaMetodoPago.visibility = View.GONE
            }

            tvMonto.text = movimiento.montoFormateado
            tvCategoria.text = movimiento.categoria
            tvFecha.text = movimiento.fecha
            tvHora.text = movimiento.hora
            tvMetodoPago.text = when (movimiento.metodoPago) {
                MetodoPago.TARJETA -> "Tarjeta"
                MetodoPago.EFECTIVO -> "Efectivo"
            }
            tvDescripcion.text = movimiento.descripcion

            val icono = obtenerIconoPorCategoria(movimiento.categoria)
            ivIcono.setImageResource(icono)

            btnEliminar.setOnClickListener {
                mostrarDialogoConfirmacion()
            }

            btnEditar.setOnClickListener {
                abrirFormularioEdicion()
            }

            cargarComprobante(movimiento.urlComprobante)
        }
    }

    private fun cargarComprobante(url: String?) {
        if (url.isNullOrEmpty()) {
            // Si no hay URL, ocultamos el ImageView
            ivComprobante.visibility = View.GONE
        } else {
            // Si hay URL, mostramos el ImageView y cargamos la foto con Glide
            ivComprobante.visibility = View.VISIBLE

            com.bumptech.glide.Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery) // Imagen mientras carga (opcional)
                .error(android.R.drawable.stat_notify_error)     // Imagen si falla (opcional)
                .into(ivComprobante)
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val movimiento = movimientoActual
        if (movimiento != null && !movimiento.id.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de eliminar este movimiento?")
                .setPositiveButton("Eliminar") {dialog, which -> eliminarMovimiento(movimiento.id!!)}
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            Toast.makeText(this, "Error: El ID del movimiento no está disponible.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarMovimiento(movimientoId: String) {
        dbMovimientos.child(movimientoId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Movimiento eliminado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun abrirFormularioEdicion() {
        val movimiento = movimientoActual
        if (movimiento == null) {
            Toast.makeText(this, "Error: No se encontró el movimiento a editar.", Toast.LENGTH_SHORT).show()
            return
        }

        val tipoFormulario = movimiento.tipo.toString()

        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("movimiento_a_editar", movimiento)
            putExtra("tipo_formulario", tipoFormulario)
        }
        startActivity(intent)
        finish()
    }

    private fun obtenerIconoPorCategoria(categoria: String?): Int {
        return when (categoria?.lowercase()) {
            "alimentación", "alimentacion" -> R.drawable.ic_food
            "entretenimiento" -> R.drawable.ic_entretenimiento
            "transporte" -> R.drawable.ic_transporte
            "vivienda" -> R.drawable.ic_vivienda
            "salud" -> R.drawable.ic_salud
            "servicios" -> R.drawable.ic_servicio
            "otros" -> R.drawable.ic_otros
            "ingreso" -> R.drawable.ic_incomes
            "compras" -> R.drawable.ic_compras
            else -> R.drawable.ic_launcher_background
        }
    }
}