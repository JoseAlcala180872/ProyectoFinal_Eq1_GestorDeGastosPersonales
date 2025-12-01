package alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.MetodoPago
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MovimientoAdapter(private var movimientos: List<Movimiento>,
                        private val onItemClick: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gasto_view, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        holder.bind(movimientos[position])
    }

    override fun getItemCount(): Int = movimientos.size

    /**
     * Actualiza la lista de movimientos
     */
    fun actualizarMovimientos(nuevosMovimientos: List<Movimiento>) {
        movimientos = nuevosMovimientos
        notifyDataSetChanged()
    }

    inner class MovimientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcono: ImageView = itemView.findViewById(R.id.icon)
        private val tvCategoria: TextView = itemView.findViewById(R.id.concepto)
        private val tvFecha: TextView = itemView.findViewById(R.id.fechaConcepto)
        private var tvMetodoPago: TextView = itemView.findViewById(R.id.metodoPago)
        private val tvMonto: TextView = itemView.findViewById(R.id.cantidadDinero)

        private fun obtenerIconoPorCategoria(categoria: String?): Int {
            return when (categoria?.lowercase()) {
                "alimentación", "alimentacion" -> R.drawable.ic_food
                "entretenimiento" -> R.drawable.ic_entretenimiento
                "transporte" -> R.drawable.ic_transporte
                "vivienda" -> R.drawable.ic_vivienda
                "salud" -> R.drawable.ic_salud
                "servicios" -> R.drawable.ic_launcher_background
                "otros" -> R.drawable.ic_otros
                "ingreso" -> R.drawable.ic_incomes
                "compras" -> R.drawable.ic_compras
                else -> R.drawable.ic_launcher_background
            }
        }

        fun bind(movimiento: Movimiento) {
            tvFecha.text = movimiento.fecha
            tvCategoria.text = movimiento.categoria
            tvMonto.text = movimiento.montoFormateado
            tvMetodoPago.text = when (movimiento.metodoPago) {
                MetodoPago.TARJETA -> "Tarjeta"
                MetodoPago.EFECTIVO -> "Efectivo"
            }

            if (movimiento.esIngreso()) {
                tvMetodoPago.visibility = View.INVISIBLE
            }else {
                tvMetodoPago.visibility = View.VISIBLE
            }

            val icono = obtenerIconoPorCategoria(movimiento.categoria)
            ivIcono.setImageResource(icono)

            val colorRes = if (movimiento.esIngreso()) {
                R.color.azulClaro
            } else {
                R.color.red
            }
            tvMonto.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            itemView.setOnClickListener {
                onItemClick(movimiento)
            }
        }
    }

}