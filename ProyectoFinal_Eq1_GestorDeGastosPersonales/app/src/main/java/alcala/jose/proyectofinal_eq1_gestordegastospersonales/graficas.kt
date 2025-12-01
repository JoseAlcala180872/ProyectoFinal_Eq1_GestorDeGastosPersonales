package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.CalculadoraGastos // Importante: Importar la nueva clase
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.GastoCategoria
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.GraficaGastosDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class GraficasFragment : Fragment() {

    // Vistas para la UI
    private lateinit var graficaImageView: ImageView
    private lateinit var tvSinDatos: TextView
    private lateinit var tvTotalGastos: TextView
    private lateinit var mapaTextViews: Map<String, TextView>

    // Dependencias y datos
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val colores = listOf(
        Color.parseColor("#FF6384"), Color.parseColor("#36A2EB"),
        Color.parseColor("#FFCE56"), Color.parseColor("#4BC0C0"),
        Color.parseColor("#9966FF"), Color.parseColor("#FF9F40"),
        Color.parseColor("#FF6347"), Color.parseColor("#ADFF2F")
    )
    private val categoriasPredeterminadas = listOf(
        "Alimentación", "Transporte", "Entretenimiento", "Vivienda",
        "Salud", "Compras", "Servicios", "Otros"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graficas, container, false)
        initVistas(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        cargarDatosDeGastos()
        return view
    }

    private fun initVistas(view: View) {
        graficaImageView = view.findViewById(R.id.grafica_gastos_view)
        tvSinDatos = view.findViewById(R.id.tv_sin_datos)
        tvTotalGastos = view.findViewById(R.id.tvTotalGastos)

        // Usamos un mapa para asociar cada categoría con su TextView, ¡código más limpio!
        mapaTextViews = mapOf(
            "Alimentación" to view.findViewById(R.id.tvAlimentacion),
            "Transporte" to view.findViewById(R.id.tvTransporte),
            "Entretenimiento" to view.findViewById(R.id.tvEntretenimiento),
            "Vivienda" to view.findViewById(R.id.tvVivienda),
            "Salud" to view.findViewById(R.id.tvSalud),
            "Compras" to view.findViewById(R.id.tvCompras),
            "Servicios" to view.findViewById(R.id.tvServicios),
            "Otros" to view.findViewById(R.id.tvOtros)
        )
    }

    private fun cargarDatosDeGastos() {
        val userId = auth.currentUser?.uid ?: return

        database.child("movimientos").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movimientos = snapshot.children.mapNotNull { it.getValue(Movimiento::class.java) }

                // 1. Usar la nueva clase de utilidad para hacer todos los cálculos
                val resultados = CalculadoraGastos.calcular(movimientos, categoriasPredeterminadas)

                // 2. Actualizar el TextView del total de gastos
                tvTotalGastos.text = String.format(Locale.US, "$%.2f", resultados.totalGastos)

                // 3. Actualizar la leyenda de porcentajes
                actualizarLeyenda(resultados.porcentajes)

                // 4. Preparar datos para la gráfica de anillo (solo categorías con gastos)
                val listaGastosCategoria = resultados.porcentajes.entries
                    .filter { it.value > 0 }
                    .map { entry ->
                        GastoCategoria(
                            categoria = entry.key,
                            total = (entry.value / 100) * resultados.totalGastos,
                            color = colores[categoriasPredeterminadas.indexOf(entry.key) % colores.size]
                        )
                    }

                // 5. Dibujar la gráfica o mostrar el estado vacío
                actualizarGrafica(listaGastosCategoria)
            }

            override fun onCancelled(error: DatabaseError) { /* Manejar error */ }
        })
    }

    private fun actualizarLeyenda(porcentajes: Map<String, Double>) {
        porcentajes.forEach { (categoria, porcentaje) ->
            // Se actualiza el texto de cada categoría con su porcentaje
            mapaTextViews[categoria]?.text = String.format(Locale.US, "%s: %.1f%%", categoria, porcentaje)
        }
    }

    private fun actualizarGrafica(listaGastos: List<GastoCategoria>) {
        if (listaGastos.isNotEmpty()) {
            graficaImageView.setImageDrawable(GraficaGastosDrawable(listaGastos))
            graficaImageView.visibility = View.VISIBLE
            tvSinDatos.visibility = View.GONE
        } else {
            graficaImageView.visibility = View.GONE
            tvSinDatos.visibility = View.VISIBLE
        }
    }
}