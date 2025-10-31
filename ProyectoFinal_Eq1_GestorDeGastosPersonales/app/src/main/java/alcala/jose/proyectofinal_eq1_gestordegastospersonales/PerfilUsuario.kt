package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Usuario
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class PerfilUsuario : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usuarioRef: FirebaseDatabase
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var etNombreCompleto: EditText
    private lateinit var etCorreo: EditText
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

        tvNombreCompleto = findViewById(R.id.tvUserName)
        tvCorreo = findViewById(R.id.tvUserEmail)
        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        etCorreo = findViewById(R.id.etCorreo)
        val btnCerrarSesion: Button = findViewById(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
            Toast.makeText(
                baseContext,
                "Sesión cerrada",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, IniciarSesion::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        if (!usuarioLogueado()){
            Toast.makeText(
                baseContext,
                "No hay usuario logueado",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        cargarDatosusuario()

        setupBackButton()
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

    fun usuarioLogueado(): Boolean {
        return auth.currentUser != null
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}