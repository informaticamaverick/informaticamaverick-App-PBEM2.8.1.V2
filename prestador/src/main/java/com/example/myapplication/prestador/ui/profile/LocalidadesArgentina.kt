package com.example.myapplication.prestador.ui.profile

data class Localidad(val nombre: String, val codigoPostal: String)

val LOCALIDADES_POR_PROVINCIA: Map<String, List<Localidad>> = mapOf(
    "Buenos Aires" to listOf(
        Localidad("La Plata", "1900"), Localidad("Mar del Plata", "7600"),
        Localidad("Bahia Blanca", "8000"), Localidad("Quilmes", "1878"),
        Localidad("Lanus", "1824"), Localidad("Lomas de Zamora", "1832"),
        Localidad("Moron", "1708"), Localidad("San Isidro", "1642"),
        Localidad("Tigre", "1648"), Localidad("Tandil", "7000"),
        Localidad("Junin", "6000"), Localidad("Pergamino", "2700"),
        Localidad("Zarate", "2800"), Localidad("San Nicolas de los Arroyos", "2900"),
        Localidad("Pilar", "1629"), Localidad("Merlo", "1722"),
        Localidad("Florencio Varela", "1888"), Localidad("Berazategui", "1880"),
        Localidad("Avellaneda", "1870"), Localidad("San Fernando", "1646"),
        Localidad("Tres Arroyos", "7500"), Localidad("Azul", "7300"),
        Localidad("Olavarria", "7400"), Localidad("Lujan", "6700"),
        Localidad("Campana", "2804"), Localidad("Necochea", "7630"),
        Localidad("Chivilcoy", "6620"), Localidad("Bragado", "6640"),
        Localidad("Ramos Mejia", "1704"), Localidad("San Justo", "1754"),
        Localidad("Ituzaingo", "1714"), Localidad("Haedo", "1706"),
        Localidad("Moreno", "1744"), Localidad("Jose C. Paz", "1665"),
        Localidad("Malvinas Argentinas", "1613"), Localidad("San Miguel", "1663")
    ),
    "CABA" to listOf(
        Localidad("Buenos Aires", "1000"), Localidad("Palermo", "1425"),
        Localidad("San Telmo", "1062"), Localidad("Belgrano", "1426"),
        Localidad("Caballito", "1406"), Localidad("Recoleta", "1112"),
        Localidad("Flores", "1406"), Localidad("Villa Urquiza", "1431"),
        Localidad("Nunez", "1429"), Localidad("Almagro", "1196"),
        Localidad("Balvanera", "1196"), Localidad("Mataderos", "1440"),
        Localidad("Villa Devoto", "1417"), Localidad("Saavedra", "1430"),
        Localidad("Puerto Madero", "1107"), Localidad("Microcentro", "1000"),
        Localidad("Villa Crespo", "1414"), Localidad("Chacarita", "1427"),
        Localidad("Colegiales", "1426"), Localidad("Paternal", "1416")
    ),
    "Catamarca" to listOf(
        Localidad("San Fernando del Valle de Catamarca", "4700"),
        Localidad("Santa Maria", "4142"), Localidad("Belen", "4730"),
        Localidad("Andalgala", "4740"), Localidad("Tinogasta", "5340"),
        Localidad("Frias", "4220"), Localidad("Recreo", "4300")
    ),
    "Chaco" to listOf(
        Localidad("Resistencia", "3500"), Localidad("Presidencia Roque Saenz Pena", "3700"),
        Localidad("Villa Angela", "3540"), Localidad("Charata", "3730"),
        Localidad("Barranqueras", "3503"), Localidad("Fontana", "3504"),
        Localidad("Quitilipi", "3580"), Localidad("Las Brenas", "3722")
    ),
    "Chubut" to listOf(
        Localidad("Rawson", "9103"), Localidad("Comodoro Rivadavia", "9000"),
        Localidad("Puerto Madryn", "9120"), Localidad("Trelew", "9100"),
        Localidad("Esquel", "9200"), Localidad("Rada Tilly", "9001"),
        Localidad("Gaiman", "9105"), Localidad("Sarmiento", "9020")
    ),
    "C\u00f3rdoba" to listOf(
        Localidad("C\u00f3rdoba", "5000"), Localidad("Villa Maria", "5900"),
        Localidad("Rio Cuarto", "5800"), Localidad("San Francisco", "2400"),
        Localidad("Alta Gracia", "5186"), Localidad("Villa Carlos Paz", "5152"),
        Localidad("Cosquin", "5166"), Localidad("La Falda", "5172"),
        Localidad("Cruz del Eje", "5230"), Localidad("Jesus Maria", "5220"),
        Localidad("Bell Ville", "2550"), Localidad("Laboulaye", "6120"),
        Localidad("Marcos Juarez", "2580"), Localidad("Rio Tercero", "5850"),
        Localidad("Mina Clavero", "5192"), Localidad("Dean Funes", "5200"),
        Localidad("Villa Allende", "5105"), Localidad("Unquillo", "5109")
    ),
    "Corrientes" to listOf(
        Localidad("Corrientes", "3400"), Localidad("Goya", "3450"),
        Localidad("Curuzu Cuatia", "3460"), Localidad("Mercedes", "3470"),
        Localidad("Paso de los Libres", "3230"), Localidad("Yapeyun", "3225"),
        Localidad("Santo Tome", "3590"), Localidad("Ituzaingo", "3302")
    ),
    "Entre R\u00edos" to listOf(
        Localidad("Parana", "3100"), Localidad("Concordia", "3200"),
        Localidad("Gualeguaychu", "2820"), Localidad("Concepcion del Uruguay", "3260"),
        Localidad("Victoria", "3153"), Localidad("Colon", "3280"),
        Localidad("Federacion", "3206"), Localidad("La Paz", "3190"),
        Localidad("Villaguay", "3240"), Localidad("Crespo", "3116")
    ),
    "Formosa" to listOf(
        Localidad("Formosa", "3600"), Localidad("Clorinda", "3616"),
        Localidad("Pirane", "3612"), Localidad("El Colorado", "3608"),
        Localidad("Ingeniero Juarez", "3636")
    ),
    "Jujuy" to listOf(
        Localidad("San Salvador de Jujuy", "4600"), Localidad("San Pedro de Jujuy", "4620"),
        Localidad("Libertador General San Martin", "4634"), Localidad("Palpala", "4612"),
        Localidad("La Quiaca", "4650"), Localidad("Tilcara", "4624"),
        Localidad("Humahuaca", "4630"), Localidad("Abra Pampa", "4640")
    ),
    "La Pampa" to listOf(
        Localidad("Santa Rosa", "6300"), Localidad("General Pico", "6360"),
        Localidad("Realico", "6305"), Localidad("Victorica", "6315"),
        Localidad("Eduardo Castex", "6380"), Localidad("Toay", "6301"),
        Localidad("General Acha", "6310")
    ),
    "La Rioja" to listOf(
        Localidad("La Rioja", "5300"), Localidad("Chilecito", "5360"),
        Localidad("Aimogasta", "5340"), Localidad("Chamical", "5380"),
        Localidad("Chepes", "5390"), Localidad("Villa Union", "5350")
    ),
    "Mendoza" to listOf(
        Localidad("Mendoza", "5500"), Localidad("San Rafael", "5600"),
        Localidad("Godoy Cruz", "5501"), Localidad("Maipu", "5515"),
        Localidad("Lujan de Cuyo", "5507"), Localidad("Las Heras", "5539"),
        Localidad("Guaymallen", "5521"), Localidad("Rivadavia", "5567"),
        Localidad("Malargue", "5613"), Localidad("Tunuyan", "5560"),
        Localidad("San Martin", "5570"), Localidad("Junin", "5571"),
        Localidad("General Alvear", "5620"), Localidad("Palmira", "5572")
    ),
    "Misiones" to listOf(
        Localidad("Posadas", "3300"), Localidad("Obera", "3360"),
        Localidad("Eldorado", "3380"), Localidad("Puerto Iguazu", "3370"),
        Localidad("Apostoles", "3316"), Localidad("Montecarlo", "3384"),
        Localidad("Jardin America", "3328"), Localidad("Leandro N. Alem", "3315"),
        Localidad("Bernardo de Irigoyen", "3376"), Localidad("Candelaria", "3307")
    ),
    "Neuqu\u00e9n" to listOf(
        Localidad("Neuqu\u00e9n", "8300"), Localidad("San Martin de los Andes", "8370"),
        Localidad("Zapala", "8340"), Localidad("Cutral Co", "8322"),
        Localidad("Junin de los Andes", "8371"), Localidad("Chos Malal", "8353"),
        Localidad("Villa La Angostura", "8407"), Localidad("Plottier", "8316"),
        Localidad("Centenario", "8309"), Localidad("Rincones de los Sauces", "8319")
    ),
    "R\u00edo Negro" to listOf(
        Localidad("Viedma", "8500"), Localidad("San Carlos de Bariloche", "8400"),
        Localidad("General Roca", "8332"), Localidad("Cipolletti", "8324"),
        Localidad("Allen", "8328"), Localidad("Villa Regina", "8336"),
        Localidad("El Bolson", "8430"), Localidad("Choele Choel", "8360"),
        Localidad("Sierra Grande", "8532"), Localidad("Las Grutas", "8520"),
        Localidad("Catriel", "8307"), Localidad("Cinco Saltos", "8303")
    ),
    "Salta" to listOf(
        Localidad("Salta", "4400"), Localidad("San Ramon de la Nueva Oran", "4530"),
        Localidad("Tartagal", "4560"), Localidad("Cafayate", "4427"),
        Localidad("General Guemes", "4420"), Localidad("Rosario de la Frontera", "4440"),
        Localidad("Metan", "4460"), Localidad("Joaquin V. Gonzalez", "4450"),
        Localidad("Embarcacion", "4550"), Localidad("Cachi", "4413")
    ),
    "San Juan" to listOf(
        Localidad("San Juan", "5400"), Localidad("Chimbas", "5413"),
        Localidad("Santa Lucia", "5409"), Localidad("Pocito", "5412"),
        Localidad("Caucete", "5441"), Localidad("Rawson", "5402"),
        Localidad("Rivadavia", "5413"), Localidad("9 de Julio", "5416"),
        Localidad("Jachal", "5460"), Localidad("Calingasta", "5444")
    ),
    "San Luis" to listOf(
        Localidad("San Luis", "5700"), Localidad("Villa Mercedes", "5730"),
        Localidad("Merlo", "5881"), Localidad("Justo Daract", "5731"),
        Localidad("La Toma", "5776"), Localidad("Quines", "5770"),
        Localidad("Arizona", "5752"), Localidad("Concarán", "5773")
    ),
    "Santa Cruz" to listOf(
        Localidad("Rio Gallegos", "9400"), Localidad("Caleta Olivia", "9011"),
        Localidad("Pico Truncado", "9015"), Localidad("Puerto Deseado", "9050"),
        Localidad("El Calafate", "9405"), Localidad("Puerto San Julian", "9310"),
        Localidad("Perito Moreno", "9040"), Localidad("Las Heras", "9013")
    ),
    "Santa Fe" to listOf(
        Localidad("Rosario", "2000"), Localidad("Santa Fe", "3000"),
        Localidad("Rafaela", "2300"), Localidad("Venado Tuerto", "2600"),
        Localidad("Reconquista", "3560"), Localidad("Santo Tome", "3016"),
        Localidad("Villa Gobernador Galvez", "2124"), Localidad("San Lorenzo", "2200"),
        Localidad("Esperanza", "3080"), Localidad("Canada de Gomez", "2500"),
        Localidad("Casilda", "2170"), Localidad("Galvez", "2252"),
        Localidad("Sunchales", "2322"), Localidad("Firmat", "2630"),
        Localidad("San Jorge", "2450"), Localidad("Villa Constitucion", "2900"),
        Localidad("Rufino", "6100"), Localidad("Tostado", "3570")
    ),
    "Santiago del Estero" to listOf(
        Localidad("Santiago del Estero", "4200"), Localidad("La Banda", "4300"),
        Localidad("Termas de Rio Hondo", "4220"), Localidad("Anatuya", "3760"),
        Localidad("Frias", "4220"), Localidad("Loreto", "4252"),
        Localidad("Villa Ojo de Agua", "4350"), Localidad("Quimili", "3752")
    ),
    "Tierra del Fuego" to listOf(
        Localidad("Ushuaia", "9410"), Localidad("Rio Grande", "9420"),
        Localidad("Tolhuin", "9423")
    ),
    "Tucum\u00e1n" to listOf(
        Localidad("San Miguel de Tucuman", "4000"), Localidad("Tafi Viejo", "4103"),
        Localidad("Concepcion", "4149"), Localidad("Banda del Rio Sali", "4006"),
        Localidad("Yerba Buena", "4107"), Localidad("Aguilares", "4158"),
        Localidad("Monteros", "4144"), Localidad("Simoca", "4130"),
        Localidad("Famailla", "4132"), Localidad("Lules", "4128"),
        Localidad("Alderetes", "4005"), Localidad("Las Talitas", "4009"),
        Localidad("Bella Vista", "4012"), Localidad("Tafi del Valle", "4137"),
        Localidad("Acheral", "4124"), Localidad("Graneros", "4162"),
        Localidad("Juan Bautista Alberdi", "4146"), Localidad("Burruyacu", "4100")
    )
)