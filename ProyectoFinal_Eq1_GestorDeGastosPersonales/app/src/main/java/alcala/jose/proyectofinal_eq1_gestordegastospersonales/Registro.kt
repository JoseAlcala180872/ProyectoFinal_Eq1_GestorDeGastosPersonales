package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Usuario
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

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
                guardarUsuario()
            }
        }
    }

    fun registrarse(correo: String, contraseña: String){
        Log.d("INFO", "email: ${correo}, contraseña: ${contraseña}")
        auth.createUserWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("INFO", "RegistroConCorreo:exitoso")
                val usuario = auth.currentUser
                val intent = Intent(this, IniciarSesion::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }else {
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
            nombre.text.toString(),
            apellido.text.toString(),
            correo.text.toString(),
            fechaNacimiento.text.toString()
        )
        usuarioRef.push().setValue(usuario)
    }

    fun verificarCampos(): Boolean {
        val nombre: EditText = findViewById(R.id.etNombre)
        val apellido: EditText = findViewById(R.id.etApellido)
        val correo: EditText = findViewById(R.id.etCorreo)
        val contraseña: EditText = findViewById(R.id.etContraseña)
        val confirmarContraseña: EditText = findViewById(R.id.etConfirmarContraseña)
        val fechaNacimiento: EditText = findViewById(R.id.etFechaNacimiento)
        if (nombre.text.toString().isEmpty()
            || apellido.text.toString().isEmpty()
            || correo.text.toString().isEmpty()
            || contraseña.text.toString().isEmpty()
            || confirmarContraseña.text.toString().isEmpty()
            || fechaNacimiento.text.toString().isEmpty()){
            return false
        } else {
            return true
        }

    }
}