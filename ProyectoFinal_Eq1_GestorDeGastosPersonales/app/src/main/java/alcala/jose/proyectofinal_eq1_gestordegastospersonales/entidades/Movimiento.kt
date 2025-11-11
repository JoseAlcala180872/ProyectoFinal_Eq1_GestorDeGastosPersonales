package alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.R
import com.google.firebase.database.Exclude
import java.io.Serializable


data class Movimiento(
    var descripcion: String? = null,
    var categoria: String? = null,
    var monto: Double = 0.0,
    var fecha: String? = null,
    var hora: String? = null,
    var tipo: TipoMovimiento = TipoMovimiento.GASTO,
    var metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    @get:Exclude var iconoRes: Int = 0
) : Serializable {

    @get:Exclude
    val montoFormateado: String
        get() = if (tipo == TipoMovimiento.INGRESO)
            String.format("+$%,.2f", monto)
        else
            String.format("-$%,.2f", monto)


    fun esIngreso(): Boolean = tipo == TipoMovimiento.INGRESO
}

enum class TipoMovimiento {
    GASTO,
    INGRESO
}

enum class MetodoPago{
    TARJETA,
    EFECTIVO
}