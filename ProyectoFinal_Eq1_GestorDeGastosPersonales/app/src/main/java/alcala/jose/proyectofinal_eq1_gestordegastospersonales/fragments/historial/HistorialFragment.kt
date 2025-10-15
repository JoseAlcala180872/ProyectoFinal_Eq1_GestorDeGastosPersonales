package alcala.jose.proyectofinal_eq1_gestordegastospersonales.fragments.historial

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.DetalleMovimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.PerfilUsuario
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.MetodoPago
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.TipoMovimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles.MovimientoAdapter
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistorialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistorialFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovimientoAdapter

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        setupUserIconClick(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        cargarMovimientosDePrueba()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistorialFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistorialFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupUserIconClick(view: View) {
        val iconUser: ImageView = view.findViewById(R.id.nav_configuracion)

        iconUser.setOnClickListener {
            val intent = Intent(requireContext(), PerfilUsuario::class.java)
            startActivity(intent)
        }

        val userName: TextView = view.findViewById(R.id.user_name)
        userName.setOnClickListener {
            val intent = Intent(requireContext(), PerfilUsuario::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.transaction_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MovimientoAdapter(emptyList()) { movimiento ->
            val intent = Intent(requireContext(), DetalleMovimiento::class.java)
            intent.putExtra("movimiento", movimiento)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun cargarMovimientosDePrueba() {
        val movimientos = listOf(
            Movimiento(
                id = 1,
                descripcion = "Este movimiento es pq tenia hambre bro T.T",
                categoria = "Alimentación",
                monto = 500.00,
                fecha = "10/05/25",
                hora = "14:30",
                tipo = TipoMovimiento.GASTO,
                metodoPago = MetodoPago.EFECTIVO,
                iconoRes = R.drawable.ic_add_24
            ),Movimiento(
                id = 2,
                descripcion = "Este movimiento es pq ya me quedare sin comida denuevo bro T.T",
                categoria = "Alimentación",
                monto = 85000000.00,
                fecha = "10/20/25",
                hora = "14:30",
                tipo = TipoMovimiento.INGRESO,
                metodoPago = MetodoPago.TARJETA,
                iconoRes = R.drawable.ic_add_24
            )
        )

        adapter.actualizarMovimientos(movimientos)
    }
}