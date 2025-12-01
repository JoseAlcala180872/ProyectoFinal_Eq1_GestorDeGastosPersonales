package alcala.jose.proyectofinal_eq1_gestordegastospersonales.utiles

import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.Movimiento
import alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades.TipoMovimiento
import java.util.Locale

/**
 * Data class para encapsular los resultados de los cálculos de gastos.
 * @param totalGastos El monto total de todos los gastos.
 * @param porcentajes Un mapa que contiene el nombre de la categoría y su porcentaje sobre el total.
 */
data class ResultadosGastos(
    val totalGastos: Double,
    val porcentajes: Map<String, Double>
)

/**
 * Clase de utilidad para realizar cálculos sobre listas de movimientos.
 */
object CalculadoraGastos {

    /**
     * Calcula el total de gastos y el porcentaje que representa cada categoría.
     * @param movimientos La lista completa de movimientos del usuario.
     * @param categorias La lista de categorías predefinidas a calcular.
     * @return Un objeto [ResultadosGastos] con el total y los porcentajes por categoría.
     */
    fun calcular(movimientos: List<Movimiento>, categorias: List<String>): ResultadosGastos {
        // 1. Filtrar solo los movimientos que son gastos
        val gastos = movimientos.filter { it.tipo == TipoMovimiento.GASTO }

        // 2. Calcular el total gastado
        val totalGastos = gastos.sumOf { it.monto }

        // 3. Agrupar y sumar los gastos por categoría, capitalizando el nombre para que coincida.
        val gastosPorCategoria = gastos.groupBy { it.categoria}
            .mapValues { entry -> entry.value.sumOf { it.monto } }
        
        // 4. Calcular el porcentaje para cada categoría predefinida
        val porcentajes = categorias.associateWith { categoria ->
            val gastoCategoria = gastosPorCategoria[categoria] ?: 0.0
            if (totalGastos > 0) {
                (gastoCategoria / totalGastos) * 100
            } else {
                0.0 // Evitar división por cero si no hay gastos
            }
        }

        return ResultadosGastos(totalGastos, porcentajes)
    }
}
