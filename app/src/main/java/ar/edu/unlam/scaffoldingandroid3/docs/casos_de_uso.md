# Casos de Uso - App de Senderismo
## Programación Móvil 3 - Trabajo Práctico Integrador

---

## 📋 **Descripción General**
Aplicación móvil para grabación, gestión y seguimiento de rutas de senderismo con funcionalidades de tracking GPS, cámara, mapas interactivos y análisis de actividad física.

---

## 🎯 **Requerimientos Técnicos Cubiertos**

### ✅ **Sensores Implementados:**
- **Geolocalización (GPS)** - Tracking de rutas y posicionamiento
- **Acelerómetro** - Conteo de pasos y detección de movimiento
- **Barómetro** - Medición de altitud y desniveles
- **Magnetómetro** - Brújula digital para orientación

### ✅ **Componentes Obligatorios:**
- **Cámara** - Captura de fotos durante recorridos
- **Mapa** - Visualización de rutas y navegación
- **Jetpack Compose** - Toda la UI implementada en Compose
- **Animaciones** - 5 animaciones implementadas (ver sección específica)

### ✅ **Arquitectura:**
- **Clean Architecture + Hexagonal**
- **MVVM con StateFlow/LiveData**
- **Gitflow** para control de versiones
- **Testing** - Tests unitarios y de componentes Jetpack

---

## 🎨 **Animaciones Implementadas**

### 1. **Swipe to Delete**
- **Descripción:** Animación de "tachito" deslizando para eliminar elementos
- **Aplicación:** Eliminar rutas guardadas e historial de actividades

### 2. **Trail Magic - Línea que se escribe**
- **Descripción:** Las rutas se dibujan progresivamente con efecto glow
- **Aplicación:** Al seleccionar una ruta guardada para visualización

### 3. **Stats Bloom - Florecimiento de Estadísticas**
- **Descripción:** Las métricas "florecen" desde el centro como pétalos de flor
- **Aplicación:** Al expandir el panel de estadísticas durante grabación

### 4. **Compass Wobble - Brújula Tambaleante**
- **Descripción:** Brújula que se tambalea físicamente al tocarla con números que "bailan"
- **Aplicación:** Widget de brújula para orientar mapa al norte

### 5. **Morphing de íconos de grabación**
- **Descripción:** Botón que se transforma fluidamente entre estados
- **Aplicación:** Estados play/pause/stop del sistema de grabación

---

## 📱 **Estructura de Navegación**

### **Bottom Navigation Bar:**
- **"Guardados"** (izquierda): Rutas favoritas descargadas listas para usar offline
- **"Mapa"** (centro): Pantalla principal con mapa interactivo
- **"Historial"** (derecha): Actividades/rutas completadas por el usuario

### **Botones principales en pantalla Mapa:**
- **"Cargar ruta"**: Buscar y descargar rutas por ubicación para uso offline
- **"Grabar ruta"**: Iniciar grabación de nueva ruta

---

## 📱 **Casos de Uso por Funcionalidad**

---

## 🗺️ **FUNCIONALIDAD: Gestión de Mapas**

### CU-001: Ver mapa principal
```gherkin
Dado que el usuario abre la aplicación
Cuando la pantalla principal carga completamente
Entonces se muestra un mapa interactivo
Y se visualizan íconos de rutas disponibles en el área
Y se muestra la ubicación actual del usuario
Y se muestra el bottom navigation bar con "Guardados", "Mapa", "Historial"
Y se muestra el botón "Cargar ruta" en la pantalla del mapa
Y se muestra el botón "Grabar ruta" en la pantalla del mapa
Y se muestra la brújula interactiva en la esquina superior derecha
```

### CU-002: Cambiar tipo de mapa
```gherkin
Dado que el usuario está en la pantalla del mapa principal
Cuando presiona el ícono "capas" en la esquina superior derecha
Entonces se despliega un panel emergente inferior (bottom sheet)
Y se muestran las opciones de tipo de mapa: "Default" y "Satellite"
Cuando selecciona un tipo de mapa diferente
Entonces el mapa cambia al tipo seleccionado
Y el panel se cierra automáticamente
```

