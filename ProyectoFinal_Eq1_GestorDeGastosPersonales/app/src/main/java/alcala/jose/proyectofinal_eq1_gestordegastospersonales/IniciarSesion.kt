package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class IniciarSesion : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciar_sesion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        val btnIngresarInicio: Button = findViewById(R.id.btnIngresarInicio)
        val correo: EditText = findViewById(R.id.etCorreo)
        val contraseña: EditText = findViewById(R.id.etConfirmarContraseña)
        val tv_olvideContra: TextView=findViewById(R.id.tv_olvideContra)

        btnIngresarInicio.setOnClickListener {
            if (correo.text.toString().isEmpty() || contraseña.text.toString().isEmpty()){
                showError("Todos los campos son obligatorios")
            }else{
                iniciarSesion(correo.text.toString(), contraseña.text.toString())
            }
        }

        tv_olvideContra.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }
    }

    fun goToMain(user: FirebaseUser){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user", user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun showError(text: String){
        Toast.makeText(
            baseContext,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun iniciarSesion(correo: String, contraseña: String){
        auth.signInWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                goToMain(user!!)
            }else{
                showError("Correo o contraseña incorrectos")
            }
        }
    }
}