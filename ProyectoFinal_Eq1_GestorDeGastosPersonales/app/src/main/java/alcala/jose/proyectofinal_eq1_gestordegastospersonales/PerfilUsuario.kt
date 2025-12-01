package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Usuario
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.PerfilUsuarioViewModel
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class PerfilUsuario : AppCompatActivity() {

    private val viewModel: PerfilUsuarioViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var usuarioRef: FirebaseDatabase
    private lateinit var tvNombreCompleto: TextView

    private lateinit var btnCerrarSesion: Button
    private lateinit var tvCorreo: TextView
    private lateinit var etNombreCompleto: EditText
    private lateinit var etCorreo: EditText
    private lateinit var ivEditarNombre: ImageButton
    private lateinit var ivEditarCorreo: ImageButton
    private lateinit var btnGuardarNombre: TextView
    private lateinit var btnGuardarCorreo: TextView
    private lateinit var etContrasena: EditText
    private lateinit var ibEditarContrasena: ImageButton
    private lateinit var btnGuardarContrasena: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        usuarioRef = Firebase.database
        if (!usuarioLogueado()){
            Toast.makeText(
                baseContext,
                "No hay usuario logueado",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        inicializarVistas()
        setupEstadoInicial()
        setUpLieteners()
//        cargarDatosusuario()
        setupBackButton()
        configurarObservadores()
    }

    private fun inicializarVistas(){
        tvNombreCompleto = findViewById(R.id.tvUserName)
        tvCorreo = findViewById(R.id.tvUserEmail)
        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        etCorreo = findViewById(R.id.etCorreo)
        btnGuardarNombre = findViewById(R.id.btnGuardarNombre)
        btnGuardarCorreo = findViewById(R.id.btnGuardarCorreo)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        ivEditarNombre = findViewById(R.id.btnEditName)
        ivEditarCorreo = findViewById(R.id.btnEditEmail)
        etContrasena = findViewById(R.id.etContrasena)
        btnGuardarContrasena = findViewById(R.id.btnGuardarContrasena)
        ibEditarContrasena = findViewById(R.id.btnEditPassword)
    }

    private fun setupEstadoInicial(){
        desactivarEdicion(etNombreCompleto, btnGuardarNombre)
        desactivarEdicion(etCorreo, btnGuardarCorreo)
        desactivarEdicion(etContrasena, btnGuardarContrasena)
    }

    private fun setUpLieteners(){
        ivEditarNombre.setOnClickListener {
            activarEdicion(etNombreCompleto, btnGuardarNombre)
        }

        btnGuardarNombre.setOnClickListener {
            guardarDatos()
        }

        // --- LOGICA CONTRASEÑA ---
        ibEditarContrasena.setOnClickListener {
            // 1. Limpiamos el campo para que escriba la nueva
            etContrasena.text.clear()
            etContrasena.hint = "Nueva contraseña"

            // 2. Activamos edición
            activarEdicion(etContrasena, btnGuardarContrasena)
        }

        btnGuardarContrasena.setOnClickListener {
            val nuevaPass = etContrasena.text.toString()

            // Bloqueamos botón temporalmente
            btnGuardarContrasena.isEnabled = false

            // Llamamos al ViewModel
            viewModel.cambiarContrasena(nuevaPass)
        }

        ivEditarCorreo.setOnClickListener { activarEdicion(etCorreo, btnGuardarCorreo) }
        btnGuardarCorreo.setOnClickListener { guardarDatos() }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            Toast.makeText(applicationContext, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun guardarDatos(){
        val textoCompleto = etNombreCompleto.text.toString().trim()

        val partes = textoCompleto.split(" ", limit = 2)

        val nuevoNombre = if(partes.isNotEmpty()) partes[0] else ""
        val nuevoApellido = if (partes.size > 1) partes[1] else ""
        val nuevoCorreo = etCorreo.text.toString()

        btnGuardarNombre.isEnabled = false
        btnGuardarCorreo.isEnabled = false

        viewModel.guardarCambios(nuevoNombre, nuevoApellido, nuevoCorreo)
    }

    private fun configurarObservadores(){
        viewModel.usuario.observe(this) { usuario ->
            if (usuario!=null){
                val nombreCompleto = "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim()
                tvNombreCompleto.text = nombreCompleto
                tvCorreo.text = usuario.correo

                if(!etNombreCompleto.isEnabled) etNombreCompleto.setText(nombreCompleto)
                if(!etCorreo.isEnabled) etCorreo.setText(usuario.correo)
            }
        }

        viewModel.resutladoActualizacion.observe(this) {resultado ->
            btnGuardarNombre.isEnabled = true
            btnGuardarCorreo.isEnabled = true
            btnGuardarContrasena.isEnabled = true

            when {
                resultado == "EXITO" -> {
                    Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    // Bloqueamos todo de nuevo
                    desactivarEdicion(etNombreCompleto, btnGuardarNombre)
                    desactivarEdicion(etCorreo, btnGuardarCorreo)
                }
                resultado == "EXITO_PASSWORD" -> {
                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT)
                        .show()

                    // Limpiamos el campo y ponemos puntitos falsos estéticos
                    etContrasena.setText("..........")
                    desactivarEdicion(etContrasena, btnGuardarContrasena)
                }
                resultado == "EXITO_CON_LOGOUT" -> {
                    Toast.makeText(this, "Correo actualizado. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()

                    // Lógica de cerrar sesión
                    auth.signOut()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                resultado == "ERROR_SEGURIDAD_PASS" -> {
                    AlertDialog.Builder(this)
                        .setTitle("Seguridad")
                        .setMessage("Para cambiar tu contraseña, necesitamos verificar que eres tú. Por favor, cierra sesión e inicia de nuevo.")
                        .setPositiveButton("Cerrar Sesión") { _, _ ->
                            auth.signOut()
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                resultado == "ERROR_SEGURIDAD" -> {
                    // Muestra un diálogo o un Toast largo explicando claramente
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Seguridad")
                        .setMessage("Para cambiar tu correo, necesitamos verificar que eres tú. Por favor, cierra sesión e inicia de nuevo.")
                        .setPositiveButton("Entendido") { _, _ ->
                            // Opcional: Cerrar sesión automáticamente aquí si quieres
                        }
                        .show()
                }
                resultado.startsWith("ERROR Auth") -> {
                    Toast.makeText(this, "Cierra sesión e inicia de nuevo para cambiar el correo.", Toast.LENGTH_LONG).show()
                }
                resultado.startsWith("ERORR") -> {
                    Toast.makeText(this, resultado, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBackButton() {
        val btnVolver: ImageView = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosusuario(){
        val currentUser = auth.currentUser
        if(currentUser == null){
            Toast.makeText(
                baseContext,
                "No hay usuario logueado",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        val uid = currentUser?.uid
        val usuarioRef = usuarioRef.getReference("usuarios").child(uid!!)
        usuarioRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()){
                val usuario = snapshot.getValue(Usuario::class.java)
                tvNombreCompleto.text = usuario?.nombre + " " + usuario?.apellido
                tvCorreo.text = usuario?.correo
                etNombreCompleto.setText(usuario?.nombre + " " + usuario?.apellido)
                etCorreo.setText(usuario?.correo)

                Log.d("PerfilUsuario", "Datos Cargados Exitosamente.")
            } else {
                Log.d("PerfilUsuario", "No se encontraron datos del $uid del usuario.")
            }
        }.addOnFailureListener { exception -> {
                Log.e("PerfilUsuario", "Error al cargar los datos del usuario.", exception)
                Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }




    //FUNCIONES DE UTILIDAD PARA LA UI

    private fun activarEdicion(editText: EditText, botonGuardar: View){
        editText.isEnabled = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        botonGuardar.visibility = View.VISIBLE
    }

    private fun desactivarEdicion(editText: EditText, botonGuardar: View){
        editText.isEnabled = false
        botonGuardar.visibility = View.GONE
    }

    fun usuarioLogueado(): Boolean {
        return auth.currentUser != null
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}