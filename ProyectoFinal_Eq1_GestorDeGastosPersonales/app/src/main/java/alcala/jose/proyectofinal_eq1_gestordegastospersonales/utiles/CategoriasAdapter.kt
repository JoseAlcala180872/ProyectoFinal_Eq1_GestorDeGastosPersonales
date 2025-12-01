package alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Presupuesto
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CategoriasAdapter(private var listaCategorias: List<Presupuesto>,
                        private val onPresupuestoClick: (String) -> Unit ) : RecyclerView.Adapter<CategoriasAdapter.PresupuestoViewHolder>() {
    class PresupuestoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
    val ivIcono: ImageView = view.findViewById(R.id.ivIconoCategoria)
    val txtActual: TextView = view.findViewById(R.id.txtCantidadActual)
    val txtTotal: TextView = view.findViewById(R.id.txtCantidadTotal)
    val progressBar: ProgressBar = view.findViewById(R.id.barraProgeso)
    val txtPorcentaje: TextView = view.findViewById(R.id.txtPorcentaje)
    val btnDefinir: ImageButton = view.findViewById(R.id.btnDefinirPresupuesto)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresupuestoViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.categoria_view, parent, false)
    return PresupuestoViewHolder(view)
}

override fun onBindViewHolder(holder: PresupuestoViewHolder, position: Int) {
    val item = listaCategorias[position]

    holder.tvNombre.text = item.nombre
    holder.ivIcono.setImageResource(item.iconoResId)

    // Formato de dinero
    holder.txtActual.text = String.format(Locale.US, "%.2f", item.gastado)
    holder.txtTotal.text = String.format(Locale.US, "%.2f", item.presupuesto)

    // Lógica de la barra de progreso
    if (item.presupuesto > 0) {
        val porcentaje = ((item.gastado / item.presupuesto) * 100).toInt()
        holder.progressBar.progress = porcentaje
        holder.txtPorcentaje.text = "$porcentaje%"

        // Cambiar color a ROJO si se pasa del 100%
        if (porcentaje > 100) {
            holder.progressBar.progressTintList = ColorStateList.valueOf(Color.RED)
        } else {
            // Color original (o verde/naranja)
            holder.progressBar.progressTintList = null
        }
    } else {
        holder.progressBar.progress = 0
        holder.txtPorcentaje.text = "0%"
    }

    // Click en el botón "+"
    holder.btnDefinir.setOnClickListener {
        onPresupuestoClick(item.nombre)
    }
}

override fun getItemCount() = listaCategorias.size

// Método para actualizar datos sin recrear todo
fun actualizarDatos(nuevaLista: List<Presupuesto>) {
    listaCategorias = nuevaLista
    notifyDataSetChanged()
}
}