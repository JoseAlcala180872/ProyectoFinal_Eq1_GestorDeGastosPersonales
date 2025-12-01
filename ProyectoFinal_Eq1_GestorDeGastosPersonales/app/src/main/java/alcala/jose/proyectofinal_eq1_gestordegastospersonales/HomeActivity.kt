package alcala.jose.proyectofinal_eq1_gestordegastospersonales

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.gasto.RegistrarGastoFragment
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.historial.HistorialFragment
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.ingreso.RegistrarIngresoFragment
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.presupuesto.PresupuestoFragment
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navGasto: LinearLayout
    private lateinit var navIngreso: LinearLayout
    private lateinit var navPresupuesto: LinearLayout
    private lateinit var navGraficos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupNavigation()

        val movimientoAEditar = intent.getSerializableExtra("movimiento_a_editar") as? Movimiento
        val tipoFormulario = intent.getStringExtra("tipo_formulario")

        if (movimientoAEditar != null && tipoFormulario != null) {
            cargarFragmentoParaEdicion(movimientoAEditar, tipoFormulario)
        } else  if (savedInstanceState == null) {
            loadFragment(HistorialFragment())
            setActiveNavButton(navHome)
        }
    }

    private fun setupNavigation() {
        navHome = findViewById(R.id.nav_home)
        navGasto = findViewById(R.id.nav_gasto)
        navIngreso = findViewById(R.id.nav_ingreso)
        navPresupuesto = findViewById(R.id.nav_presupuesto)
        navGraficos = findViewById(R.id.nav_graficos)

        navHome.setOnClickListener {
            loadFragment(HistorialFragment())
            setActiveNavButton(navHome)
        }

        navGasto.setOnClickListener {
            loadFragment(RegistrarGastoFragment())
            setActiveNavButton(navGasto)
        }

        navIngreso.setOnClickListener {
            loadFragment(RegistrarIngresoFragment())
            setActiveNavButton(navIngreso)
        }

        navPresupuesto.setOnClickListener {
            loadFragment(PresupuestoFragment())
            setActiveNavButton(navPresupuesto)
        }

        navGraficos.setOnClickListener {
            loadFragment(GraficasFragment())
            setActiveNavButton(navGraficos)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setActiveNavButton(activeButton: LinearLayout) {
        val navButtons = listOf(navHome, navGasto, navIngreso, navPresupuesto, navGraficos)
        navButtons.forEach { button ->
            button.background = null
        }
        activeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moradoClaro))
    }

    private fun cargarFragmentoParaEdicion(movimiento: Movimiento, tipo: String) {

        val fragment: Fragment = when (tipo) {
            "INGRESO" -> RegistrarIngresoFragment()
            "GASTO" -> RegistrarGastoFragment()
            else -> {
                Toast.makeText(this, "Error: Tipo de movimiento desconocido.", Toast.LENGTH_SHORT).show()
                loadFragment(HistorialFragment())
                setActiveNavButton(navHome)
                return
            }
        }

        fragment.arguments = Bundle().apply {
            putSerializable("movimiento_a_editar", movimiento)
        }

        loadFragment(fragment)

        val activeNavButton = if (tipo == "INGRESO") navIngreso else navGasto
        setActiveNavButton(activeNavButton)
    }
}