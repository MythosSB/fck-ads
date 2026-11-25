ESPECIFICACIÓN: ControlGastos (Android)

STACK: Kotlin, Jetpack Compose con Material Design 3 (librería androidx.compose.material3), Room (base de datos local), Navigation Compose. Un solo módulo. Sin internet ni cuentas; todo se guarda en el teléfono.

ARQUITECTURA (capas, de arriba a abajo):
Pantalla (Compose) -> ViewModel -> Repositorio -> DAO/Room.
Cada capa solo habla con la de abajo. Las pantallas nunca tocan la base de datos directamente.

DATOS
Categoria: id (autogenerado), nombre (texto, único).
Gasto: id (autogenerado), montoCentavos (Long), categoriaId (referencia a Categoria), fecha (día, sin hora), nota (texto opcional).

REGLAS
- El monto se guarda en CENTAVOS como entero (12.50 pesos = 1250) para evitar errores de decimales. Solo se convierte a formato "$12.50" al mostrarlo.
- Moneda única: pesos mexicanos.
- Monto obligatorio y mayor que 0. Máximo 2 decimales al escribirlo.
- Fecha por defecto: hoy; editable.
- Categorías iniciales (se crean en el primer arranque): Comida, Transporte, Hogar, Ocio, Salud, Otros.
- Las categorías no se pueden crear, editar ni borrar desde la app (versión 1).

PANTALLAS
1. Inicio: selector de mes (anterior/siguiente), total del mes y lista de gastos de ese mes ordenados del más reciente al más antiguo. Botón "+" para agregar. Tocar un gasto lo abre para editar.
2. Agregar/Editar gasto: campos monto, categoría, fecha, nota. Botón guardar; al editar, también botón eliminar con confirmación.
3. Resumen: total del mes dividido por categoría.

DISEÑO (Material 3)
- Usar SOLO componentes de androidx.compose.material3. Prohibido usar androidx.compose.material (versión 2, antigua).
- Colores y tipografía siempre desde MaterialTheme (colorScheme y typography); no escribir colores fijos dentro de las pantallas.
- Tema claro y oscuro, siguiendo el ajuste del sistema.
- Colores dinámicos (Material You) activados en Android 12 o superior, con paleta propia de respaldo en versiones anteriores.
- Estructura de pantallas con Scaffold, barra superior (TopAppBar) y FloatingActionButton para el botón "+".
- Lista de gastos con ListItem o Card; campos de texto con OutlinedTextField; selector de fecha con DatePicker de Material 3; confirmación de borrado con AlertDialog.
- Respetar los márgenes seguros del sistema (edge-to-edge) y tamaños táctiles mínimos de 48 dp.

DECISIONES CERRADAS
- Android mínimo: 8.0 (API 26).
- Categorías: solo las 6 iniciales, sin pantalla de administración. En la base de datos se mantiene una clave foránea con RESTRICT como protección, sin interfaz asociada.
- Navegación: barra inferior con dos destinos, Inicio y Resumen. El mes seleccionado se comparte entre ambos.
- No se permiten fechas futuras.
- Orden de la lista: fecha descendente y, en empate, id descendente.
- El Resumen muestra solo las categorías que tengan gastos en el mes seleccionado.
- Sin categoría por defecto en el formulario. El botón Guardar se activa solo con monto válido y categoría elegida.
- Límites: monto máximo 9,999,999.99; nota máximo 200 caracteres. Validar que no haya desbordamiento numérico.
- Decimales: se acepta coma o punto y se normaliza internamente.
- Salir del formulario con cambios sin guardar muestra una confirmación; sin cambios, sale directo.
- Paleta de respaldo (Android menor a 12): verde sobrio, con variantes clara y oscura.
- El respaldo automático de Android se deja activado (valor por defecto).
- Técnicas: corrutinas y Flow; fechas con LocalDate guardadas en Room como días desde la época Unix; sumas y agrupaciones mensuales con consultas de Room; inyección de dependencias manual, sin frameworks; todos los textos en strings.xml (solo español).
- Las pruebas automáticas se hacen en un paso aparte al final, no en cada paso.

FUERA DEL ALCANCE (por ahora): presupuestos, ingresos, gráficas, exportar, varias monedas, respaldo en la nube.