### CU-003: Usar brújula interactiva
```gherkin
Dado que el usuario está en cualquier pantalla con mapa
Cuando toca la brújula en la esquina superior derecha
Entonces la brújula se tambalea físicamente con animación realista
Y los números de grados "bailan" siguiendo el movimiento
Y el mapa se orienta automáticamente al norte
Y la brújula vuelve gradualmente a su posición de reposo
```

### CU-004: Centrar ubicación actual
```gherkin
Dado que el usuario está en el mapa y su ubicación no está centrada
Cuando presiona el ícono de ubicación resaltado
Entonces el mapa se centra suavemente en la ubicación actual del usuario
Y se muestra una animación de zoom hacia la posición
```

---

## 🔍 **FUNCIONALIDAD: Cargar Rutas por Ubicación**

### CU-005: Buscar rutas por ubicación
```gherkin
Dado que el usuario está en la pantalla principal del mapa
Cuando presiona el botón "Cargar ruta"
Entonces se abre la pantalla de búsqueda de rutas
Y se muestra un campo de búsqueda con placeholder "Buscar rutas por ubicación"
Y se pueden ver sugerencias de ubicaciones populares
```

### CU-006: Realizar búsqueda de rutas
```gherkin
Dado que el usuario está en la pantalla de búsqueda de rutas
Cuando escribe el nombre de una ubicación (ej: "San Luis", "Córdoba")
Y presiona buscar o enter
Entonces se muestran las rutas disponibles para esa ubicación
Y cada ruta muestra: foto de vista previa, nombre, distancia, dificultad, estado de descarga
Y se agrupan por ubicación con un ícono de marcador
```

### CU-007: Descargar ruta para uso offline
```gherkin
Dado que el usuario está viendo la lista de rutas de una ubicación
Cuando selecciona una ruta específica para descargar
Entonces se inicia la descarga de la ruta con indicador de progreso
Y al completarse, la ruta se marca como "Descargada"
Y la ruta se agrega automáticamente a la sección "Guardados"
Y queda disponible para uso sin conexión a internet
```

### CU-008: Ver detalle de ruta antes de descargar
```gherkin
Dado que el usuario está en la lista de rutas de una ubicación
Cuando toca en el nombre o foto de una ruta (no en descargar)
Entonces se abre el detalle de la ruta
Y se muestra: foto del mapa, descripción, distancia, elevación, dificultad
Y se muestran fotos del recorrido subidas por otros usuarios
Y se muestra el botón "Descargar ruta" para agregarla a guardados
```

---

## 📍 **FUNCIONALIDAD: Grabación de Nueva Ruta**

### CU-009: Iniciar grabación de nueva ruta
```gherkin
Dado que el usuario está en la pantalla principal del mapa
Cuando presiona el botón "Grabar ruta"
Entonces se abre la pantalla de grabación
Y se muestra la ubicación actual del usuario en el mapa
Y se muestra el botón de grabación en estado inicial (círculo)
Y se muestran las estadísticas en cero: tiempo (00:00:00), distancia (0.00km), velocidades (0.0km/h), altura (0m), desniveles (0m)
Y se muestra el ícono de cámara para tomar fotos
```

### CU-010: Comenzar grabación de ruta
```gherkin
Dado que el usuario está en la pantalla de grabación con el botón en estado inicial
Cuando presiona el botón de grabación (círculo)
Entonces el botón se transforma a estado "grabando" (cuadrado rojo) con animación morphing
Y comienza el tracking GPS de la posición
Y se inicia el cronómetro de tiempo
Y se actualiza la distancia recorrida en tiempo real
Y se calculan y muestran las velocidades actual y media
Y se registra la altura actual y se calculan los desniveles
Y se habilita el conteo de pasos con el acelerómetro
```

### CU-011: Pausar grabación de ruta
```gherkin
Dado que el usuario está grabando una ruta activa
Cuando presiona el botón de grabación (cuadrado rojo)
Entonces el botón se transforma a estado "pausado" (ícono de pausa) con animación morphing
Y se pausa el tracking GPS
Y se pausa el cronómetro (manteniendo el tiempo acumulado)
Y se mantienen las estadísticas actuales sin actualizar
Y se deshabilita temporalmente el conteo de pasos
```

