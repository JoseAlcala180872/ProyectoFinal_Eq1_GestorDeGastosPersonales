package alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.gasto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.MetodoPago
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.TipoMovimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.DatePickerHelper
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrarGastoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrarGastoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var montoGasto: EditText
    private lateinit var categoria: AutoCompleteTextView
    private lateinit var tipoPago: AutoCompleteTextView
    private lateinit var fechaGasto: EditText
    private lateinit var descripcionGasto: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtTitulo: TextView

    private val db = FirebaseDatabase.getInstance().getReference("movimientos")
    private val auth = FirebaseAuth.getInstance()
    private var movimientoAEditar: Movimiento? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrar_gasto, container, false)

        montoGasto = view.findViewById(R.id.montoGasto)
        categoria = view.findViewById(R.id.categoria)
        tipoPago = view.findViewById(R.id.tipoPago)
        fechaGasto = view.findViewById(R.id.fechaGasto)
        descripcionGasto = view.findViewById(R.id.descripcionGasto)
        btnRegistrar = view.findViewById(R.id.btnRegistrarGasto)
        txtTitulo = view.findViewById(R.id.txtTitulo)

        // Evita que se abra el teclado al pulsar
        fechaGasto.isFocusable = false
        fechaGasto.isClickable = true

        val movimientoRecibido = arguments?.getSerializable("movimiento_a_editar") as? Movimiento
        if (movimientoRecibido != null) {
            movimientoAEditar = movimientoRecibido
            precargarDatosParaEdicion(movimientoRecibido)
            btnRegistrar.text = "Actualizar"
            txtTitulo.text = "Actualizar Gasto"
        }

        //Configura el listener para que al pulsar se abra el calendario
        fechaGasto.setOnClickListener {
            // Llama a nuestro helper para mostrar el DatePickerDialog
            DatePickerHelper.showDatePickerDialog(requireContext(), fechaGasto)
        }

        val categorias = listOf(
            "Alimentación",
            "Transporte",
            "Entretenimiento",
            "Vivienda",
            "Salud",
            "Compras",
            "Servicios",
            "Otros"
        )

        val adapterCategorias = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categorias
        )
        categoria.setAdapter(adapterCategorias)

        categoria.setOnClickListener {
            categoria.showDropDown()
        }

        val anchoDp = 250
        val scale = resources.displayMetrics.density
        categoria.dropDownWidth = (anchoDp * scale).toInt()

        val metodosPago = listOf("Efectivo", "Tarjeta")

        val adapterMetodosPago = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            metodosPago
        )
        tipoPago.setAdapter(adapterMetodosPago)

        tipoPago.setOnClickListener {
            tipoPago.showDropDown()
        }

        val anchoDp1 = 250
        val scale1 = resources.displayMetrics.density
        tipoPago.dropDownWidth = (anchoDp * scale).toInt()

        btnRegistrar.setOnClickListener {
            registrarGasto()
        }

        return view
    }


    private fun precargarDatosParaEdicion(movimiento: Movimiento) {
        montoGasto.setText(movimiento.monto.toString())
        categoria.setText(movimiento.categoria)
        fechaGasto.setText(movimiento.fecha)
        descripcionGasto.setText(movimiento.descripcion)

        val tipoPagoString = when (movimiento.metodoPago) {
            MetodoPago.TARJETA -> "Tarjeta"
            MetodoPago.EFECTIVO -> "Efectivo"
            else -> "Efectivo" // Por si acaso
        }
        tipoPago.setText(tipoPagoString)
    }



    private fun registrarGasto() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val montoStr = montoGasto.text.toString().trim()
        val categoriaStr = categoria.text.toString().trim()
        val tipoPagoStr = tipoPago.text.toString().trim()
        val fechaStr = fechaGasto.text.toString().trim()
        val descripcionStr = descripcionGasto.text.toString().trim()

        if (montoStr.isEmpty() || categoriaStr.isEmpty() || tipoPagoStr.isEmpty() ||
            fechaStr.isEmpty() || descripcionStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val monto = montoStr.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val metodoPago = when (tipoPagoStr.lowercase(Locale.ROOT)) {
            "tarjeta" -> MetodoPago.TARJETA
            else -> MetodoPago.EFECTIVO
        }

        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val movimientoId = if (movimientoAEditar != null && !movimientoAEditar!!.id.isNullOrEmpty()) {
            movimientoAEditar!!.id!!
        } else {
            db.child(user.uid).push().key ?: return
        }

        val movimiento = Movimiento(
            id = movimientoId,
            descripcion = descripcionStr,
            categoria = categoriaStr,
            monto = monto,
            fecha = fechaStr,
            hora = horaActual,
            tipo = TipoMovimiento.GASTO,
            metodoPago = metodoPago
        )

        db.child(user.uid).child(movimientoId)
            .setValue(movimiento)
            .addOnSuccessListener {
                val mensaje = if (movimientoAEditar != null) "Gasto actualizado correctamente" else "Gasto registrado correctamente"
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                if (movimientoAEditar == null) {
                    limpiarCampos()
                } else {
                    requireActivity().finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al registrar gasto: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun limpiarCampos() {
        montoGasto.text.clear()
        categoria.text.clear()
        tipoPago.text.clear()
        fechaGasto.text.clear()
        descripcionGasto.text.clear()
    }
}