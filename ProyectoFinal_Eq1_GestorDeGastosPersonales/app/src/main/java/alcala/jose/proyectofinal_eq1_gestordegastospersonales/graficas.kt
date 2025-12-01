package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.CalculadoraGastos
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
    private lateinit var mapaImageViews: Map<String, ImageView>

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
        actualizarColoresLeyenda()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        cargarDatosDeGastos()
        return view
    }

    private fun initVistas(view: View) {
        graficaImageView = view.findViewById(R.id.grafica_gastos_view)
        tvSinDatos = view.findViewById(R.id.tv_sin_datos)
        tvTotalGastos = view.findViewById(R.id.tvTotalGastos)

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


        mapaImageViews = mapOf(
            "Alimentación" to view.findViewById(R.id.ivAlimentacion),
            "Transporte" to view.findViewById(R.id.ivTransporte),
            "Entretenimiento" to view.findViewById(R.id.ivEntretenimiento),
            "Vivienda" to view.findViewById(R.id.ivVivienda),
            "Salud" to view.findViewById(R.id.ivSalud),
            "Compras" to view.findViewById(R.id.ivCompras),
            "Servicios" to view.findViewById(R.id.ivServicios),
            "Otros" to view.findViewById(R.id.ivOtros)
        )
    }

    private fun actualizarColoresLeyenda() {
        categoriasPredeterminadas.forEachIndexed { index, categoria ->
            mapaImageViews[categoria]?.let { imageView ->
                val color = colores[index % colores.size]
                // Se usa setColorFilter para teñir la imagen del color correspondiente
                imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun cargarDatosDeGastos() {
        val userId = auth.currentUser?.uid ?: return

        database.child("movimientos").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movimientos = snapshot.children.mapNotNull { it.getValue(Movimiento::class.java) }
                val resultados = CalculadoraGastos.calcular(movimientos, categoriasPredeterminadas)

                tvTotalGastos.text = String.format(Locale.US, "$%.2f", resultados.totalGastos)
                actualizarLeyenda(resultados.porcentajes)

                val listaGastosCategoria = resultados.porcentajes.entries
                    .filter { it.value > 0 }
                    .map { entry ->
                        GastoCategoria(
                            categoria = entry.key,
                            total = (entry.value / 100) * resultados.totalGastos,
                            color = colores[categoriasPredeterminadas.indexOf(entry.key) % colores.size]
                        )
                    }

                actualizarGrafica(listaGastosCategoria)
            }

            override fun onCancelled(error: DatabaseError) { /* Manejar error */ }
        })
    }

    private fun actualizarLeyenda(porcentajes: Map<String, Double>) {
        porcentajes.forEach { (categoria, porcentaje) ->
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