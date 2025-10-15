package alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades

import java.io.Serializable


data class Movimiento(
    val id: Int,
    val descripcion: String,
    val categoria: String,
    val monto: Double,
    val fecha: String,
    val hora: String,
    val tipo: TipoMovimiento, // GASTO o INGRESO
    val metodoPago: MetodoPago,
    val iconoRes: Int = 0 // Recurso del icono
) : Serializable {

    fun getMontoFormateado(): String {
        return if (tipo == TipoMovimiento.INGRESO) {
            String.format("+$%,.2f", monto)
        } else {
            String.format("-$%,.2f", monto)
        }
    }

    fun getCategoriaYHora(): String {
        return "$categoria • $hora"
    }

    fun esIngreso(): Boolean {
        return tipo == TipoMovimiento.INGRESO
    }
}

enum class TipoMovimiento {
    GASTO,
    INGRESO
}

enum class MetodoPago{
    TARJETA,
    EFECTIVO
}