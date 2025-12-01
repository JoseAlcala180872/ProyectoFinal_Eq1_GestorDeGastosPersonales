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
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class RegistrarGastoFragment : Fragment() {

    private lateinit var montoGasto: EditText
    private lateinit var categoria: AutoCompleteTextView
    private lateinit var tipoPago: AutoCompleteTextView
    private lateinit var fechaGasto: EditText
    private lateinit var descripcionGasto: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtTitulo: TextView
    private lateinit var ivVistaPreviaTicket: ImageView
    private lateinit var btnSeleccionarFoto: Button

    private val db = FirebaseDatabase.getInstance().getReference("movimientos")
    private val auth = FirebaseAuth.getInstance()
    private var movimientoAEditar: Movimiento? = null

    // Variable para guardar la ruta de la foto temporalmente
    private var uriImagenSeleccionada: Uri? = null

    // El lanzador para abrir la galería
    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uriImagenSeleccionada = uri
            ivVistaPreviaTicket.setImageURI(uri)
            ivVistaPreviaTicket.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos Cloudinary una sola vez
        try {
            val config = mapOf(
                "cloud_name" to "dw8yxze4m",
                "secure" to true
            )
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
            // Ya estaba inicializado
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrar_gasto, container, false)

        // Inicializar Vistas
        montoGasto = view.findViewById(R.id.montoGasto)
        categoria = view.findViewById(R.id.categoria)
        tipoPago = view.findViewById(R.id.tipoPago)
        fechaGasto = view.findViewById(R.id.fechaGasto)
        descripcionGasto = view.findViewById(R.id.descripcionGasto)
        btnRegistrar = view.findViewById(R.id.btnRegistrarGasto)
        txtTitulo = view.findViewById(R.id.txtTitulo)
        ivVistaPreviaTicket = view.findViewById(R.id.ivVistaPreviaTicket)
        btnSeleccionarFoto = view.findViewById(R.id.btnSeleccionarFoto)

        // Configuración de campos
        fechaGasto.isFocusable = false
        fechaGasto.isClickable = true

        // Lógica de Edición (Precarga)
        val movimientoRecibido = arguments?.getSerializable("movimiento_a_editar") as? Movimiento
        if (movimientoRecibido != null) {
            movimientoAEditar = movimientoRecibido
            precargarDatosParaEdicion(movimientoRecibido)
            btnRegistrar.text = "Actualizar"
            txtTitulo.text = "Actualizar Gasto"

            // TODO: Si deseas mostrar la foto existente al editar,
            // necesitarías usar una librería como Glide o Picasso aquí para cargar la URL en ivVistaPreviaTicket
        }

        // Listener Fecha
        fechaGasto.setOnClickListener {
            DatePickerHelper.showDatePickerDialog(requireContext(), fechaGasto)
        }

        // Configuración de Adapters (Categoría y Pago)
        setupAdapters()

        // Listener para Seleccionar Foto (MOVIDO AQUÍ CORRECTAMENTE)
        btnSeleccionarFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        // Listener Principal: REGISTRAR
        btnRegistrar.setOnClickListener {
            iniciarProcesoRegistro()
        }

        return view
    }

    // --- LÓGICA DE REGISTRO PASO A PASO ---

    private fun iniciarProcesoRegistro() {
        // 1. Validaciones básicas antes de subir nada
        if (!validarCampos()) return

        btnRegistrar.isEnabled = false // Bloquear botón para evitar doble click

        // 2. Decisión: ¿Tiene foto o no?
        if (uriImagenSeleccionada != null) {
            // Opción A: Tiene foto -> Subir a Cloudinary primero
            subirImagenACloudinary(uriImagenSeleccionada!!)
        } else {
            // Opción B: No tiene foto -> Guardar directo en Firebase (con null en la foto)
            // Si estamos editando y ya tenía una foto, deberíamos mantener la anterior (lógica abajo)
            val urlFotoAnterior = movimientoAEditar?.urlComprobante
            guardarEnFirebase(urlFotoAnterior)
        }
    }

    private fun subirImagenACloudinary(uri: Uri) {
        Toast.makeText(requireContext(), "Subiendo comprobante...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri)
            .unsigned("gastos_app_preset") // <--- ¡¡OJO!! CAMBIA ESTO POR TU PRESET DE CLOUDINARY
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // La subida fue exitosa, obtenemos el link
                    val urlPublica = resultData["secure_url"] as String

                    // Ahora guardamos en Firebase con el link nuevo
                    // Usamos runOnUiThread porque el callback de Cloudinary a veces no corre en el hilo principal
                    activity?.runOnUiThread {
                        guardarEnFirebase(urlPublica)
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    activity?.runOnUiThread {
                        btnRegistrar.isEnabled = true
                        Toast.makeText(requireContext(), "Error al subir imagen: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun guardarEnFirebase(urlFoto: String?) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            btnRegistrar.isEnabled = true
            return
        }

        // Recolección de datos
        val monto = montoGasto.text.toString().trim().toDouble()
        val categoriaStr = categoria.text.toString().trim()
        val tipoPagoStr = tipoPago.text.toString().trim()
        val fechaStr = fechaGasto.text.toString().trim()
        val descripcionStr = descripcionGasto.text.toString().trim()

        val metodoPago = when (tipoPagoStr.lowercase(Locale.ROOT)) {
            "tarjeta" -> MetodoPago.TARJETA
            else -> MetodoPago.EFECTIVO
        }

        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Decidir ID (Nuevo o Existente)
        val movimientoId = if (movimientoAEditar != null && !movimientoAEditar!!.id.isNullOrEmpty()) {
            movimientoAEditar!!.id!!
        } else {
            db.child(user.uid).push().key ?: return
        }

        // Crear Objeto Movimiento (Asegúrate que tu Data Class tenga urlComprobante)
        val movimiento = Movimiento(
            id = movimientoId,
            descripcion = descripcionStr,
            categoria = categoriaStr,
            monto = monto,
            fecha = fechaStr,
            hora = horaActual,
            tipo = TipoMovimiento.GASTO,
            metodoPago = metodoPago,
            urlComprobante = urlFoto // <--- AQUÍ GUARDAMOS LA URL
        )

        // Guardar en BD
        db.child(user.uid).child(movimientoId)
            .setValue(movimiento)
            .addOnSuccessListener {
                val mensaje = if (movimientoAEditar != null) "Actualizado correctamente" else "Registrado correctamente"
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                if (movimientoAEditar == null) {
                    limpiarCampos()
                } else {
                    requireActivity().finish()
                }
                btnRegistrar.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error BD: ${e.message}", Toast.LENGTH_LONG).show()
                btnRegistrar.isEnabled = true
            }
    }

    // --- FUNCIONES AUXILIARES ---

    private fun validarCampos(): Boolean {
        if (montoGasto.text.isEmpty() || categoria.text.isEmpty() ||
            tipoPago.text.isEmpty() || fechaGasto.text.isEmpty() ||
            descripcionGasto.text.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        val monto = montoGasto.text.toString().toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun setupAdapters() {
        val categorias = listOf("Alimentación", "Transporte", "Entretenimiento", "Vivienda", "Salud", "Compras", "Servicios", "Otros")
        val adapterCategorias = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categorias)
        categoria.setAdapter(adapterCategorias)
        categoria.setOnClickListener { categoria.showDropDown() }

        val metodosPago = listOf("Efectivo", "Tarjeta")
        val adapterMetodosPago = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, metodosPago)
        tipoPago.setAdapter(adapterMetodosPago)
        tipoPago.setOnClickListener { tipoPago.showDropDown() }
    }

    private fun precargarDatosParaEdicion(movimiento: Movimiento) {
        montoGasto.setText(movimiento.monto.toString())
        categoria.setText(movimiento.categoria)
        fechaGasto.setText(movimiento.fecha)
        descripcionGasto.setText(movimiento.descripcion)

        val tipoPagoString = when (movimiento.metodoPago) {
            MetodoPago.TARJETA -> "Tarjeta"
            MetodoPago.EFECTIVO -> "Efectivo"
            else -> "Efectivo"
        }
        tipoPago.setText(tipoPagoString)
    }

    private fun limpiarCampos() {
        montoGasto.text.clear()
        categoria.text.clear()
        tipoPago.text.clear()
        fechaGasto.text.clear()
        descripcionGasto.text.clear()

        // Limpiamos también la imagen y la vista previa
        uriImagenSeleccionada = null
        ivVistaPreviaTicket.setImageDrawable(null)
        ivVistaPreviaTicket.visibility = View.GONE
    }
}