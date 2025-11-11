package alcala.jose.proyectofinal_eq1_gestordegastospersonales.entidades

data class Usuario(var id: String? = null,
                   var nombre: String? = null,
                   var apellido: String? = null,
                   var correo: String? = null,
                   var fechaNacimiento: String? = null)