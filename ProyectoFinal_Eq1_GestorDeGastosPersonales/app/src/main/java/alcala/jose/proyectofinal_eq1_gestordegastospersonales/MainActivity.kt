package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var btnIngresar: Button = findViewById(R.id.btnIngresar)
        var registro: TextView=findViewById(R.id.registro)

        btnIngresar.setOnClickListener {
            var intent: Intent= Intent(this, IniciarSesion::class.java)
            startActivity(intent)
        }

        registro.setOnClickListener {
            var intent: Intent= Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }
}