### CU-012: Reanudar grabación pausada
```gherkin
Dado que el usuario tiene una grabación pausada
Cuando presiona el botón de pausa
Entonces el botón se transforma de nuevo a estado "grabando" (cuadrado rojo)
Y se reanuda el tracking GPS desde la posición actual
Y se reanuda el cronómetro desde el tiempo acumulado
Y se continúa actualizando las estadísticas en tiempo real
Y se reanuda el conteo de pasos
```

### CU-013: Ver estadísticas durante grabación
```gherkin
Dado que el usuario está grabando una ruta activa
Cuando desliza hacia arriba el panel de "Datos del recorrido"
Entonces se despliega la pantalla completa de estadísticas con animación "Stats Bloom"
Y se muestran todas las métricas detalladas: tiempo total, distancia recorrida, velocidad actual, velocidad media, tiempo en movimiento, altura actual, desnivel positivo, desnivel negativo
Y las estadísticas se actualizan en tiempo real
```

### CU-014: Finalizar grabación de ruta
```gherkin
Dado que el usuario está grabando una ruta activa
Cuando mantiene presionado el botón de grabación por 2 segundos
Entonces aparece un modal de confirmación "¿Finalizar grabación?"
Cuando confirma la finalización
Entonces se detiene el tracking GPS
Y se para el cronómetro
Y se abre el modal "Guardar recorrido"
```

---

## 📸 **FUNCIONALIDAD: Captura de Fotos Durante Recorrido**

### CU-015: Tomar foto durante recorrido
```gherkin
Dado que el usuario está en la pantalla de grabación activa
Cuando presiona el ícono de cámara
Entonces se abre la interfaz de la cámara del dispositivo
Y se mantiene el tracking GPS en segundo plano
```

### CU-012: Confirmar foto tomada
```gherkin
Dado que el usuario acaba de tomar una foto
Cuando la foto se captura exitosamente
Entonces se muestra la foto en pantalla de confirmación
Y se muestran las opciones "Reintentar" y "OK"
Cuando selecciona "OK"
Entonces se abre la pantalla de gestión de fotos
Y se muestra la foto recién tomada en el contador (ej: "Fotos 1/6")
Y se proporciona campos para agregar "Nombre" y "Descripción" de la foto
Y se registra automáticamente la ubicación GPS donde se tomó la foto
```

### CU-013: Gestionar múltiples fotos
```gherkin
Dado que el usuario está en la pantalla de gestión de fotos
Cuando presiona el ícono "+" para agregar más fotos
Entonces se muestra un modal con opciones "Galería" y "Tomar una foto"
Cuando selecciona "Tomar una foto"
Entonces se abre nuevamente la interfaz de cámara
Cuando selecciona "Galería"
Entonces se abre la galería del dispositivo para seleccionar fotos existentes
```

### CU-014: Eliminar foto durante gestión
```gherkin
Dado que el usuario está en la pantalla de gestión de fotos
Cuando presiona el ícono de basura junto a una foto
Entonces la foto se elimina de la colección
Y se actualiza el contador de fotos
Y se reorganizan las fotos restantes
```

---

## 💾 **FUNCIONALIDAD: Guardado de Rutas**

### CU-019: Guardar ruta después de grabar
```gherkin
Dado que el usuario finalizó una grabación y está en el modal "Guardar recorrido"
Cuando completa el campo "Nombre" con el nombre del recorrido
Y completa el campo "Descripción" con detalles del recorrido
Y confirma las fotos agregadas con sus nombres y descripciones
Y presiona el botón "Guardar recorrido"
Entonces la ruta se guarda en el historial de actividades
Y se captura automáticamente una foto del mapa del recorrido
Y se almacenan todas las estadísticas: tiempo total, distancia, pasos, calorías gastadas, altitud máxima, desniveles
Y se guarda la información de todas las fotos con sus ubicaciones GPS
Y se regresa a la pantalla principal del mapa
```

