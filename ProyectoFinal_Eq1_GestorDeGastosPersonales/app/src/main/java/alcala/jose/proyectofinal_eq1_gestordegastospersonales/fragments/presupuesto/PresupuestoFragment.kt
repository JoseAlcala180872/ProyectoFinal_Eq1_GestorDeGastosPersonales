package alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.presupuesto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Presupuesto
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.TipoMovimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.CategoriasAdapter
import android.app.AlertDialog
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import kotlin.jvm.java

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PresupuestoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PresupuestoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var tvPresupuestoTotal: TextView
    private lateinit var tvGastado: TextView
    private lateinit var btnDefinirPresupuestoGlobal: Button
    private lateinit var recyclerView: RecyclerView

    // Adapter
    private lateinit var adapter: CategoriasAdapter

    // Datos
    private var listaCategorias = ArrayList<Presupuesto>()

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    // Variables globales
    private var presupuestoGlobalTotal = 0.0
    private var gastoGlobalTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_presupuesto, container, false)

        // 1. Vincular Vistas
        tvPresupuestoTotal = view.findViewById(R.id.presupuestoTotal)
        tvGastado = view.findViewById(R.id.gastado)
        btnDefinirPresupuestoGlobal = view.findViewById(R.id.agregar)
        recyclerView = view.findViewById(R.id.categories_List)

        // 2. Configurar RecyclerView
        setupRecyclerView()

        // 3. Listener Botón Global
        btnDefinirPresupuestoGlobal.setOnClickListener {
            mostrarDialogoPresupuesto("total")
        }

        // 4. Cargar Datos de Firebase
        escucharDatosFirebase()

        return view
    }

    private fun setupRecyclerView() {
        // Inicializamos la lista con las categorías fijas y sus iconos
        // Asegúrate de tener estos drawables en tu proyecto o usa uno genérico por ahora
        listaCategorias = arrayListOf(
            Presupuesto("Alimentación", iconoResId = R.drawable.ic_food),
            Presupuesto("Transporte", iconoResId = R.drawable.ic_transporte),
            Presupuesto("Entretenimiento", iconoResId = R.drawable.ic_entretenimiento),
            Presupuesto("Vivienda", iconoResId = R.drawable.ic_vivienda),
            Presupuesto("Salud", iconoResId = R.drawable.ic_salud),
            Presupuesto("Compras", iconoResId = R.drawable.ic_compras),
            Presupuesto("Servicios", iconoResId = R.drawable.ic_servicio),
            Presupuesto("Otros", iconoResId = R.drawable.ic_otros)
        )

        adapter = CategoriasAdapter(listaCategorias) { categoriaNombre ->
            // Callback: Al hacer click en el "+" de una tarjeta
            // Convertimos nombre a minúscula o como lo guardes en firebase (ej: "alimentacion")
            // Para simplificar, usaremos el nombre tal cual para la clave o lo normalizamos
            val claveFirebase = categoriaNombre.lowercase(Locale.ROOT)
            mostrarDialogoPresupuesto(claveFirebase)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun escucharDatosFirebase() {
        val uid = auth.currentUser?.uid ?: return

        // --- 1. Escuchar Presupuestos (Global y por Categoría) ---
        db.getReference("presupuestos").child(uid).addValueEventListener(object :
            ValueEventListener {
            // CORRECCIÓN: Se agregó la palabra 'override' aquí abajo
            override fun onDataChange(snapshot: DataSnapshot) {
                // A. Presupuesto Global
                // Usamos una conversión segura a Double para evitar crasheos si es entero (Long)
                val total = snapshot.child("total").getValue(Double::class.java)
                    ?: snapshot.child("total").getValue(Long::class.java)?.toDouble()
                    ?: 0.0

                presupuestoGlobalTotal = total
                tvPresupuestoTotal.text = formatearDinero(presupuestoGlobalTotal)

                // B. Presupuestos por Categoría
                for (cat in listaCategorias) {
                    // Generamos la clave en minúsculas (ej: "alimentacion")
                    val clave = cat.nombre.lowercase(Locale.ROOT)

                    // Leemos el valor, manejando si es Double o Long en la base de datos
                    val presCat = snapshot.child(clave).getValue(Double::class.java)
                        ?: snapshot.child(clave).getValue(Long::class.java)?.toDouble()
                        ?: 0.0

                    cat.presupuesto = presCat
                }

                // Notificar al adapter que los datos cambiaron
                adapter.notifyDataSetChanged()
                actualizarColorGastadoGlobal()
            }

            // CORRECCIÓN: Se agregó la palabra 'override' aquí abajo
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar presupuestos", Toast.LENGTH_SHORT).show()
            }
        })

        // --- 2. Escuchar Gastos (Movimientos) ---
        db.getReference("movimientos").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gastoGlobalTotal = 0.0

                // Reiniciar contadores de gasto de categorías en local
                for (cat in listaCategorias) { cat.gastado = 0.0 }

                for (child in snapshot.children) {
                    // Intentamos convertir el hijo a un objeto Movimiento
                    // Asegúrate que tu clase Movimiento tenga un constructor vacío
                    try {
                        val movimiento = child.getValue(Movimiento::class.java)

                        if (movimiento != null && movimiento.tipo == TipoMovimiento.GASTO) {
                            // Suma Global
                            gastoGlobalTotal += (movimiento.monto ?: 0.0)

                            // Suma por Categoría
                            val nombreCatMovimiento = movimiento.categoria // Ej: "Alimentación"

                            // Buscamos en nuestra lista local la categoría que coincida
                            val categoriaEncontrada = listaCategorias.find {
                                it.nombre.equals(nombreCatMovimiento, ignoreCase = true)
                            }

                            // Si encontramos la categoría, le sumamos el gasto
                            categoriaEncontrada?.gastado = (categoriaEncontrada?.gastado ?: 0.0) + (movimiento.monto ?: 0.0)
                        }
                    } catch (e: Exception) {
                        // Si un movimiento tiene datos corruptos, lo ignoramos para no cerrar la app
                        continue
                    }
                }

                tvGastado.text = formatearDinero(gastoGlobalTotal)
                adapter.notifyDataSetChanged()
                actualizarColorGastadoGlobal()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarDialogoPresupuesto(claveFirebase: String) {
        val titulo = if(claveFirebase == "total") "Presupuesto Total" else "Presupuesto ${claveFirebase.capitalize()}"

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Ej: 2000"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val montoStr = input.text.toString()
            if (montoStr.isNotEmpty()) {
                val monto = montoStr.toDouble()
                guardarPresupuestoEnFirebase(claveFirebase, monto)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun guardarPresupuestoEnFirebase(clave: String, monto: Double) {
        val uid = auth.currentUser?.uid ?: return
        db.getReference("presupuestos").child(uid).child(clave).setValue(monto)
            .addOnSuccessListener {
                Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarColorGastadoGlobal() {
        if (gastoGlobalTotal > presupuestoGlobalTotal && presupuestoGlobalTotal > 0) {
            tvGastado.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            // Regresar a color normal (rojoClaro según tu XML)
            tvGastado.setTextColor(resources.getColor(R.color.rojoClaro, null))
        }
    }

    private fun formatearDinero(cantidad: Double): String {
        return String.format(Locale.US, "$%.2f", cantidad)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PresupuestoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PresupuestoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}