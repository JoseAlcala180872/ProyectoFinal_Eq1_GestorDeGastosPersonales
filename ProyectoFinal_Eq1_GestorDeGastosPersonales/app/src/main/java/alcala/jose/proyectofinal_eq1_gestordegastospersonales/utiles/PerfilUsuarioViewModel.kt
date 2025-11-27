package alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Usuario
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class PerfilUsuarioViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val database = Firebase.database.getReference("usuarios")
    private val uid = auth.currentUser?.uid

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> = _usuario

    private val _estadoCarga = MutableLiveData<String>()
    val estadoCarga: LiveData<String> = _estadoCarga

    private val _resultadoActualizacion = MutableLiveData<String>()
    val resutladoActualizacion: LiveData<String> = _resultadoActualizacion

    init {
        cargarDatosDelusuario()
    }

    private fun cargarDatosDelusuario(){
        if (uid != null) {
            _estadoCarga.value = "CARGANDO"

            database.child(uid).get().addOnSuccessListener { snapshot ->
                if(snapshot.exists()){
                    val usuarioData = snapshot.getValue(Usuario::class.java)

                    if (usuarioData!= null){

                        _usuario.value = usuarioData!!
                        _estadoCarga.value = "EXITO"
                    }

                } else {
                    _estadoCarga.value = "ERROR: No se encontró información del usuario"
                }
            }.addOnFailureListener {
                _estadoCarga.value = "ERROR: ${it.message}"
            }
        }else {
            _estadoCarga.value = "ERROR: No hay usuario autenticado."
        }
    }

    // En PerfilUsuarioViewModel.kt

    fun guardarCambios(nuevoNombre: String, nuevoApellido: String, nuevoCorreo: String) {
        val usuarioActualAuth = auth.currentUser

        if (usuarioActualAuth == null || uid == null) {
            _resultadoActualizacion.value = "ERROR: Sesión no válida."
            return
        }

        _resultadoActualizacion.value = "CARGANDO"

        // Verificamos si el correo va a cambiar
        val cambioDeCorreo = usuarioActualAuth.email != nuevoCorreo

        if (!cambioDeCorreo) {
            // Si NO cambia el correo, pasamos 'false' (no cerrar sesión)
            actualizarSoloBaseDeDatos(nuevoNombre, nuevoApellido, nuevoCorreo, requiereCierreSesion = false)
        } else {
            // Si SÍ cambia, primero Auth...
            usuarioActualAuth.updateEmail(nuevoCorreo)
                .addOnSuccessListener {
                    // ...y luego BD, pasando 'true' (sí cerrar sesión)
                    actualizarSoloBaseDeDatos(nuevoNombre, nuevoApellido, nuevoCorreo, requiereCierreSesion = true)
                }
                .addOnFailureListener { e ->
                    // ... (Tu manejo de errores existente)
                    if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                        _resultadoActualizacion.value = "ERROR_SEGURIDAD"
                    } else {
                        _resultadoActualizacion.value = "ERROR Auth: ${e.message}"
                    }
                }
        }
    }

    // Actualizamos la función auxiliar para aceptar el parámetro booleano
    private fun actualizarSoloBaseDeDatos(nombre: String, apellido: String, correo: String, requiereCierreSesion: Boolean) {

        val actualizaciones = mapOf<String, Any>(
            "nombre" to nombre,
            "apellido" to apellido,
            "correo" to correo
        )

        database.child(uid!!).updateChildren(actualizaciones)
            .addOnSuccessListener {
                // AQUÍ ESTÁ EL TRUCO:
                // Si cambiamos el correo, mandamos una señal especial. Si no, la señal normal.
                if (requiereCierreSesion) {
                    _resultadoActualizacion.value = "EXITO_CON_LOGOUT"
                } else {
                    _resultadoActualizacion.value = "EXITO"
                    // Solo actualizamos el objeto local si NO vamos a salir
                    val usuarioActualizado = _usuario.value?.copy(nombre = nombre, apellido = apellido, correo = correo)
                    _usuario.value = usuarioActualizado!!
                }
            }
            .addOnFailureListener {
                _resultadoActualizacion.value = "ERROR BD: ${it.message}"
            }
    }
}