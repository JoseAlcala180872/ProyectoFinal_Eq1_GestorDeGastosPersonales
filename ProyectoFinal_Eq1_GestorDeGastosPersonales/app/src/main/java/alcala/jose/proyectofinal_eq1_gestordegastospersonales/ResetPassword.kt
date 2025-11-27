package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnEnviarCorreo: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        btnEnviarCorreo = findViewById(R.id.btnEnviarCorreo)
        progressBar = findViewById(R.id.progressBar)

        // Configurar listeners
        btnEnviarCorreo.setOnClickListener {
            enviarCorreoRecuperacion()
        }
    }

    private fun enviarCorreoRecuperacion() {
        val email = etEmail.text.toString().trim()

        // Validar que el campo no esté vacío
        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu correo electrónico"
            etEmail.requestFocus()
            return
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Ingresa un correo válido"
            etEmail.requestFocus()
            return
        }
        progressBar.visibility = View.VISIBLE
        btnEnviarCorreo.isEnabled = false

        // Enviar correo de recuperación
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                btnEnviarCorreo.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Se ha enviado un correo de recuperación a $email",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Volver a la pantalla anterior
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}