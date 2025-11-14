package alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.ingreso

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
 * Use the [RegistrarIngresoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrarIngresoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var montoIngreso: EditText
    private lateinit var fechaIngreso: EditText
    private lateinit var descripcionIngreso: EditText
    private lateinit var btnRegistrarIngreso: Button
    private lateinit var txtTitulo: TextView
    private val db = FirebaseDatabase.getInstance().getReference("movimientos")
    private val auth = FirebaseAuth.getInstance()
    private var param2: String? = null
    private var movimientoAEditar: Movimiento? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_registrar_ingreso, container, false)
        montoIngreso = view.findViewById(R.id.montoIngreso)
        fechaIngreso = view.findViewById(R.id.fechaIngreso)
        descripcionIngreso = view.findViewById(R.id.descripcionIngreso)
        btnRegistrarIngreso = view.findViewById(R.id.btnRegistrarIngreso)
        txtTitulo = view.findViewById(R.id.txtTitulo)

        // Evita que se abra el teclado al pulsar
        fechaIngreso.isFocusable = false
        fechaIngreso.isClickable = true

        val movimientoRecibido = arguments?.getSerializable("movimiento_a_editar") as? Movimiento
        if (movimientoRecibido != null) {
            movimientoAEditar = movimientoRecibido
            precargarDatosParaEdicion(movimientoRecibido) // 🛑 Muestra la información
            btnRegistrarIngreso.text = "Actualizar Ingreso"
            txtTitulo.text = "Editar Ingreso"
        }

        //Configura el listener para que al pulsar se abra el calendario
        fechaIngreso.setOnClickListener {
            // Llama a nuestro helper para mostrar el DatePickerDialog
            DatePickerHelper.showDatePickerDialog(requireContext(), fechaIngreso)
        }

        btnRegistrarIngreso.setOnClickListener {
            registrarIngreso()
        }
        return view
    }

    private fun precargarDatosParaEdicion(movimiento: Movimiento) {
        montoIngreso.setText(movimiento.monto.toString())
        fechaIngreso.setText(movimiento.fecha)
        descripcionIngreso.setText(movimiento.descripcion)
    }

    private fun registrarIngreso() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val montoStr = montoIngreso.text.toString().trim()
        val fechaStr = fechaIngreso.text.toString().trim()
        val descripcionStr = descripcionIngreso.text.toString().trim()

        if(montoStr.isEmpty() || fechaStr.isEmpty() || descripcionStr.isEmpty()){
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val monto = montoStr.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
            return
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
            monto = monto,
            fecha = fechaStr,
            hora = horaActual,
            categoria = "Ingreso",
            tipo = TipoMovimiento.INGRESO
            )

        db.child(user.uid).child(movimientoId)
            .setValue(movimiento)
            .addOnSuccessListener {
                val mensaje = if (movimientoAEditar != null) "Ingreso actualizado correctamente" else "Ingreso registrado correctamente"
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
        montoIngreso.text.clear()
        fechaIngreso.text.clear()
        descripcionIngreso.text.clear()
    }
}