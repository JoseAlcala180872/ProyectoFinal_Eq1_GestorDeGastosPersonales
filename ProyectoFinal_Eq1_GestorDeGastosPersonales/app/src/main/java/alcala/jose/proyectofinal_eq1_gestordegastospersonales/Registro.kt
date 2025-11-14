package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Usuario
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.DatePickerHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Registro : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        val nombre: EditText = findViewById(R.id.etNombre)
        val apellido: EditText = findViewById(R.id.etApellido)
        val correo: EditText = findViewById(R.id.etCorreo)
        val contraseña: EditText = findViewById(R.id.etContraseña)
        val confirmarContraseña: EditText = findViewById(R.id.etConfirmarContraseña)
        val fechaNacimiento: EditText = findViewById(R.id.etFechaNacimiento)
        val botonRegistro: Button = findViewById(R.id.btnRegistrarGasto)

        // Evita que se abra el teclado al pulsar
        fechaNacimiento.isFocusable = false
        fechaNacimiento.isClickable = true

        //Configura el listener para que al pulsar se abra el calendario
        fechaNacimiento.setOnClickListener {
            // Llama a nuestro helper para mostrar el DatePickerDialog
            DatePickerHelper.showDatePickerDialog(this, fechaNacimiento)
        }

        botonRegistro.setOnClickListener {
            if (!verificarCampos()) {
                Log.w("ERROR", "RegistroConCorreo:falló")
                Toast.makeText(
                    baseContext,
                    "Todos los campos son obligatorios",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!contraseña.text.toString().equals(confirmarContraseña.text.toString())){
                Log.w("ERROR", "RegistroConCorreo:falló")
                Toast.makeText(
                    baseContext,
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_LONG
                ).show()
            }else {
                registrarse(correo.text.toString(), contraseña.text.toString())
            }
        }
    }

    fun registrarse(correo: String, contraseña: String){
        Log.d("INFO", "email: ${correo}, contraseña: ${contraseña}")
        auth.createUserWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("INFO", "RegistroConCorreo:exitoso")
                guardarUsuario()
                val usuario = auth.currentUser
                val intent = Intent(this, IniciarSesion::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Log.w("ERROR", "RegistroConCorreo:falló", task.exception)
                Toast.makeText(
                    baseContext,
                    "El registro falló",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun guardarUsuario() {
        val nombre: EditText = findViewById(R.id.etNombre)
        val apellido: EditText = findViewById(R.id.etApellido)
        val correo: EditText = findViewById(R.id.etCorreo)
        val fechaNacimiento: EditText = findViewById(R.id.etFechaNacimiento)

        val usuario = Usuario(
            null,
            nombre.text.toString(),
            apellido.text.toString(),
            correo.text.toString(),
            fechaNacimiento.text.toString()
        )

        val currentUser = auth.currentUser
        if (currentUser != null) {
            usuarioRef.child(currentUser.uid).setValue(usuario)
                .addOnSuccessListener {
                    Log.d("INFO", "Usuario guardado correctamente con UID: ${currentUser.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("ERROR", "Error al guardar usuario", e)
                }
        } else {
            Log.w("ERROR", "No hay usuario autenticado")
        }
    }

    fun esMayorDe16(fechaNacimientoStr: String): Boolean {
        return try {
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formato.isLenient = false

            val fechaNacimiento = formato.parse(fechaNacimientoStr) ?: return false

            val calendarioNacimiento = Calendar.getInstance()
            calendarioNacimiento.time = fechaNacimiento

            val hoy = Calendar.getInstance()

            val edad = hoy.get(Calendar.YEAR) - calendarioNacimiento.get(Calendar.YEAR)

            if (hoy.get(Calendar.DAY_OF_YEAR) < calendarioNacimiento.get(Calendar.DAY_OF_YEAR)) {
                return edad - 1 >= 16
            }

            edad >= 16
        } catch (e: Exception) {
            false
        }
    }

    fun verificarCampos(): Boolean {
        val nombre: EditText = findViewById(R.id.etNombre)
        val apellido: EditText = findViewById(R.id.etApellido)
        val correo: EditText = findViewById(R.id.etCorreo)
        val contraseña: EditText = findViewById(R.id.etContraseña)
        val confirmarContraseña: EditText = findViewById(R.id.etConfirmarContraseña)
        val fechaNacimiento: EditText = findViewById(R.id.etFechaNacimiento)

        if (nombre.text.isNullOrEmpty()
            || apellido.text.isNullOrEmpty()
            || correo.text.isNullOrEmpty()
            || contraseña.text.isNullOrEmpty()
            || confirmarContraseña.text.isNullOrEmpty()
            || fechaNacimiento.text.isNullOrEmpty()
        ) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!esMayorDe16(fechaNacimiento.text.toString())) {
            Toast.makeText(this, "Debes tener al menos 16 años", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}