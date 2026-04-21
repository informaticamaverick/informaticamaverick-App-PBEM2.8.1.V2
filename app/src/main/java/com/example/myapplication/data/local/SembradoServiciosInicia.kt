package com.example.myapplication.data.local

import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.local.CategoryItem

/**
 * --- SEMBRADO INICIAL DE SERVICIOS (CATEGORÍAS) ---
 * Este archivo contiene el diccionario maestro de categorías y supercategorías.
 * 
 * REFACTORIZACIÓN:
 * 1. Colores: Se eliminaron de los constructores individuales. Ahora se heredan del ViewModel.
 * 2. Descripciones: Se añadió un detalle profesional a cada categoría para mejorar la UX.
 * 3. Integridad: Se mantienen nombres, emojis y pertenencia a supercategorías intactos.
 */

// ==================================================================================
// 🔥 SECCIÓN 1: DICCIONARIO DE ICONOS DE SUPERCATEGORÍAS
// ==================================================================================
val superCategoryIconsMap = mapOf(
    "Salud y Medicina" to "⚕️",
    "Bienestar y Terapias Alternativas" to "🌿",
    "Cuidado Personal y Belleza" to "✨",
    "Moda y Textil" to "👗",
    "Hogar y Mantenimiento" to "🏠",
    "Construcción y Oficios Pesados" to "🏗️",
    "Limpieza y Saneamiento" to "🧹",
    "Jardinería y Paisajismo" to "🌳",
    "Tecnología y Sistemas" to "💻",
    "Deporte y Recreación" to "⚽",
    "Eventos y Entretenimiento" to "🎉",
    "Gastronomía y Bares" to "🍔",
    "Transporte y Logística" to "🚛",
    "Servicios Automotores" to "🚗",
    "Servicios Profesionales y Legales" to "⚖️",
    "Servicios Ingenieria" to "🛠️",
    "Finanzas y Negocios" to "📈",
    "Marketing, Diseño y Medios" to "📱",
    "Ciencias y Humanidades" to "🔬",
    "Educación y Clases" to "📚",
    "Cuidado y Asistencia" to "🤝",
    "Mascotas y Veterinaria" to "🐾",
    "Turismo y Hotelería" to "✈️",
    "Seguridad y Emergencias" to "🚨",
    "Agricultura y Ganadería" to "🌾",
    "Esoterismo" to "🔮"
)

// ==================================================================================
// 🛠️ SECCIÓN 2: MODELO DE DATOS DE CATEGORÍA (REFACTORIZADO)
// ==================================================================================
data class CategoryItem(
    val name: String,
    val icon: String, // Emoji
    val superCategory: String,
    
    // [DESCRIPCIÓN] Detalle descriptivo para la categoría
    val description: String = "",

    // Búsqueda automática del icono de supercategoría
    val superCategoryIcon: String = superCategoryIconsMap[superCategory] ?: "📂",

    val providerIds: MutableList<String> = mutableListOf(),
    val isNew: Boolean = Math.random() < 0.2,
    val isNewPrestador: Boolean = Math.random() < 0.15,
    val isAd: Boolean = false,
    // [NUEVO] Estado de favorito inicial para el sembrado
    val isFavorite: Boolean = false
)

