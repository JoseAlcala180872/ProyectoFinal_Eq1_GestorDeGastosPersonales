package alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades

data class Presupuesto(
    var id: String? = null,
    var userId: String? = null,
    var mes: String? = null,
    var total: Double = 0.0,
    var distribucionPorCategoria: Map<String, Double>? = null,
    var alertaPorcentaje: Double = 0.8
)