### CU-020: Guardar ruta como favorita
```gherkin
Dado que el usuario está visualizando el detalle de una ruta descargada
Cuando presiona el botón "Descargar ruta" o "Guardar como favorita"
Entonces la ruta se descarga para uso offline
Y se guarda en la sección "Guardados" (accesible desde bottom navigation)
Y se marca como "Ya descargada" en futuras visualizaciones
Y queda disponible para seguimiento sin conexión a internet
```

### CU-021: Cancelar grabación sin guardar
```gherkin
Dado que el usuario está en el modal "Guardar recorrido"
Cuando presiona el ícono "X" de cerrar
Entonces aparece un modal de confirmación "¿Descartar grabación?"
Cuando confirma descartar
Entonces se elimina toda la información de la grabación
Y se regresa a la pantalla principal del mapa
Y no se guarda nada en el historial
```

---

## 📂 **FUNCIONALIDAD: Gestión de Rutas Guardadas**

### CU-022: Acceder a rutas guardadas
```gherkin
Dado que el usuario está en cualquier pantalla de la aplicación
Cuando presiona "Guardados" en el bottom navigation
Entonces se abre la pantalla "Rutas Guardadas"
Y se visualizan todas las rutas descargadas como favoritas
Y cada ruta muestra: foto de vista previa, nombre del lugar, nombre del usuario autor, distancia en km, ícono de play
Y las rutas aparecen ordenadas por fecha de guardado (más recientes primero)
```

### CU-023: Ver detalle de ruta guardada
```gherkin
Dado que el usuario está en la lista de "Rutas Guardadas"
Cuando selecciona una ruta específica (toca en cualquier parte excepto el ícono play)
Entonces se abre la pantalla de detalle de la ruta
Y se muestra la foto del mapa elegido
Y se muestran los datos: nombre del lugar, ubicación, distancia, elevación
Y se muestra el carrusel de fotos del lugar que subió el autor
Y cada foto tiene su nombre y descripción visible
Y se muestra el botón play para iniciar el seguimiento de la ruta
```

### CU-024: Eliminar ruta guardada
```gherkin
Dado que el usuario está en la lista de "Rutas Guardadas"
Cuando desliza hacia la derecha sobre una ruta específica
Entonces aparece una animación de "tachito" de basura deslizándose sobre el registro
Y la animación continúa hasta el extremo derecho de la pantalla
Y el registro desaparece de la lista con animación suave
Y la ruta se elimina permanentemente de "Rutas Guardadas"
Y se actualiza la numeración de las rutas restantes
```

---

## 🎯 **FUNCIONALIDAD: Seguimiento de Rutas Guardadas**

### CU-025: Iniciar ruta guardada desde lista
```gherkin
Dado que el usuario está en la lista de "Rutas Guardadas"
Cuando presiona el ícono de play de una ruta específica
Entonces se abre el mapa con la ruta trazada
Y se muestra la posición actual del usuario
Y se muestra el botón "Iniciar" para comenzar el seguimiento
Y la ruta aparece dibujada con animación "Trail Magic" (línea que se escribe progresivamente)
```

### CU-026: Iniciar ruta desde detalle
```gherkin
Dado que el usuario está en la pantalla de detalle de una ruta guardada
Cuando presiona el botón play
Entonces se abre el mapa con la ruta trazada
Y se muestra la posición actual del usuario
Y se muestra el botón "Iniciar" para comenzar el seguimiento
Y la ruta se dibuja con animación "Trail Magic"
```

### CU-027: Comenzar seguimiento de ruta
```gherkin
Dado que el usuario está en el mapa con una ruta trazada
Cuando presiona el botón "Iniciar"
Entonces comienza el tracking GPS para seguir la ruta
Y se inicia el cronómetro de tiempo
Y se muestran las estadísticas de progreso en tiempo real
Y se habilita la visualización de desviaciones de la ruta planificada
Y se activa el conteo de pasos y cálculo de calorías
```

### CU-028: Seguir ruta en tiempo real
```gherkin
Dado que el usuario está siguiendo una ruta activa
Cuando se mueve por el recorrido
Entonces se actualiza su posición en tiempo real sobre la ruta trazada
Y se muestran las estadísticas actualizadas: tiempo transcurrido, distancia recorrida, velocidad actual, distancia restante
Y se indica visualmente si está dentro o fuera del trazado de la ruta
Y se puede acceder al panel expandido de estadísticas deslizando hacia arriba
```