// ==================================================================================
// 🚀 SECCIÓN 3: OBJETO DE SEMBRADO (LISTADO MAESTRO CON DESCRIPCIONES)
// ==================================================================================
object SembradoServiciosInicia {
    val categories = listOf(

        // --------------------------------------------------------------------------
        // ⚕️ SUPERCATEGORÍA: SALUD Y MEDICINA
        // --------------------------------------------------------------------------
        CategoryItem("Médico Clínico", "🩺", "Salud y Medicina", "Atención médica integral para adultos, diagnósticos y chequeos preventivos."),
        CategoryItem("Pediatra", "👶", "Salud y Medicina", "Cuidado especializado para bebés, niños y adolescentes."),
        CategoryItem("Cardiólogo", "❤️", "Salud y Medicina", "Especialistas en la salud del corazón y el sistema circulatorio."),
        CategoryItem("Odontólogo", "🦷", "Salud y Medicina", "Cuidado dental general, limpiezas y tratamientos preventivos."),
        CategoryItem("Ortodoncista", "😁", "Salud y Medicina", "Especialistas en alineación dental y ortodoncia correctiva."),
        CategoryItem("Odontopediatra", "🧒", "Salud y Medicina", "Odontología especializada para el cuidado dental infantil."),
        CategoryItem("Psicólogo", "🧠", "Salud y Medicina", "Apoyo terapéutico y salud mental para individuos y parejas."),
        CategoryItem("Psiquiatra", "🛋️", "Salud y Medicina", "Tratamiento médico de trastornos mentales y emocionales."),
        CategoryItem("Psicopedagogo", "🧩", "Salud y Medicina", "Diagnóstico y tratamiento de dificultades del aprendizaje."),
        CategoryItem("Nutricionista", "🍇", "Salud y Medicina", "Asesoramiento dietético y planes de alimentación saludable."),
        CategoryItem("Fisioterapeutas / Kinesiólogo", "🏃", "Salud y Medicina", "Rehabilitación física y tratamiento de lesiones musculares."),
        CategoryItem("Enfermero", "🩹", "Salud y Medicina", "Servicios de enfermería a domicilio y cuidados asistenciales."),
        CategoryItem("Oncólogo", "🎗️", "Salud y Medicina", "Especialista en el diagnóstico y tratamiento del cáncer."),
        CategoryItem("Fonoaudiólogo", "🗣️", "Salud y Medicina", "Terapia de lenguaje, audición y trastornos de la voz."),
        CategoryItem("Oftalmólogo", "👁️", "Salud y Medicina", "Cuidado de la visión, exámenes oculares y cirugías."),
        CategoryItem("Traumatólogo", "🦴", "Salud y Medicina", "Tratamiento de lesiones óseas, fracturas y articulaciones."),
        CategoryItem("Dermatólogo", "🧴", "Salud y Medicina", "Cuidado de la piel, tratamiento de acné y afecciones cutáneas."),
        CategoryItem("Ginecólogo", "👩🏻‍⚕️️", "Salud y Medicina", "Salud reproductiva femenina y controles preventivos."),
        CategoryItem("Obstetra", "🤰", "Salud y Medicina", "Control del embarazo, parto y cuidado posparto."),
        CategoryItem("Urólogo", "👨🏻‍⚕️", "Salud y Medicina", "Salud del sistema urinario y aparato reproductor masculino."),
        CategoryItem("Neurólogo", "🧠", "Salud y Medicina", "Tratamiento de trastornos del cerebro y sistema nervioso."),
        CategoryItem("Neuropediatria", "🧠", "Salud y Medicina", "Neurología especializada en el desarrollo infantil."),
        CategoryItem("Endocrinólogo", "🩸", "Salud y Medicina", "Tratamiento de trastornos hormonales y metabólicos."),
        CategoryItem("Otorrinolaringólogo", "👂", "Salud y Medicina", "Especialista en oídos, nariz y garganta."),
        CategoryItem("Gastroenterólogo", "🤢", "Salud y Medicina", "Tratamiento del sistema digestivo y órganos asociados."),
        CategoryItem("Podólogo", "🦶", "Salud y Medicina", "Cuidado especializado de los pies y tratamiento de afecciones."),
        CategoryItem("Cirujano General", "😷", "Salud y Medicina", "Procedimientos quirúrgicos generales y de urgencia."),
        CategoryItem("Cirujano Plástico", "💉", "Salud y Medicina", "Cirugías estéticas y procedimientos reconstructivos."),
        CategoryItem("Alergista", "🤧", "Salud y Medicina", "Diagnóstico y tratamiento de alergias e inmunología."),
        CategoryItem("Óptica", "👓", "Salud y Medicina", "Venta y reparación de anteojos y lentes de contacto."),
        CategoryItem("Laboratorio Clínico", "🧪", "Salud y Medicina", "Análisis de sangre, orina y estudios diagnósticos."),
        CategoryItem("Farmacia", "💊", "Salud y Medicina", "Venta de medicamentos y productos de salud."),
        CategoryItem("Radiología", "🩻", "Salud y Medicina", "Estudios por imágenes, rayos X y tomografías."),
        CategoryItem("Ecografista", "🖥️", "Salud y Medicina", "Realización de ecografías y diagnósticos por ultrasonido."),
        CategoryItem("Anestesista", "😴", "Salud y Medicina", "Administración de anestesia para procedimientos quirúrgicos."),
        CategoryItem("Instrumentador Quirúrgico", "🔪", "Salud y Medicina", "Asistencia técnica en el quirófano durante cirugías."),
        CategoryItem("Hemoterapeuta", "🩸", "Salud y Medicina", "Especialistas en medicina transfusional y manejo de sangre."),
        CategoryItem("Geriatra", "🧓", "Salud y Medicina", "Atención médica especializada para personas mayores."),
        CategoryItem("Urgencias Médicas", "👨🏻‍⚕️", "Salud y Medicina", "Servicio de atención rápida ante emergencias de salud."),

        // --------------------------------------------------------------------------
        // 🌿 SUPERCATEGORÍA: BIENESTAR Y TERAPIAS ALTERNATIVAS
        // --------------------------------------------------------------------------
        CategoryItem("Quiropráctico", "🦴", "Bienestar y Terapias Alternativas", "Ajustes de la columna para mejorar el sistema nervioso."),
        CategoryItem("Osteópata", "👐", "Bienestar y Terapias Alternativas", "Tratamiento integral del cuerpo mediante manipulación física."),
        CategoryItem("Acupuntura", "📍", "Bienestar y Terapias Alternativas", "Técnica milenaria para el equilibrio energético y alivio del dolor."),
        CategoryItem("Terapeuta Reiki", "✨", "Bienestar y Terapias Alternativas", "Canalización de energía para la armonización y el bienestar."),
        CategoryItem("Homeopatía", "🌿", "Bienestar y Terapias Alternativas", "Tratamientos naturales basados en la medicina homeopática."),
        CategoryItem("Reflexología", "🦶", "Bienestar y Terapias Alternativas", "Masaje terapéutico en puntos reflejos de pies y manos."),
        CategoryItem("Aromaterapia", "🌺", "Bienestar y Terapias Alternativas", "Uso de aceites esenciales para mejorar la salud física y mental."),
        CategoryItem("Flores de Bach", "🌼", "Bienestar y Terapias Alternativas", "Terapia floral para el equilibrio de las emociones."),
        CategoryItem("Biodescodificación", "🧬", "Bienestar y Terapias Alternativas", "Búsqueda del origen emocional de las enfermedades físicas."),
        CategoryItem("Terapia Holística", "☯️", "Bienestar y Terapias Alternativas", "Enfoque integral que une cuerpo, mente y espíritu."),
        CategoryItem("Masajes Descontracturantes", "💆‍♂️", "Bienestar y Terapias Alternativas", "Alivio de tensiones musculares y estrés acumulado."),

        // --------------------------------------------------------------------------
        // 💅 SUPERCATEGORÍA: CUIDADO PERSONAL Y BELLEZA
        // --------------------------------------------------------------------------
        CategoryItem("Peluquería", "✂️", "Cuidado Personal y Belleza", "Corte, color y peinado para un estilo renovado."),
        CategoryItem("Barbería", "💈", "Cuidado Personal y Belleza", "Cuidado de barba y cortes masculinos tradicionales."),
        CategoryItem("Estilista", "💇‍♀️", "Cuidado Personal y Belleza", "Asesoramiento de imagen y servicios de belleza integral."),
        CategoryItem("Maquillaje Profesional", "💄", "Cuidado Personal y Belleza", "Maquillaje para eventos, bodas y sesiones fotográficas."),
        CategoryItem("Manicura y Uñas Esculpidas", "💅", "Cuidado Personal y Belleza", "Cuidado de manos y diseños creativos en uñas."),
        CategoryItem("Pedicuro", "🦶", "Cuidado Personal y Belleza", "Tratamiento estético y cuidado preventivo de los pies."),
        CategoryItem("Cosmetólogo", "🧴", "Cuidado Personal y Belleza", "Tratamientos faciales y cuidado de la salud de la piel."),
        CategoryItem("Cosmiatría", "💆‍♀️", "Cuidado Personal y Belleza", "Tratamientos estéticos avanzados y correctivos faciales."),
        CategoryItem("Depilación", "🦵", "Cuidado Personal y Belleza", "Eliminación de vello con cera o métodos tradicionales."),
        CategoryItem("Depilación Definitiva (Láser)", "🔦", "Cuidado Personal y Belleza", "Tratamiento de reducción de vello permanente con tecnología láser."),
        CategoryItem("Perfilado de Cejas", "👁️‍🗨️", "Cuidado Personal y Belleza", "Diseño y definición de la mirada mediante el arco de las cejas."),
        CategoryItem("Lifting de Pestañas", "👁️", "Cuidado Personal y Belleza", "Tratamiento para arquear y dar volumen a las pestañas naturales."),
        CategoryItem("Microblading", "✒️", "Cuidado Personal y Belleza", "Pigmentación semipermanente para el diseño de cejas."),
        CategoryItem("Tatuador", "🖋️", "Cuidado Personal y Belleza", "Arte en la piel con diseños personalizados y artísticos."),
        CategoryItem("Piercer", "🧷", "Cuidado Personal y Belleza", "Colocación profesional de piercings y joyería corporal."),
        CategoryItem("Spa y Relax", "🧖‍♀️", "Cuidado Personal y Belleza", "Circuitos de relajación, saunas y tratamientos de bienestar."),
        CategoryItem("Cama Solar", "😎", "Cuidado Personal y Belleza", "Bronceado controlado con equipos de rayos UV."),
        CategoryItem("Centro de Estética", "🏥", "Cuidado Personal y Belleza", "Tratamientos corporales y faciales de alta complejidad."),

        // --------------------------------------------------------------------------
        // 👗 SUPERCATEGORÍA: MODA Y TEXTIL
        // --------------------------------------------------------------------------
        CategoryItem("Asesor de Imagen", "🤩", "Moda y Textil", "Consultoría personalizada para potenciar tu estilo y presencia."),
        CategoryItem("Modista", "👗", "Moda y Textil", "Confección de prendas a medida y arreglos complejos."),
        CategoryItem("Sastre", "👔", "Moda y Textil", "Alta costura masculina, trajes y prendas de etiqueta."),
        CategoryItem("Costurera", "🪡", "Moda y Textil", "Arreglos de ropa en general, cambios de cierres y dobladillos."),
        CategoryItem("Alta Costura", "✨", "Moda y Textil", "Diseño y confección de vestidos de gala y eventos especiales."),
        CategoryItem("Zapatero", "👞", "Moda y Textil", "Reparación de calzado, cambio de suelas y limpieza."),
        CategoryItem("Marroquinería", "👜", "Moda y Textil", "Reparación de bolsos, carteras y artículos de cuero."),
        CategoryItem("Joyero", "💎", "Moda y Textil", "Diseño, reparación y tasación de joyas y relojes."),
        CategoryItem("Diseñador de Modas", "📐", "Moda y Textil", "Creación de colecciones y diseño textil creativo."),
        CategoryItem("Venta de Ropa", "👕", "Moda y Textil", "Tiendas de indumentaria multimarca o de diseño."),
        CategoryItem("Estampado y Bordado", "🖨️", "Moda y Textil", "Personalización de prendas mediante diversas técnicas."),
        CategoryItem("Disfraces", "🦸", "Moda y Textil", "Alquiler y venta de trajes temáticos y caracterizaciones."),
        CategoryItem("Serigrafía", "🎨", "Moda y Textil", "Impresión artesanal sobre telas y diversos soportes."),

        // 🏠 SUPERCATEGORÍA: HOGAR Y MANTENIMIENTO
        // Se marcan como favoritas por defecto para cumplir con el plan de priorización inicial
        // --------------------------------------------------------------------------
        CategoryItem("Plomería", "🪠", "Hogar y Mantenimiento", "Reparación de tuberías, filtraciones e instalaciones sanitarias.", isFavorite = true),
        CategoryItem("Electricista", "⚡", "Hogar y Mantenimiento", "Instalaciones eléctricas residenciales y reparaciones de urgencia.", isFavorite = true),
        CategoryItem("Gasista", "🔥", "Hogar y Mantenimiento", "Instalación y mantenimiento de estufas, termotanques y redes de gas.", isFavorite = true),
        CategoryItem("Carpintería", "🪚", "Hogar y Mantenimiento", "Muebles a medida, reparaciones en madera y aberturas.", isFavorite = true),
        CategoryItem("Cerrajero", "🔑", "Hogar y Mantenimiento", "Apertura de puertas, cambio de cerraduras y llaves de seguridad.", isFavorite = true),
        CategoryItem("Vidriero", "🪟", "Hogar y Mantenimiento", "Colocación de vidrios, espejos y cerramientos de cristal."),
        CategoryItem("Aire Acondicionado (Técnico)", "❄️", "Hogar y Mantenimiento", "Instalación, carga de gas y mantenimiento preventivo."),
        CategoryItem("Refrigeración (Técnico)", "🧊", "Hogar y Mantenimiento", "Reparación de heladeras residenciales y comerciales."),
        CategoryItem("Estufas y Calefacción", "♨️", "Hogar y Mantenimiento", "Mantenimiento de sistemas de calefacción y calderas."),
        CategoryItem("Electrodomésticos (Técnico)", "🔌", "Hogar y Mantenimiento", "Reparación de lavarropas, microondas y pequeños artefactos."),
        CategoryItem("Mantenimiento de Piscinas", "🏊‍♂️", "Hogar y Mantenimiento", "Limpieza, filtrado y balance químico del agua."),
        CategoryItem("Paneles Solares (Técnico)", "☀️", "Hogar y Mantenimiento", "Instalación de sistemas de energía solar fotovoltaica."),
        CategoryItem("Diseño de Interiores", "🛋️", "Hogar y Mantenimiento", "Planificación de espacios, decoración y ambientación."),
        CategoryItem("Tapicero", "🛋️", "Hogar y Mantenimiento", "Renovación de tapizados de sillas, sillones y cabeceras."),
        CategoryItem("Mueblería a Medida", "🪑", "Hogar y Mantenimiento", "Diseño exclusivo de mobiliario para cada ambiente."),
        CategoryItem("Domótica para el Hogar", "🏡", "Hogar y Mantenimiento", "Automatización de luces, persianas y sistemas inteligentes."),
        CategoryItem("Service de Calderas", "♨️", "Hogar y Mantenimiento", "Mantenimiento técnico especializado para sistemas de calefacción central."),
        CategoryItem("Armado de Muebles", "🛠️", "Hogar y Mantenimiento", "Ensamblado profesional de muebles en kit (estilo caja)."),
        CategoryItem("Instalación de Redes de Protección", "🥅", "Hogar y Mantenimiento", "Colocación de mallas de seguridad para balcones y ventanas."),
        CategoryItem("Pulido y Plastificado", "✨", "Hogar y Mantenimiento", "Restauración, lijado y laqueado de pisos de madera y parquet."),
        CategoryItem("Limpieza de Tanques de Agua", "🚰", "Hogar y Mantenimiento", "Mantenimiento higiénico y desinfección de depósitos de agua potable."),
        CategoryItem("Carpintería de Aluminio", "🪟", "Hogar y Mantenimiento", "Fabricación e instalación de aberturas y cerramientos de aluminio."),
        // --------------------------------------------------------------------------
        // 🏗️ SUPERCATEGORÍA: CONSTRUCCIÓN Y OFICIOS PESADOS
        // --------------------------------------------------------------------------
        CategoryItem("Albañil", "🧱", "Construcción y Oficios Pesados", "Construcción en seco y tradicional, revoques y mampostería."),
        CategoryItem("Maestro Mayor de Obra", "🏗️", "Construcción y Oficios Pesados", "Dirección de obra y firma de planos para construcciones."),
        CategoryItem("Contratista", "📋", "Construcción y Oficios Pesados", "Gestión integral de cuadrillas y suministros de obra."),
        CategoryItem("Obrero", "👷", "Construcción y Oficios Pesados", "Mano de obra especializada en tareas de construcción."),
        CategoryItem("Pintor de Obras", "🖌️", "Construcción y Oficios Pesados", "Pintura de interiores, fachadas y tratamientos de paredes."),
        CategoryItem("Techista", "🏠", "Construcción y Oficios Pesados", "Colocación y reparación de techos de teja, chapa y losa."),
        CategoryItem("Herrero de Obra", "⚒️", "Construcción y Oficios Pesados", "Fabricación de rejas, portones y estructuras metálicas."),
        CategoryItem("Soldador de Obra", "🔥", "Construcción y Oficios Pesados", "Soldadura profesional en hierro, acero e inoxidable."),
        CategoryItem("Tornero", "⚙️", "Construcción y Oficios Pesados", "Mecanizado de piezas metálicas con alta precisión."),
        CategoryItem("Yesero / Durlock", "🏢", "Construcción y Oficios Pesados", "Instalación de cielorrasos y tabiques de cartón yeso."),
        CategoryItem("Pisos y Revestimientos", "🔲", "Construcción y Oficios Pesados", "Colocación de cerámicos, porcelanatos y pisos flotantes."),
        CategoryItem("Impermeabilización", "☔", "Construcción y Oficios Pesados", "Tratamiento de humedad en techos, terrazas y cimientos."),
        CategoryItem("Zinguería", "🏗️", "Construcción y Oficios Pesados", "Instalación de canaletas, babetas y conductos de ventilación."),
        CategoryItem("Marmolería", "🪨", "Construcción y Oficios Pesados", "Mesadas de granito, mármol y cuarzo para cocina y baño."),
        CategoryItem("Ascensores (Instalación y Mantenimiento)", "🛗", "Construcción y Oficios Pesados", "Servicio técnico para elevadores y montacargas."),
        CategoryItem("Operador de Grúa / Maquinaria", "🏗️", "Construcción y Oficios Pesados", "Manejo experto de maquinaria pesada para construcción."),
        CategoryItem("Agrimensor", "🗺️", "Construcción y Oficios Pesados", "Medición de terrenos, loteos y certificaciones de parcelas."),
        CategoryItem("Topógrafo", "🔭", "Construcción y Oficios Pesados", "Estudio de superficies y relieve para proyectos de ingeniería."),
        CategoryItem("Arquitecto", "📐", "Construcción y Oficios Pesados", "Diseño arquitectónico, planificación urbana y gestión de obras."),

        // --------------------------------------------------------------------------
        // 🧹 SUPERCATEGORÍA: LIMPIEZA Y SANEAMIENTO
        // --------------------------------------------------------------------------
        CategoryItem("Limpieza Doméstica", "🧹", "Limpieza y Saneamiento", "Servicio de limpieza general para casas y departamentos."),
        CategoryItem("Limpieza de Obras / Final de Obra", "🏗️", "Limpieza y Saneamiento", "Remoción de residuos y limpieza profunda tras construcción."),
        CategoryItem("Limpieza de Vidrios en Altura", "🪟", "Limpieza y Saneamiento", "Limpieza profesional de ventanales en edificios y oficinas."),
        CategoryItem("Limpieza de Tapizados y Alfombras", "🛋️", "Limpieza y Saneamiento", "Lavado profundo de sillones, alfombras y butacas de autos."),
        CategoryItem("Fumigación y Control de Plagas", "💨", "Limpieza y Saneamiento", "Eliminación de insectos, roedores y desinsectación general."),
        CategoryItem("Desinfección de Ambientes", "🦠", "Limpieza y Saneamiento", "Sanitización de espacios contra virus y bacterias."),
        CategoryItem("Lavadero de Ropa / Tintorería", "🧺", "Limpieza y Saneamiento", "Lavado, secado y planchado de prendas delicadas."),

        // --------------------------------------------------------------------------
        // 🌳 SUPERCATEGORÍA: JARDINERÍA Y PAISAJISMO
        // --------------------------------------------------------------------------
        CategoryItem("Jardinería", "🌿", "Jardinería y Paisajismo", "Mantenimiento general de plantas, canteros y espacios verdes."),
        CategoryItem("Paisajista", "🏞️", "Jardinería y Paisajismo", "Diseño estético de jardines y parques naturales."),
        CategoryItem("Poda de Árboles", "🪓", "Jardinería y Paisajismo", "Corte de ramas en altura y despeje de cables."),
        CategoryItem("Diseño de Exteriores", "🏡", "Jardinería y Paisajismo", "Planificación de patios, terrazas y mobiliario de exterior."),
        CategoryItem("Mantenimiento de Césped", "🌱", "Jardinería y Paisajismo", "Corte periódico y fertilización de superficies de grama."),

        // --------------------------------------------------------------------------
        // 💻 SUPERCATEGORÍA: TECNOLOGÍA Y SISTEMAS
        // -----------------------------------------
        CategoryItem("Desarrollador de Aplicaciones", "👨‍💻", "Tecnología y Sistemas", "Desarrollo de aplicaciones móviles y web para iOS y Android."),
        CategoryItem("PC y Notebooks (Técnico)", "💻", "Tecnología y Sistemas", "Reparación de hardware y optimización de sistemas operativos."),
        CategoryItem("Servidores (Técnico)", "🖥️", "Tecnología y Sistemas", "Administración de centros de datos y servidores corporativos."),
        CategoryItem("Impresoras (Técnico)", "🖨️", "Tecnología y Sistemas", "Mantenimiento de impresoras láser, tinta y multifunción."),
        CategoryItem("Celulares (Técnico)", "📱", "Tecnología y Sistemas", "Cambio de pantallas, baterías y reparación de placas."),
        CategoryItem("Redes e Internet (Técnico)", "🌐", "Tecnología y Sistemas", "Configuración de Wi-Fi, cableado estructurado y routers."),
        CategoryItem("Control de Accesos - Porteros (Técnico)", "🪪", "Tecnología y Sistemas", "Instalación de porteros visores y cerraduras biométricas."),
        CategoryItem("Seguridad Electronica (Técnico)", "🛡️", "Tecnología y Sistemas", "Sistemas integrados de monitoreo y protección perimetral."),
        CategoryItem("Cámaras de Seguridad (Técnico)", "📹", "Tecnología y Sistemas", "Instalación de sistemas CCTV y monitoreo remoto."),
        CategoryItem("Alarmas de Seguridad (Técnico)", "🚨", "Tecnología y Sistemas", "Configuración de paneles de alarma y sensores de movimiento."),
        CategoryItem("Desarrollador de Software", "👨‍💻", "Tecnología y Sistemas", "Creación de aplicaciones y sistemas a medida."),
        CategoryItem("Desarrollador PLC", "👨‍💻", "Tecnología y Sistemas", "Desarrollo de sistemas PLC para automatización de procesos"),
        CategoryItem("Desarrollador de Video Juegos", "🕹️", "Tecnología y Sistemas", "Programación y diseño de experiencias interactivas."),
        CategoryItem("Tester de Software", "💻", "Tecnología y Sistemas", "Control de calidad y detección de fallos en aplicaciones."),
        CategoryItem("Desarrollador de Web", "🌐", "Tecnología y Sistemas", "Diseño y programación de sitios web institucionales."),
        CategoryItem("Tester de Web", "🌐", "Tecnología y Sistemas", "Validación de funcionalidad y UX en plataformas online."),
        CategoryItem("Desarrollador de Android / iOS", "📱", "Tecnología y Sistemas", "Creación de aplicaciones nativas para smartphones."),
        CategoryItem("Tester de Android / iOS", "👨‍💻", "Tecnología y Sistemas", "Pruebas de rendimiento y usabilidad en dispositivos móviles."),
        CategoryItem("Desarrollador Frontend", "🎨", "Tecnología y Sistemas", "Programación de la interfaz visual del usuario."),
        CategoryItem("Desarrollador Backend", "⚙️", "Tecnología y Sistemas", "Programación de la lógica y base de datos del servidor."),
        CategoryItem("Desarrollador Fullstack", "🛠️", "Tecnología y Sistemas", "Experto en desarrollo tanto de cliente como de servidor."),
        CategoryItem("Analista de BigData", "📊", "Tecnología y Sistemas", "Procesamiento y análisis de grandes volúmenes de datos."),
        CategoryItem("Data Scientist", "📉", "Tecnología y Sistemas", "Modelado predictivo y extracción de conocimiento de datos."),
        CategoryItem("Machine Learning Engineer", "🤖", "Tecnología y Sistemas", "Desarrollo de algoritmos de inteligencia artificial."),
        CategoryItem("Ciberseguridad", "🛡️", "Tecnología y Sistemas", "Protección de datos y defensa contra ciberataques."),
        CategoryItem("DevOps", "♾️", "Tecnología y Sistemas", "Integración entre desarrollo y operaciones de software."),
        CategoryItem("QA Tester", "✅", "Tecnología y Sistemas", "Aseguramiento de la calidad en procesos tecnológicos."),
        CategoryItem("SysAdmin", "🗄️", "Tecnología y Sistemas", "Gestión y mantenimiento de infraestructura informática."),
        CategoryItem("Administrador de Bases de Datos", "💽", "Tecnología y Sistemas", "Diseño y gestión de almacenes de datos relacionales."),
        CategoryItem("Scrum Master / Project Manager", "📋", "Tecnología y Sistemas", "Liderazgo de equipos mediante metodologías ágiles."),
        CategoryItem("Informática (Técnico)", "🖥️", "Tecnología y Sistemas", "Soporte técnico general para usuarios y empresas."),
        CategoryItem("Reparación de Consolas (Técnico)", "🎮", "Tecnología y Sistemas", "Servicio técnico para PlayStation, Xbox y Nintendo."),
        CategoryItem("Impresión 3D", "🧊", "Tecnología y Sistemas", "Modelado y fabricación de piezas en materiales plásticos."),
        CategoryItem("Drones (Técnico)", "🚁", "Tecnología y Sistemas", "Mantenimiento y configuración de aeronaves no tripuladas."),
        CategoryItem("Gamer / Coach E-Sports", "🕹️", "Tecnología y Sistemas", "Entrenamiento profesional para competencias de videojuegos."),
        CategoryItem("Apple (Técnico)", "🍏", "Tecnología y Sistemas", "Especialista en reparación de iPhone, Mac e iPad."),
        CategoryItem("Soporte Técnico Remoto", "🎧", "Tecnología y Sistemas", "Asistencia informática inmediata a distancia para software y configuración."),
        CategoryItem("Especialista en IA", "🤖", "Tecnología y Sistemas", "Consultoría e integración de soluciones basadas en Inteligencia Artificial."),








        // -----------------------------------------
        // ⚽ SUPERCATEGORÍA: DEPORTE Y RECREACIÓN
        // -----------------------------------------
        CategoryItem("Natación Artística", "🏊", "Deporte y Recreación", "Entrenamiento en natación sincronizada y figuras artísticas en el agua."),
        CategoryItem("Waterpolo", "🤽🏽", "Deporte y Recreación", "Entrenamiento y práctica recreativa de polo acuático en equipo."),
        CategoryItem("Entrenamiento Funcional", "⏱️", "Deporte y Recreación", "Rutinas de ejercicios para mejorar la movilidad, fuerza y resistencia diaria."),
        CategoryItem("Entrenamiento de Cardio", "🏋️", "Deporte y Recreación", "Ejercicios aeróbicos enfocados en la salud cardiovascular y quema de calorías."),
        CategoryItem("Skatepark", "🛹", "Deporte y Recreación", "Clases de skate, rollers y BMX en pistas especializadas."),
        CategoryItem("Surf / Kitesurf / Windsurf", "🏄", "Deporte y Recreación", "Deportes acuáticos de tabla y viento en mar o lagunas."),
        CategoryItem("Paracaidismo", "🪂", "Deporte y Recreación", "Saltos tándem y cursos de paracaidismo para principiantes y expertos."),
        CategoryItem("Parapente", "🪂", "Deporte y Recreación", "Vuelos biplaza guiados y cursos de iniciación al vuelo libre."),
        CategoryItem("Ciclismo de Montaña", "⛰️", "Deporte y Recreación", "Salidas guiadas y entrenamiento en senderos y terrenos naturales."),
        CategoryItem("Rafting / Kayak", "🚣🏽", "Deporte y Recreación", "Descenso de ríos y navegación en canoas o kayaks."),
        CategoryItem("Esgrima", " fencing", "Deporte y Recreación", "Práctica de esgrima con florete, espada o sable."),
        CategoryItem("HandBall", "🤾🏽", "Deporte y Recreación", "Entrenamiento y partidos de balonmano para todas las edades."),
        CategoryItem("Arquería", "🏹", "Deporte y Recreación", "Práctica de tiro con arco, puntería y concentración."),
        CategoryItem("Gimnasia Artística", "🤸🏽", "Deporte y Recreación", "Entrenamiento en aparatos, saltos y acrobacias de gimnasia."),
        CategoryItem("Tiro (Deporte)", "🎯", "Deporte y Recreación", "Práctica de tiro deportivo con armas de aire comprimido o fuego en polígono."),
        CategoryItem("Atletismo", "🏃", "Deporte y Recreación", "Carreras, saltos y lanzamientos en pista y campo."),
        CategoryItem("Béisbol / Sóftbol", "⚾", "Deporte y Recreación", "Práctica y entrenamiento de deportes de bate y campo."),
        CategoryItem("Cancha Fútbol 5", "⚽", "Deporte y Recreación", "Alquiler de canchas de césped sintético para fútbol reducido."),
        CategoryItem("Cancha Fútbol 7", "🥅", "Deporte y Recreación", "Espacios para partidos de fútbol con equipos medianos."),
        CategoryItem("Cancha Fútbol 11", "🏟️", "Deporte y Recreación", "Canchas reglamentarias para partidos profesionales o amateurs."),
        CategoryItem("Escuela Fútbol", "⚽", "Deporte y Recreación", "Entrenamiento infantil y juvenil en fundamentos del fútbol."),
        CategoryItem("Escuela Hockey", "🏑", "Deporte y Recreación", "Clases de hockey sobre césped para todas las edades."),
        CategoryItem("Cancha de Hockey", "🏑", "Deporte y Recreación", "Alquiler de predios para la práctica de hockey."),
        CategoryItem("Rugby", "🏉", "Deporte y Recreación", "Clubes y espacios para la práctica de rugby."),
        CategoryItem("Cancha de Pádel", "🎾", "Deporte y Recreación", "Alquiler de canchas de cristal o muro."),
        CategoryItem("Tenis", "🎾", "Deporte y Recreación", "Práctica de tenis en canchas de polvo de ladrillo o cemento."),
        CategoryItem("Tenis de Mesa (PingPong)", "🏓", "Deporte y Recreación", "Espacios para juego recreativo o competitivo de ping pong."),
        CategoryItem("Escuela Tenis", "🏸", "Deporte y Recreación", "Clases particulares y grupales de técnica de tenis."),
        CategoryItem("Golf", "⛳", "Deporte y Recreación", "Campos de golf y clases para principiantes."),
        CategoryItem("Cancha Básquet", "🏀", "Deporte y Recreación", "Espacios techados o al aire libre para básquet."),
        CategoryItem("Escuela Básquet", "🏀", "Deporte y Recreación", "Formación deportiva en básquetbol para jóvenes."),
        CategoryItem("Vóley", "🏐", "Deporte y Recreación", "Práctica de voley en canchas cubiertas o de playa."),
        CategoryItem("Gimnasio", "🏋️", "Deporte y Recreación", "Centros de musculación y entrenamiento cardiovascular."),
        CategoryItem("Crossfit", "🤸", "Deporte y Recreación", "Entrenamiento de alta intensidad y ejercicios funcionales."),
        CategoryItem("Entrenador Personal (Personal Trainer)", "💪", "Deporte y Recreación", "Rutinas personalizadas y seguimiento físico individual."),
        CategoryItem("Paintball", "🔫", "Deporte y Recreación", "Juegos de estrategia con marcadoras de pintura."),
        CategoryItem("Escuela de Baile", "💃", "Deporte y Recreación", "Clases de diversos ritmos: danza clásica, moderna y urbana."),
        CategoryItem("Salsa y Bachata", "🕺", "Deporte y Recreación", "Ritmos latinos para recreación y eventos sociales."),
        CategoryItem("Patinaje", "⛸️", "Deporte y Recreación", "Práctica de patín artístico o de velocidad."),
        CategoryItem("Boxeo", "🥊", "Deporte y Recreación", "Entrenamiento de combate y defensa personal."),
        CategoryItem("Artes Marciales Mixtas", "🏆", "Deporte y Recreación", "Entrenamiento integral de diversas disciplinas de combate."),
        CategoryItem("Taekwondo", "🥋", "Deporte y Recreación", "Disciplina marcial coreana enfocada en técnicas de patada."),
        CategoryItem("Karate", "🥋", "Deporte y Recreación", "Arte marcial tradicional japonés para todas las edades."),
        CategoryItem("Natación", "🏊", "Deporte y Recreación", "Clases de natación y pileta libre."),
        CategoryItem("Yoga", "🧘‍♀️", "Deporte y Recreación", "Prácticas de flexibilidad, respiración y meditación."),
        CategoryItem("Pilates", "🧘", "Deporte y Recreación", "Método de entrenamiento físico para fortalecer el core."),
        CategoryItem("Artes Marciales", "🥋", "Deporte y Recreación", "Entrenamiento en judo, jiu-jitsu y otras artes."),
        CategoryItem("Ciclismo", "🚴", "Deporte y Recreación", "Salidas grupales y entrenamiento en ciclismo de ruta o montaña."),
        CategoryItem("Trekking y Montañismo", "🧗", "Deporte y Recreación", "Excursiones guiadas a montañas y senderismo natural."),
        CategoryItem("Surf / Kitesurf", "🏄", "Deporte y Recreación", "Deportes acuáticos extremos en mar y lagunas."),
        CategoryItem("Buceo", "🤿", "Deporte y Recreación", "Cursos de buceo recreativo y expediciones submarinas."),
        CategoryItem("Alquiler de Bicicletas", "🚲", "Deporte y Recreación", "Servicio de renta de bicis para turismo o recreación."),
        CategoryItem("Karting", "🏎️", "Deporte y Recreación", "Carreras de karts en circuitos profesionales."),








        // --------------------------------------------------------------------------
        // 🎉 SUPERCATEGORÍA: EVENTOS Y ENTRETIMIENTO
        // -----------------------------------------
        CategoryItem("Maquillador Artístico / FX", "🧟", "Eventos y Entretenimiento", "Maquillaje de caracterización, efectos especiales y transformaciones para cine o teatro."),
        CategoryItem("Peluquería Artística", "💇", "Eventos y Entretenimiento", "Peinados creativos y vanguardistas para desfiles, shows y producciones visuales."),
        CategoryItem("Barras Móviles", "🍸", "Eventos y Entretenimiento", "Servicio de coctelería y tragos para invitados."),
        CategoryItem("Salon de Eventos", "🎪", "Eventos y Entretenimiento", "Alquiler de espacios para bodas, XV y eventos corporativos."),
        CategoryItem("Salon de Fiestas", "🎊", "Eventos y Entretenimiento", "Salones infantiles y familiares con juegos incluidos."),
        CategoryItem("Organizador de Bodas", "💒", "Eventos y Entretenimiento", "Planificación integral y coordinación del gran día."),
        CategoryItem("Ambientación de Eventos", "🎀", "Eventos y Entretenimiento", "Decoración temática y diseño visual de fiestas."),
        CategoryItem("Fotografía de Eventos", "📷", "Eventos y Entretenimiento", "Cobertura profesional de momentos inolvidables."),
        CategoryItem("Videografía de Eventos", "🎥", "Eventos y Entretenimiento", "Filmación y edición de video en alta definición."),
        CategoryItem("DJ", "🎧", "Eventos y Entretenimiento", "Musicalización profesional para todo tipo de fiestas."),
        CategoryItem("Sonido e Iluminación", "🎛️", "Eventos y Entretenimiento", "Equipamiento técnico para eventos en vivo y salones."),
        CategoryItem("Animador Infantil", "🎈", "Eventos y Entretenimiento", "Entretenimiento para niños con juegos y dinámicas."),
        CategoryItem("Comediante / Stand Up", "🎤", "Eventos y Entretenimiento", "Shows de humor para eventos y teatros."),
        CategoryItem("Músico / Banda en Vivo", "🎸", "Eventos y Entretenimiento", "Presentaciones musicales para amenizar eventos."),
        CategoryItem("Mago", "🪄", "Eventos y Entretenimiento", "Espectáculos de magia e ilusionismo para todas las edades."),
        CategoryItem("Payaso", "🤡", "Eventos y Entretenimiento", "Animación clásica con humor y globología."),
        CategoryItem("Bailarín / Coreógrafo", "🕺", "Eventos y Entretenimiento", "Presentaciones de baile y armado de coreografías."),
        CategoryItem("Alquiler de Vajilla", "🍽️", "Eventos y Entretenimiento", "Servicio de platos, cubiertos y mantelería."),
        CategoryItem("Carpas y Toldos", "⛺", "Eventos y Entretenimiento", "Estructuras para eventos al aire libre."),
        CategoryItem("Florista", "💐", "Eventos y Entretenimiento", "Diseños florales para centros de mesa y ramos."),
        CategoryItem("Servicio de Lunch / Catering", "🥪", "Eventos y Entretenimiento", "Comida para eventos, finger food y menús completos."),
        CategoryItem("Asador / Parrillero para Eventos", "🥩", "Eventos y Entretenimiento", "Servicio de asado criollo a domicilio para fiestas."),
        CategoryItem("Fotocabina", "📸", "Eventos y Entretenimiento", "Entretenimiento interactivo con fotos instantáneas."),
        CategoryItem("Castillos Inflables", "🏰", "Eventos y Entretenimiento", "Alquiler de juegos inflables para niños."),
        CategoryItem("Cotillón", "🥳", "Eventos y Entretenimiento", "Artículos de fiesta y accesorios para celebraciones."),


        // --------------------------------------------------------------------------
        // 🍔 SUPERCATEGORÍA: GASTRONOMÍA Y BARES
        // -----------------------------------------
        CategoryItem("Comida Árabe / Shawarma", "🥙", "Gastronomía y Bares", "Especialidades del medio oriente, falafel y shawarmas tradicionales."),
        CategoryItem("Comida Mexicana", "🌮", "Gastronomía y Bares", "Tacos, burritos y platos típicos con el sabor auténtico de México."),
        CategoryItem("Comida Peruana", "🥘", "Gastronomía y Bares", "Cebiches, lomo saltado y lo mejor de la cocina andina."),
        CategoryItem("Comida China", "🥡", "Gastronomía y Bares", "Variedad de platos salteados, arroz chaufa y sabores orientales."),
        CategoryItem("Comida Japonesa", "🍱", "Gastronomía y Bares", "Ramen, tempura y platos tradicionales de la cocina nipona."),
        CategoryItem("Chocolatería", "🍫", "Gastronomía y Bares", "Bombones artesanales, tabletas y delicias de chocolate premium."),
        CategoryItem("Camarero / Mozo", "🤵", "Gastronomía y Bares", "Servicio profesional de atención a mesas para eventos y restaurantes."),
        CategoryItem("Bartender", "🍸", "Gastronomía y Bares", "Elaboración de coctelería clásica y moderna con estilo profesional."),
        CategoryItem("Cocinero", "👨‍🍳", "Gastronomía y Bares", "Servicios de cocina profesional para establecimientos o eventos privados."),
        CategoryItem("Restaurante", "🍽️", "Gastronomía y Bares", "Servicio de comida a la carta con atención en mesa."),
        CategoryItem("Bar / Pub", "🍻", "Gastronomía y Bares", "Venta de bebidas y minutas en un ambiente relajado."),
        CategoryItem("Cervecería Artesanal", "🍺", "Gastronomía y Bares", "Variedad de cervezas de elaboración propia y picadas."),
        CategoryItem("Coctelería", "🍹", "Gastronomía y Bares", "Tragos de autor y coctelería clásica profesional."),
        CategoryItem("Bodegón", "🥩", "Gastronomía y Bares", "Comida casera abundante y tradicional."),
        CategoryItem("Pizzería", "🍕", "Gastronomía y Bares", "Venta de pizzas al horno de barro o piedra."),
        CategoryItem("Hamburguesería", "🍔", "Gastronomía y Bares", "Hamburguesas gourmet con ingredientes seleccionados."),
        CategoryItem("Sushi", "🍣", "Gastronomía y Bares", "Comida japonesa tradicional y rolls creativos."),
        CategoryItem("Comida Vegana / Vegetariana", "🥗", "Gastronomía y Bares", "Opciones saludables libres de productos animales."),
        CategoryItem("Dietética", "🥜", "Gastronomía y Bares", "Venta de productos naturales, semillas y frutos secos."),
        CategoryItem("Cafetería", "☕", "Gastronomía y Bares", "Especialistas en café, infusiones y pastelería."),
        CategoryItem("Pastelería", "🍰", "Gastronomía y Bares", "Tortas, budines y dulces artesanales."),
        CategoryItem("Panadería", "🥖", "Gastronomía y Bares", "Pan fresco, facturas y productos de panificación."),
        CategoryItem("Food Truck", "🚐", "Gastronomía y Bares", "Gastronomía móvil para eventos y ferias."),
        CategoryItem("Heladería", "🍦", "Gastronomía y Bares", "Helados artesanales y postres helados."),
        CategoryItem("Vinoteca", "🍾", "Gastronomía y Bares", "Venta de vinos, espumantes y licores seleccionados."),
        CategoryItem("Rotisería", "🍗", "Gastronomía y Bares", "Comida lista para llevar y entrega a domicilio."),
        CategoryItem("Chef Privado", "🧑‍🍳", "Gastronomía y Bares", "Servicio de cocina profesional en tu propia casa."),
        CategoryItem("Barista", "☕", "Gastronomía y Bares", "Expertos en la preparación de café de especialidad."),
        CategoryItem("Sommelier / Enólogo", "🍷", "Gastronomía y Bares", "Catas guiadas y asesoramiento en vinos."),
        CategoryItem("Carnicero", "🥩", "Gastronomía y Bares", "Cortes de carne vacuna, porcina y aviar."),
        CategoryItem("Pescadero", "🐟", "Gastronomía y Bares", "Venta de pescados frescos y mariscos."),
        CategoryItem("Repostero/Pastelero", "🧑‍🍳", "Gastronomía y Bares", "Elaboración de postres y dulces por encargo."),



        // --------------------------------------------------------------------------
        // 🚛 SUPERCATEGORÍA: TRANSPORTE Y LOGÍSTICA
        // -----------------------------------------
        CategoryItem("Transporte de Larga Distancia de Personas", "🚌", "Transporte y Logística", "Servicios de ómnibus y traslados de pasajeros entre ciudades."),
        CategoryItem("Camionero", "🚛", "Transporte y Logística", "Conductor profesional de camiones para transporte de carga pesada."),
        CategoryItem("Piloto de Avión / Helicóptero", "✈️", "Transporte y Logística", "Servicios de pilotaje profesional para traslados privados y comerciales."),
        CategoryItem("Capitán de Yate / Lancha", "🛥️", "Transporte y Logística", "Navegación profesional y conducción de embarcaciones de recreo o comerciales."),
        CategoryItem("Fletes", "🛻", "Transporte y Logística", "Traslado de objetos medianos y cargas locales."),
        CategoryItem("Mudanzas", "🛻", "Transporte y Logística", "Servicio integral de traslado de muebles y pertenencias."),
        CategoryItem("Mensajería y Envíos", "✉️", "Transporte y Logística", "Reparto de paquetes y documentos de forma rápida."),
        CategoryItem("Chofer Privado", "🕴️", "Transporte y Logística", "Servicio de transporte personalizado para personas."),
        CategoryItem("Transporte Escolar", "🚌", "Transporte y Logística", "Traslado seguro de niños desde y hacia la escuela."),
        CategoryItem("Transporte de Larga Distancia", "🚛", "Transporte y Logística", "Cargas pesadas y traslados entre provincias."),
        CategoryItem("Almacenamiento / Self-Storage", "🔐", "Transporte y Logística", "Alquiler de depósitos temporales para mercadería u otros objetos."),




        // --------------------------------------------------------------------------
        // 🚗 SUPERCATEGORÍA: SERVICIOS AUTOMOTORES
        // -----------------------------------------
        CategoryItem("Grúa Mecánica", "🛻",  "Servicios Automotores", "Servicio de remolque y traslado de vehículos averiados."),
        CategoryItem("Gomería Móvil", "🛞",  "Servicios Automotores", "Reparación de neumáticos a domicilio o en ruta."),
        CategoryItem("Tren Delantero", "🚙",  "Servicios Automotores", "Reparación de suspensión, dirección y amortiguación."),
        CategoryItem("Inyección Electrónica", "🔌",  "Servicios Automotores", "Diagnóstico computarizado y reparación de sistemas de inyección."),
        CategoryItem("Especialista en Cajas Automáticas", "⚙️",  "Servicios Automotores", "Mantenimiento y reparación de transmisiones automáticas."),
        CategoryItem("Especialista en Frenos", "🛑",  "Servicios Automotores", "Reparación de sistemas de frenado, discos, pastillas y ABS."),
        CategoryItem("Electricidad de Motos", "🔋",  "Servicios Automotores", "Reparación de sistemas eléctricos específicos para motocicletas."),
        CategoryItem("Plotters (ploteos)", "🏴‍☠️",  "Servicios Automotores", "Personalización estética y protección de pintura mediante vinilos."),
        CategoryItem("Repuestos Motos", "🏍️",  "Servicios Automotores", "Venta de componentes y accesorios para todo tipo de motos."),
        CategoryItem("Venta de Neumáticos", "🛞",  "Servicios Automotores", "Comercialización de cubiertas nuevas de diversas marcas y medidas."),
        CategoryItem("Maquinaria Pesada (Vial)", "🚜",  "Servicios Automotores", "Mecánica especializada en máquinas de construcción y viales."),
        CategoryItem("Auxilio Mecánico", "🆘", "Servicios Automotores", "Asistencia en ruta y remolque ante averías."),
        CategoryItem("Gomería", "🛞", "Servicios Automotores", "Reparación y cambio de neumáticos."),
        CategoryItem("Taller Mecánico", "⚙️", "Servicios Automotores", "Mantenimiento general del motor y sistemas mecánicos."),
        CategoryItem("Mecánico de Autos", "🚗", "Servicios Automotores", "Diagnóstico y reparación de vehículos livianos."),
        CategoryItem("Mecánico de Motos", "🏍️", "Servicios Automotores", "Servicio técnico especializado en motocicletas."),
        CategoryItem("Alineación y Balanceo", "⚖️", "Servicios Automotores", "Optimización del tren delantero y desgaste de gomas."),
        CategoryItem("Instalación de GNC", "⛽", "Servicios Automotores", "Conversión de vehículos a gas natural comprimido."),
        CategoryItem("Lavadero de Autos", "🧽", "Servicios Automotores", "Limpieza interior y exterior de vehículos."),
        CategoryItem("Chapa y Pintura", "🚙", "Servicios Automotores", "Reparación de carrocería y acabados de pintura."),
        CategoryItem("Detailing", "💎", "Servicios Automotores", "Tratamientos estéticos profundos para el brillo del auto."),
        CategoryItem("Electricidad del Automótor", "⚡️", "Servicios Automotores", "Reparación de alternadores, baterías y luces."),
        CategoryItem("Polarizado", "🏴‍☠️🕶️", "Servicios Automotores", "Colocación de láminas de seguridad y control solar."),
        CategoryItem("Repuestos Autos", "🚘", "Servicios Automotores", "Venta de autopartes y componentes originales."),
        CategoryItem("Alquiler de Autos", "🚗", "Servicios Automotores", "Renta de vehículos por día o semana."),




        // -----------------------------------------
        // ⚖️ SUPERCATEGORÍA: SERVICIOS PROFESIONALES Y LEGALES
        // -----------------------------------------
        CategoryItem("Abogado Laboral", "⚖️", "Servicios Profesionales y Legales", "Asesoría especializada en contratos de trabajo, despidos y reclamos."),
        CategoryItem("Abogado Comercial", "⚖️", "Servicios Profesionales y Legales", "Asesoramiento jurídico para empresas, sociedades y contratos mercantiles."),
        CategoryItem("Gestoría General", "📁", "Servicios Profesionales y Legales", "Tramitación de documentos ante organismos públicos y privados."),
        CategoryItem("Despachante de Aduana", "🚢", "Servicios Profesionales y Legales", "Gestión de importaciones, exportaciones y trámites aduaneros."),
        CategoryItem("Abogado Civil", "⚖️", "Servicios Profesionales y Legales", "Litigios civiles, daños y perjuicios, y contratos entre particulares."),
        CategoryItem("Abogado General", "⚖️", "Servicios Profesionales y Legales", "Asesoría jurídica en diversas áreas del derecho."),
        CategoryItem("Abogado Penalista", "⚖️", "Servicios Profesionales y Legales", "Defensa en casos penales y procesos judiciales ante tribunales."),
        CategoryItem("Abogado Familia", "⚖️", "Servicios Profesionales y Legales", "Divorcios, cuotas alimentarias, régimen de visitas y filiaciones."),
        CategoryItem("Escribano Público", "✍️", "Servicios Profesionales y Legales", "Certificación de firmas, escrituras, poderes y actos notariales."),
        CategoryItem("Gestor del Automotor", "🚗", "Servicios Profesionales y Legales", "Trámites de transferencia, patentamiento e informes de vehículos."),
        CategoryItem("Traductor Público", "🗣️", "Servicios Profesionales y Legales", "Traducciones con validez legal de documentos oficiales y técnicos."),
        CategoryItem("Perito Judicial", "🔍", "Servicios Profesionales y Legales", "Dictámenes técnicos especializados para procesos legales y tribunales."),
        CategoryItem("Perito Calígrafo", "✍️", "Servicios Profesionales y Legales", "Verificación de autenticidad en firmas, documentos y manuscritos."),
        CategoryItem("Tasador", "🏷️", "Servicios Profesionales y Legales", "Valoración profesional de inmuebles, vehículos y activos comerciales."),
        CategoryItem("Abogado Previsional", "⚖️", "Servicios Profesionales y Legales", "Especialista en jubilaciones, pensiones y trámites ante entes estatales."),
        CategoryItem("Abogado de Sucesiones", "⚖️", "Servicios Profesionales y Legales", "Tramitación de herencias, declaratoria de herederos y partición de bienes."),
        CategoryItem("Mediador Judicial", "🤝", "Servicios Profesionales y Legales", "Resolución de conflictos mediante mediación obligatoria o voluntaria."),
        CategoryItem("Perito de Seguros", "📋", "Servicios Profesionales y Legales", "Evaluación de daños y siniestros para liquidación de seguros."),
        CategoryItem("Propiedad Intelectual", "💡", "Servicios Profesionales y Legales", "Registro de marcas, patentes, derechos de autor y licencias."),
        CategoryItem("Abogado de Accidentes de Tránsito", "🚗", "Servicios Profesionales y Legales", "Reclamos ante compañías de seguros y litigios por siniestros viales."),
        CategoryItem("Gestoría de Ciudadanías Extranjeras", "🌍", "Servicios Profesionales y Legales", "Tramitación de ciudadanías italiana, española y otras nacionalidades."),
        CategoryItem("Técnico en Higiene y Seguridad", "⛑️", "Servicios Profesionales y Legales", "Asesoramiento en prevención de riesgos laborales y cumplimiento de normativas."),
        CategoryItem("Auditoría de Consorcios", "🏢", "Servicios Profesionales y Legales", "Control contable y legal de la administración de edificios y barrios cerrados."),


        // -----------------------------------------
        // 🛠️ SUPERCATEGORÍA: SERVICIOS Ingenieria
        // -----------------------------------------
        CategoryItem("Ingeniero Agrónomo", "🌱", "Servicios Ingenieria", "Asesoramiento en producción agropecuaria y manejo de suelos."),
        CategoryItem("Ingeniero Químico", "🧪", "Servicios Ingenieria", "Diseño de procesos químicos y control de calidad industrial."),
        CategoryItem("Ingeniero Ambiental", "🌍", "Servicios Ingenieria", "Consultoría en sostenibilidad, impacto ambiental y tratamiento de residuos."),
        CategoryItem("Ingeniero Civil", "🏗️", "Servicios Ingenieria", "Cálculo estructural y dirección de grandes obras de infraestructura."),
        CategoryItem("Ingeniero Mecánico", "⚙️", "Servicios Ingenieria", "Diseño de maquinaria, sistemas térmicos y procesos industriales."),
        CategoryItem("Ingeniero Electrónico", "🔌", "Servicios Ingenieria", "Desarrollo de circuitos complejos y sistemas de control."),
        CategoryItem("Ingeniero Eléctrico", "⚡", "Servicios Ingenieria", "Proyectos de generación, transporte y distribución de energía eléctrica."),
        CategoryItem("Ingeniero en Sistemas", "💻", "Servicios Ingenieria", "Arquitectura de infraestructuras informáticas y redes a gran escala."),


        // -----------------------------------------
        // 📈 SUPERCATEGORÍA: FINANZAS Y NEGOCIOS
        // -----------------------------------------
        CategoryItem("Auditor", "🔎", "Finanzas y Negocios", "Examen crítico y sistemático de estados contables y procesos internos."),
        CategoryItem("Analista de Riesgos", "📉", "Finanzas y Negocios", "Evaluación de amenazas financieras y operativas para empresas."),
        CategoryItem("Corredor de Bolsa", "📈","Finanzas y Negocios", "Intermediación en mercados financieros y asesoramiento bursátil."),
        CategoryItem("Analista de Inversiones", "📊", "Finanzas y Negocios", "Estudio de oportunidades de mercado para la colocación de capital."),
        CategoryItem("Actuario", "🧮", "Finanzas y Negocios", "Análisis estadístico y financiero para evaluación de riesgos en seguros."),
        CategoryItem("Corredor de Seguros", "🛡️", "Finanzas y Negocios", "Intermediación y asesoramiento en la contratación de pólizas."),
        CategoryItem("Analista Financiero", "💰", "Finanzas y Negocios", "Seguimiento y planificación de la situación económica de una entidad."),
        CategoryItem("Productor de Seguros", "📋", "Finanzas y Negocios", "Gestión comercial y administrativa de carteras de seguros."),
        CategoryItem("Administrador de Consorcios", "🏢", "Finanzas y Negocios", "Gestión integral de edificios, gastos comunes y personal."),
        CategoryItem("Contador Público", "🧾", "Finanzas y Negocios", "Liquidación de impuestos, balances y contabilidad externa."),
        CategoryItem("Asesor Financiero", "💹", "Finanzas y Negocios", "Planificación de inversiones y gestión de capital."),
        CategoryItem("Consultor de Negocios", "💼", "Finanzas y Negocios", "Estrategias para el crecimiento y mejora de empresas."),
        CategoryItem("Recursos Humanos", "👥", "Finanzas y Negocios", "Selección de personal y gestión del talento."),
        CategoryItem("Agente Inmobiliario", "🏢", "Finanzas y Negocios", "Compra, venta y alquiler de propiedades."),



        // --------------------------------------------------------------------------
        // 📱 SUPERCATEGORÍA: MARKETING, DISEÑO Y MEDIOS
        // -----------------------------------------
        CategoryItem("Gráfica (Impresión Carteles, insumos)", "🖼️", "Marketing, Diseño y Medios", "Impresión de gran formato, cartelería, vinilos y artículos promocionales."),
        CategoryItem("Influencer", "🤳", "Marketing, Diseño y Medios", "Creadores de contenido con impacto en audiencias digitales específicas."),
        CategoryItem("Streamer", "🎮", "Marketing, Diseño y Medios", "Transmisiones en vivo de videojuegos, charlas o eventos especiales."),
        CategoryItem("Youtuber / Tiktoker", "▶️", "Marketing, Diseño y Medios", "Producción de contenido en video para plataformas sociales modernas."),
        CategoryItem("Copywriter / Redactor Freelance", "✍️", "Marketing, Diseño y Medios", "Escritura persuasiva para anuncios, blogs y sitios web corporativos."),
        CategoryItem("SEO / SEM Specialist", "🔍", "Marketing, Diseño y Medios", "Optimización en buscadores y gestión de campañas de anuncios pagos."),
        CategoryItem("Trafficker Digital", "🚦", "Marketing, Diseño y Medios", "Gestión experta de tráfico pago para maximizar conversiones."),
        CategoryItem("Relaciones Públicas", "🤝", "Marketing, Diseño y Medios", "Gestión de la comunicación institucional y vínculos con la prensa."),
        CategoryItem("Periodista", "📰", "Marketing, Diseño y Medios", "Investigación y redacción de noticias para diversos medios de comunicación."),
        CategoryItem("Locutor", "🎙️", "Marketing, Diseño y Medios", "Voz profesional para comerciales, radio, podcasts y eventos."),
        CategoryItem("Doblajista", "🗣️", "Marketing, Diseño y Medios", "Doblaje de voz para películas, series y material audiovisual."),
        CategoryItem("Marketing Digital", "📱", "Marketing, Diseño y Medios", "Estrategias en redes sociales, anuncios y crecimiento online."),
        CategoryItem("Diseñador Gráfico", "🎨", "Marketing, Diseño y Medios", "Creación de logos, identidad visual y folletería."),
        CategoryItem("Diseñador UX/UI", "🖥️", "Marketing, Diseño y Medios", "Diseño de interfaces enfocadas en la experiencia del usuario."),
        CategoryItem("Community Manager", "📱", "Marketing, Diseño y Medios", "Gestión y moderación de comunidades en redes sociales."),
        CategoryItem("Fotógrafo de Productos", "📦", "Marketing, Diseño y Medios", "Fotos profesionales para catálogos y e-commerce."),
        CategoryItem("Editor de Video", "🎞️", "Marketing, Diseño y Medios", "Montaje y postproducción de material audiovisual."),

        // --------------------------------------------------------------------------
        // 🔬 SUPERCATEGORÍA: CIENCIAS Y HUMANIDADES
        // -----------------------------------------
        CategoryItem("Matemático", "📐", "Ciencias y Humanidades", "Estudio de números, funciones y relaciones."),
        CategoryItem("Astrónomo", "🔭", "Ciencias y Humanidades", "Estudio de estrellas, planetas y galaxias."),
        CategoryItem("Geólogo", "🌍", "Ciencias y Humanidades", "Estudio de la Tierra y el planeta"),
        CategoryItem("Botánico", "🪴", "Ciencias y Humanidades", "Estudio de plantas y animales"),
        CategoryItem("Filósofo", "🤔", "Ciencias y Humanidades", "Investigación y análisis del pensamiento humano"),
        CategoryItem("Sociólogo", "📊", "Ciencias y Humanidades", "Estudio de la sociedad y las relaciones sociales"),
        CategoryItem("Politólogo", "🏛️", "Ciencias y Humanidades", "Análisis de sistemas políticos, políticas públicas y relaciones de poder."),
        CategoryItem("Historiador", "📜", "Ciencias y Humanidades", "Investigación y divulgación de procesos históricos y sociales."),
        CategoryItem("Arqueólogo / Antropólogo", "🏺", "Ciencias y Humanidades", "Estudio de las sociedades humanas a través de su cultura y restos materiales."),
        CategoryItem("Lingüista", "🗣️", "Ciencias y Humanidades", "Estudio científico del lenguaje, su estructura y evolución."),
        CategoryItem("Químico", "🧪", "Ciencias y Humanidades", "Análisis de laboratorio y desarrollo de productos químicos."),
        CategoryItem("Biólogo", "🧬", "Ciencias y Humanidades", "Estudio de los seres vivos y su interacción con el medio ambiente."),
        CategoryItem("Filósofo", "🤔", "Ciencias y Humanidades", "Investigación crítica sobre el conocimiento, la ética y la existencia."),


        // --------------------------------------------------------------------------
        // 📚 SUPERCATEGORÍA: EDUCACIÓN Y CLASES
        // -----------------------------------------
        CategoryItem("Clases Particulares", "🎒", "Educación y Clases", "Apoyo escolar integral para niveles primario y secundario."),
        CategoryItem("Profesor Universitario", "🏫", "Educación y Clases", "Tutorías y apoyo académico para nivel superior."),
        CategoryItem("Profesor de Matemáticas", "➗", "Educación y Clases", "Clases de álgebra, análisis matemático y geometría."),
        CategoryItem("Profesor de Física", "⚛️", "Educación y Clases", "Enseñanza de leyes físicas y resolución de problemas técnicos."),
        CategoryItem("Profesor de Química", "🧪", "Educación y Clases", "Clases de química general, orgánica e inorgánica."),
        CategoryItem("Profesor de Literatura", "📖", "Educación y Clases", "Análisis de textos, redacción y crítica literaria."),
        CategoryItem("Profesor de Historia", "📜", "Educación y Clases", "Enseñanza de procesos históricos mundiales y nacionales."),
        CategoryItem("Profesor de Geografía", "🌍", "Educación y Clases", "Estudio de la organización del espacio y recursos naturales."),
        CategoryItem("Profesor de Biología", "🧫", "Educación y Clases", "Clases de ciencias biológicas, anatomía y ecología."),
        CategoryItem("Profesor de Inglés", "🇬🇧", "Educación y Clases", "Enseñanza del idioma para todos los niveles y propósitos."),
        CategoryItem("Profesor de Francés", "🇫🇷", "Educación y Clases", "Enseñanza del idioma francés y cultura francófona."),
        CategoryItem("Profesor de Alemán", "🇩🇪", "Educación y Clases", "Clases de idioma alemán y preparación de exámenes."),
        CategoryItem("Profesor de Chino", "👲🏻", "Educación y Clases", "Enseñanza de mandarín y caligrafía china."),
        CategoryItem("Profesor de Japonés", "🇯🇵", "Educación y Clases", "Clases de idioma japonés y cultura nipona."),
        CategoryItem("Profesor de Español", "🇪🇸", "Educación y Clases", "Español para extranjeros y perfeccionamiento gramatical."),
        CategoryItem("Profesor de Portugués", "🇧🇷", "Educación y Clases", "Clases de portugués con enfoque comunicativo."),
        CategoryItem("Profesor de Canto", "🎤", "Educación y Clases", "Técnica vocal, respiración y expresión artística."),
        CategoryItem("Profesor de Guitarra", "🎸", "Educación y Clases", "Enseñanza de guitarra clásica, eléctrica o popular."),
        CategoryItem("Profesor de Piano", "🎹", "Educación y Clases", "Técnica de teclado y lectura de partituras."),
        CategoryItem("Profesor de Batería", "🥁", "Educación y Clases", "Coordinación rítmica y percusión aplicada."),
        CategoryItem("Profesor de Violín", "🎻", "Educación y Clases", "Técnica de arco y ejecución de instrumentos de cuerda."),
        CategoryItem("Profesor de Arte / Dibujo", "🎨", "Educación y Clases", "Exploración de técnicas plásticas y dibujo creativo."),
        CategoryItem("Profesor de Programación", "💻", "Educación y Clases", "Enseñanza de lenguajes de programación y lógica de sistemas."),
        CategoryItem("Profesor de Cocina", "👨‍🍳", "Educación y Clases", "Talleres de gastronomía, pastelería y técnicas culinarias."),
        CategoryItem("Profesor de Fotografía", "📸", "Educación y Clases", "Manejo de equipos y composición visual profesional."),
        CategoryItem("Profesor de Manejo", "🚗", "Educación y Clases", "Instrucción práctica para conducir vehículos livianos."),
        CategoryItem("Profesor Educación Especial", "🧩", "Educación y Clases", "Apoyo pedagógico para alumnos con necesidades especiales."),


        // -----------------------------------------
        // 🤝 SUPERCATEGORÍA: CUIDADO Y ASISTENCIA Especial
        // -----------------------------------------
        CategoryItem("Interprete Lenguaje de Señas", "🧏🏻", "Cuidado y Asistencia", "Interpretación profesional para facilitar la comunicación de personas sordas."),
        CategoryItem("Tiflopedagogía (Lenguaje Braille)", "👨🏻‍🦯","Cuidado y Asistencia", "Enseñanza y apoyo especializado para personas con discapacidad visual."),
        CategoryItem("Tutor de SAAC con Pictogramas", "💭", "Cuidado y Asistencia", "Sistemas Aumentativos y Alternativos de Comunicación para desafíos del habla."),
        CategoryItem("Asistencia con Animales guias (IAA)", "🦮","Cuidado y Asistencia", "Acompañamiento y entrenamiento de animales para asistencia personal."),
        CategoryItem("Terapia Asistida con Animales (TAA)", "🐎","Cuidado y Asistencia", "Intervenciones terapéuticas apoyadas en el vínculo con animales."),
        CategoryItem("Niñera (Baby Sitter)", "👶", "Cuidado y Asistencia", "Cuidado responsable de niños en domicilio."),
        CategoryItem("Cuidador de Ancianos", "🧓", "Cuidado y Asistencia", "Asistencia y acompañamiento para adultos mayores."),
        CategoryItem("Acompañante Terapéutico", "🤝", "Cuidado y Asistencia", "Apoyo a pacientes en tratamientos de salud mental."),
        CategoryItem("Terapeuta Ocupacional", "🤲", "Cuidado y Asistencia", "Rehabilitación para la autonomía en la vida diaria."),


        // --------------------------------------------------------------------------
        // 🐾 SUPERCATEGORÍA: MASCOTAS Y VETERINARIA
        // -----------------------------------------
        CategoryItem("Guardería de Mascotas", "🏡",  "Mascotas y Veterinaria", "Hospedaje temporal y cuidado responsable para perros y gatos."),
        CategoryItem("Acuario", "🐠",  "Mascotas y Veterinaria", "Venta de peces, peceras, mantenimiento y asesoría en acuariofilia."),
        CategoryItem("Crematorio de Mascotas", "🕊️", "Mascotas y Veterinaria", "Servicios de despedida digna y cremación para animales de compañía."),
        CategoryItem("Veterinaria", "🐶", "Mascotas y Veterinaria", "Atención médica y urgencias para animales domésticos."),
        CategoryItem("Peluquería Canina", "🐩", "Mascotas y Veterinaria", "Baño, corte y estética para perros y gatos."),
        CategoryItem("Paseador de Perros", "🦮", "Mascotas y Veterinaria", "Salidas recreativas y ejercicio para tu mascota."),
        CategoryItem("Adiestrador", "🐕‍🦺", "Mascotas y Veterinaria", "Educación conductual y obediencia canina."),
        CategoryItem("Pet Shop", "🦴", "Mascotas y Veterinaria", "Venta de alimentos, juguetes y accesorios."),


        // --------------------------------------------------------------------------
        // ✈️ SUPERCATEGORÍA: TURISMO Y HOTELERÍA
        // -----------------------------------------
        CategoryItem("Guía Turístico", "🗺️", "Turismo y Hotelería", "Recorridos guiados por puntos de interés histórico y natural."),
        CategoryItem("Agencia de Viajes", "✈️", "Turismo y Hotelería", "Venta de paquetes turísticos, pasajes y asesoramiento integral."),
        CategoryItem("Hotel", "🏨", "Turismo y Hotelería", "Alojamiento con servicios completos para viajeros."),
        CategoryItem("Hostel", "🛏️", "Turismo y Hotelería", "Alojamiento compartido o privado enfocado en el intercambio cultural."),
        CategoryItem("Cabañas", "🏕️", "Turismo y Hotelería", "Alojamiento en entornos naturales con privacidad y confort."),
        CategoryItem("Traductor Turístico", "🗣️", "Turismo y Hotelería", "Asistencia lingüística para viajeros y grupos internacionales."),
        CategoryItem("Camping", "⛺", "Turismo y Hotelería", "Zonas habilitadas para acampar con servicios básicos y recreación."),
        CategoryItem("Balneario", "🏖️", "Turismo y Hotelería", "Instalaciones de playa con servicios de sombra y recreación."),
        CategoryItem("Museo", "🖼️", "Turismo y Hotelería", "Espacios de exposición cultural, histórica o artística."),
        CategoryItem("Alquiler Temporario", "🏠", "Turismo y Hotelería", "Departamentos y casas amobladas para estancias de corta duración."),
        CategoryItem("Apart Hotel", "🏢", "Turismo y Hotelería", "Departamentos con servicios de hotel para mayor comodidad."),
        CategoryItem("Guía de Pesca", "🎣", "Turismo y Hotelería", "Excursiones de pesca deportiva con guía y equipo especializado."),
        CategoryItem("Transfer Aeropuerto", "🚐", "Turismo y Hotelería", "Traslados privados o compartidos desde y hacia terminales aéreas."),
        CategoryItem("Turismo Aventura", "🧗", "Turismo y Hotelería", "Actividades recreativas de alta intensidad en entornos naturales."),
        CategoryItem("Glamping", "⛺✨", "Turismo y Hotelería", "Camping de lujo que combina la experiencia al aire libre con comodidades de hotel."),
        CategoryItem("Turismo Rural", "🚜", "Turismo y Hotelería", "Estadías en estancias y campos para conocer la vida y producción rural."),
        CategoryItem("Enoturismo", "🍷", "Turismo y Hotelería", "Visitas guiadas a bodegas, viñedos y experiencias de cata de vinos."),
        CategoryItem("Coordinador de Viajes Grupales", "🚩", "Turismo y Hotelería", "Liderazgo y logística para grupos de turistas en viajes organizados."),

        // --------------------------------------------------------------------------
        // 🚨 SUPERCATEGORÍA: SEGURIDAD Y EMERGENCIAS
        // -----------------------------------------
        CategoryItem("Seguridad de Eventos", "💂", "Seguridad y Emergencias", "Control de acceso y vigilancia en fiestas, conciertos y congresos."),
        CategoryItem("Guardaespaldas", "🕴️", "Seguridad y Emergencias", "Protección personal y custodia VIP con profesionales capacitados."),
        CategoryItem("Valet Parking", "🚘", "Seguridad y Emergencias", "Servicio de recepción, estacionamiento y entrega de vehículos en eventos."),
        CategoryItem("Seguridad Privada", "🛡️", "Seguridad y Emergencias", "Vigilancia y protección para hogares y comercios."),
        CategoryItem("Bombero Voluntario", "🚒", "Seguridad y Emergencias", "Respuesta ante incendios y catástrofes naturales."),
        CategoryItem("Paramédico", "🚑", "Seguridad y Emergencias", "Atención médica pre-hospitalaria de urgencia."),


        // --------------------------------------------------------------------------
        // 🌾 SUPERCATEGORÍA: AGRICULTURA Y GANADERÍA
        // -----------------------------------------
        CategoryItem("Operador de Tractor / Cosechadora", "🚜", "Agricultura y Ganadería", "Operador de maquinaria de cultivo y cosecha."),
        CategoryItem("Apicultor", "🐝","Agricultura y Ganadería", "Apicultor de Abejas" ),
        CategoryItem("Mecánico Agrícola", "🚜","Agricultura y Ganadería", "Mecanico de Maquinaria para el trabajo de campos , como tractores, cosechadoras, etc" ),
        CategoryItem("Cosechador", "🌾","Agricultura y Ganadería", "Trabajo de Cosecha en el campo, se lo conoce popularmente como trabajos Golondrina" ),
        CategoryItem("Ganadero", "🐑","Agricultura y Ganadería", "Especialistas en Ganaderia" ),
        CategoryItem("Pesquero", "🐟","Agricultura y Ganadería", "Actividades de pesca comercial y manejo de recursos marítimos o fluviales." ),
        CategoryItem("Veterinario Rural", "🐄", "Agricultura y Ganadería", "Atención de grandes animales y ganado."),
        CategoryItem("Agricultor", "🌾", "Agricultura y Ganadería", "Producción de cultivos y manejo de tierras agrícolas."),
        CategoryItem("Vivero", "🪴", "Agricultura y Ganadería", "Venta de plantas, plantines e insumos de cultivo."),

        // --------------------------------------------------------------------------
        // 🔮 SUPERCATEGORÍA: ESOTERISMO
        // --------------------------------------------------------------------------
        CategoryItem("Carta Astral/Natal", "🃏", "Esoterismo", "Lectura de cartas para orientación personal y autoconocimiento profundo."),
        CategoryItem("Tarotista", "🔮", "Esoterismo", "Interpretación profesional de arcanos y guía a través del tarot."),
        CategoryItem("Lectura de Cartas", "🎴", "Esoterismo", "Consulta de oráculos y barajas para claridad en decisiones de vida."),
        CategoryItem("Astrólogo", "🪐", "Esoterismo", "Estudio de mapas natales y tránsitos planetarios para el crecimiento personal."),
        CategoryItem("Limpieza Energética", "✨", "Esoterismo", "Remoción de bloqueos y armonización vibracional de ambientes y personas."),
        CategoryItem("Numerología", "🔢", "Esoterismo", "Análisis del impacto de los números en el destino y la personalidad."),
        CategoryItem("Medium", "👻", "Esoterismo", "Canalización de mensajes y comunicación con planos espirituales."),

        CategoryItem("Sanador Espiritual", "🙌", "Esoterismo", "Terapias de sanación profunda del alma y equilibrio energético.")
    )
}
