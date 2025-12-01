package alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades

data class Presupuesto(
    val nombre: String,
    var presupuesto: Double = 0.0,
    var gastado: Double = 0.0,
    val iconoResId: Int // R.drawable.ic_food, etc.
)