---

## 🎯 **FUNCIONALIDAD: Selección de Rutas desde Mapa**

### CU-029: Explorar rutas cercanas en mapa
```gherkin
Dado que el usuario está en la pantalla principal del mapa
Cuando navega por el mapa moviendo y haciendo zoom
Entonces se visualizan íconos de rutas disponibles en el área visible
Y se muestran rutas cercanas a la ubicación actual
Y se pueden distinguir visualmente las rutas ya guardadas vs no guardadas
```

### CU-030: Seleccionar ruta desde mapa
```gherkin
Dado que el usuario está visualizando el mapa principal
Cuando toca sobre un ícono de ruta específica
Entonces se visualiza el detalle de la ruta seleccionada
Y se muestra la información básica: nombre del lugar, distancia, elevación
Y se muestran las fotos del lugar que subió el autor
Y se proporciona la opción de iniciar la ruta o guardarla como favorita
```os de rutas disponibles en el área visible
Y se muestran rutas cercanas a la ubicación actual
Y se pueden distinguir visualmente las rutas ya guardadas vs no guardadas
```

### CU-026: Seleccionar ruta desde mapa
```gherkin
Dado que el usuario está visualizando el mapa principal
Cuando toca sobre un ícono de ruta específica
Entonces se visualiza el detalle de la ruta seleccionada
Y se muestra la información básica: nombre del lugar, distancia, elevación
Y se muestran las fotos del lugar que subió el autor
Y se proporciona la opción de iniciar la ruta o guardarla como favorita
```

---

## 📊 **FUNCIONALIDAD: Historial de Actividades**

### CU-031: Acceder al historial de actividades
```gherkin
Dado que el usuario está en cualquier pantalla de la aplicación
Cuando presiona "Historial" en el bottom navigation
Entonces se abre la pantalla "Tu historial de actividad"
Y se muestran todas las rutas que el usuario ha completado
Y cada actividad muestra: foto de vista previa, nombre del lugar, fecha, tiempo que tardó, pasos dados, altitud máxima alcanzada
Y las actividades aparecen ordenadas por fecha (más recientes primero)
```

### CU-032: Ver estadísticas detalladas de actividad
```gherkin
Dado que el usuario está en "Tu historial de actividad"
Cuando selecciona una actividad específica
Entonces se abre la pantalla de estadísticas detalladas
Y se muestra la información básica: nombre de la caminata, fecha, hora de inicio
Y se muestra el carrusel de fotos tomadas durante esa ruta
Y se muestran gráficos interactivos de velocidad y altitud a lo largo del tiempo
Y se muestran estadísticas completas: tiempo total, pasos, calorías gastadas, velocidades, altitudes, desniveles
```

### CU-033: Eliminar actividad del historial
```gherkin
Dado que el usuario está en "Tu historial de actividad"
Cuando desliza hacia la derecha sobre una actividad específica
Entonces aparece una animación de "tachito" de basura deslizándose sobre el registro
Y la animación continúa hasta el extremo derecho de la pantalla
Y el registro desaparece de la lista con animación suave
Y la actividad se elimina permanentemente del historial
Y se actualizan las estadísticas globales del usuario
```

---

## 🎮 **FUNCIONALIDAD: Interacciones y Animaciones**

### CU-034: Interactuar con estadísticas expandidas
```gherkin
Dado que el usuario está en cualquier pantalla de tracking o seguimiento
Cuando desliza hacia arriba el panel de "Datos del recorrido"
Entonces las estadísticas se despliegan con animación "Stats Bloom"
Y cada métrica aparece como un pétalo que florece desde el centro
Y se muestra el panel completo con todas las estadísticas detalladas
Y el usuario puede deslizar hacia abajo para colapsar el panel
```

### CU-035: Visualizar ruta con animación
```gherkin
Dado que el usuario selecciona una ruta guardada para visualizar
Cuando la ruta se carga en el mapa
Entonces la ruta se dibuja progresivamente con animación "Trail Magic"
Y la línea se traza desde el punto de inicio hasta el final como si fuera escrita con un marcador brillante
Y se muestra un efecto de "glow" en la punta que se va moviendo
Y la animación completa dura aproximadamente 2-3 segundos
```

### CU-036: Interactuar con brújula
```gherkin
Dado que el usuario está en cualquier pantalla con mapa
Cuando toca repetidamente la brújula
Entonces cada toque genera la animación "Compass Wobble"
Y la brújula se tambalea con física realista (inercia, amortiguación)
Y los números de grados se mueven sutilmente siguiendo el movimiento
Y el mapa se reorienta al norte cada vez
Y la interacción es satisfactoria y adictiva de repetir
```

---

## 📱 **FUNCIONALIDAD: Gestión General de la App**

### CU-037: Inicializar aplicación
```gherkin
Dado que el usuario abre la aplicación por primera vez
Cuando la app se carga completamente
Entonces se solicitan los permisos necesarios: ubicación, cámara, almacenamiento
Y se muestra la pantalla principal del mapa
Y se detecta automáticamente la ubicación actual del usuario
Y se inicializan todos los sensores: GPS, acelerómetro, barómetro, magnetómetro
Y se muestra el bottom navigation con "Guardados", "Mapa", "Historial"
```

### CU-038: Manejar permisos
```gherkin
Dado que la aplicación necesita permisos específicos
Cuando el usuario no ha otorgado permisos de ubicación
Entonces se muestra un mensaje explicativo sobre la necesidad del permiso
Cuando el usuario no ha otorgado permisos de cámara
Entonces las funciones de fotografía se deshabilitan temporalmente
Cuando todos los permisos son otorgados
Entonces se habilitan todas las funcionalidades de la aplicación
```

### CU-039: Funcionar en modo offline
```gherkin
Dado que el usuario tiene rutas descargadas en "Guardados"
Cuando no hay conexión a internet
Entonces las rutas descargadas siguen disponibles para seguimiento
Y el tracking GPS funciona normalmente
Y las fotos se almacenan localmente
Y las estadísticas se calculan sin conexión
Y se sincroniza la información cuando se recupera la conexión
```

---

## 🧪 **Testing y Validación**

### Casos de Uso para Testing:

#### CU-T001: Validar precisión de GPS
```gherkin
Dado que el usuario está grabando una ruta
Cuando se mueve por un recorrido conocido
Entonces la distancia calculada debe tener una precisión mínima del 95%
Y la velocidad mostrada debe corresponder al movimiento real
Y la altitud debe actualizarse según el barómetro
```

#### CU-T002: Validar conteo de pasos
```gherkin
Dado que el usuario está caminando con la app activa
Cuando da pasos reales
Entonces el acelerómetro debe detectar los pasos con precisión del 90%
Y el conteo debe incrementarse en tiempo real
Y debe diferenciarse del movimiento en vehículo
```

#### CU-T003: Validar funcionamiento de cámara
```gherkin
Dado que el usuario está en modo de grabación
Cuando presiona el ícono de cámara
Entonces la cámara debe abrirse sin errores
Y debe capturar fotos con la resolución correcta
Y debe guardar correctamente la ubicación GPS de cada foto
```

---

## 📋 **Resumen de Cobertura de Requerimientos**

### ✅ **Sensores Implementados:**
- **Movimiento:** Acelerómetro (pasos, detección movimiento)
- **Ambiental:** Barómetro (altitud, presión atmosférica)
- **Posición:** Magnetómetro (orientación, brújula digital)
- **Geolocalización:** GPS (tracking, posicionamiento)

### ✅ **Componentes Obligatorios:**
- **Cámara:** Captura durante recorridos y gestión de fotos
- **Vista Jetpack Compose:** Toda la aplicación implementada en Compose
- **Animaciones:** 5 animaciones diferentes implementadas
- **Mapa:** Visualización interactiva con múltiples tipos
- **Geolocalización:** Tracking en tiempo real y navegación

### ✅ **Arquitectura y Buenas Prácticas:**
- **Clean Architecture + Hexagonal:**
- **MVVM:** ViewModels con StateFlow para manejo de estado
- **Testing:** Casos de uso específicos para validación de componentes
