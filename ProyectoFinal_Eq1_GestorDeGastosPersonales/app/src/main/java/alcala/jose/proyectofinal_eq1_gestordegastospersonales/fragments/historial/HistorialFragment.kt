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
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    private lateinit var userNameTextView: TextView
    private val auth = FirebaseAuth.getInstance()
    private val dbMovimientos = FirebaseDatabase.getInstance().getReference("movimientos")
    private val dbUsuarios = FirebaseDatabase.getInstance().getReference("usuarios")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        setupUserHeader(view)
        setupRecyclerView(view)
//        cargarUsuarioActual()
        cargarMovimientosUsuario()

        return view
    }

    override fun onResume() {
        super.onResume()
        cargarUsuarioActual()
    }

    private fun setupUserHeader(view: View) {
        val iconUser: ImageView = view.findViewById(R.id.nav_configuracion)
        userNameTextView = view.findViewById(R.id.user_name)

        iconUser.setOnClickListener {
            val intent = Intent(requireContext(), PerfilUsuario::class.java)
            startActivity(intent)
        }

        userNameTextView.setOnClickListener {
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


    private fun cargarUsuarioActual() {
        val user = auth.currentUser
        if (user == null) {
            userNameTextView.text = "Invitado"
            return
        }

        dbUsuarios.child(user.uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").value?.toString() ?: "Usuario"
                    val apellido = snapshot.child("apellido").value?.toString() ?: ""
                    userNameTextView.text = "$nombre $apellido"
                } else {
                    userNameTextView.text = "Usuario"
                }
            }
            .addOnFailureListener {
                userNameTextView.text = "Usuario"
            }
    }

    private fun cargarMovimientosUsuario() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val movimientosRef = dbMovimientos.child(user.uid)
        movimientosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaMovimientos = mutableListOf<Movimiento>()

                for (movSnapshot in snapshot.children) {
                    val movimiento = movSnapshot.getValue(Movimiento::class.java)

                    val movimientoId = movSnapshot.key

                    if (movimiento != null && movimientoId != null) {
                        movimiento.id = movimientoId
                        listaMovimientos.add(movimiento)
                    }
                }

                if (listaMovimientos.isEmpty()) {
                    Toast.makeText(requireContext(), "No tienes movimientos registrados", Toast.LENGTH_SHORT).show()
                }

                val ordenados = listaMovimientos.sortedByDescending { it.fecha }
                adapter.actualizarMovimientos(ordenados)
            }

            override fun onCancelled(error: DatabaseError) {
                // Verificamos si el fragmento está agregado (attached) a la actividad
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Consulta cancelada o error de permisos", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("HistorialFragment", "Error en DB, pero el fragmento ya no está activo: ${error.message}")
                }
            }
        })
    }
}