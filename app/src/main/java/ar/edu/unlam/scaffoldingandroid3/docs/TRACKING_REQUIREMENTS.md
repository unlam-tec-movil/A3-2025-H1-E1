# Requerimientos - Funcionalidad Tracking GPS

## 🎯 Objetivo
Implementar la funcionalidad de "Grabar nueva ruta" con tracking GPS básico, sensores y gestión de fotos.

**IMPORTANTE:**
- ✅ **Revisar código existente** antes de implementar para reutilizar componentes
- ✅ **Implementación SENCILLA** - priorizar funcionalidad sobre complejidad
- ✅ **Usar arquitectura existente** del proyecto sin cambios mayores

---

## 📱 Flujo Principal Completo

### 1. NAVEGACIÓN INICIAL
**Desde:** Mapa principal (botón "Nueva ruta")
**Hacia:** Pantalla tracking con mapa centrado automáticamente en ubicación actual
**Elementos visibles:**
- Mapa centrado en ubicación del usuario
- Botón "Iniciar recorrido" prominente y destacado
- Ícono de ubicación del usuario (marker azul)
- Botón de retroceso normal en header

**Comportamiento retroceso en estado inicial:**
- Navegación normal de vuelta al mapa principal (sin confirmación)
- No hay datos que perder todavía

---

### 2. TRACKING ACTIVO

#### Al presionar "Iniciar recorrido":
**Cambios inmediatos en UI:**
- ✅ Botón "Iniciar recorrido" desaparece completamente
- ✅ Aparecen botones "Pausar" y "Detener"
- ✅ Aparece panel de datos desplegable (inicialmente plegado)
- ✅ Aparece ícono de cámara en esquina superior izquierda
- ✅ Contador de fotos inicial: 📷 [0]

**Funcionalidades que se activan:**
- ✅ Inicia cronómetro desde 00:00:00
- ✅ Comienza tracking GPS (captura coordenadas cada 1-2 segundos)
- ✅ Inicia dibujo de ruta en mapa (polyline azul)
- ✅ Activa sensores: acelerómetro (pasos) y barómetro (altitud)

#### Panel de datos - Estado PLEGADO:
```
┌─────────────────────────────────────┐
│ Tiempo: 00:15:32 | Distancia: 1.2km │
│ Pasos: 1,250                        │
│ [Pausar] [Detener]                  │ 
└─────────────────────────────────────┘
```

#### Panel de datos - Estado EXPANDIDO (mitad pantalla):
```
┌─────────────────────────────────────┐
│ Tiempo: 00:15:32                    │
│ Distancia: 1.2 km                   │
│ Pasos: 1,250                        │
│ ──────────────────                  │
│ Velocidad Media: 4.2 km/h           │
│ Velocidad Máxima: 6.1 km/h          │
│ Altitud actual: 445m                │
│ ──────────────────                  │
│ [Pausar] [Detener]                  │
└─────────────────────────────────────┘
```

**Interacción del panel:**
- **Expandir:** Deslizar hacia arriba sobre el panel → se expande a mitad de pantalla
- **Colapsar:** Deslizar hacia abajo sobre el panel expandido → vuelve a estado plegado
- **Animación:** Transición suave de expansión/colapso

---

### 3. DATOS Y SENSORES ESPECÍFICOS

#### Unidades de medida exactas:
- **Tiempo:** Formato HH:MM:SS (ej: 01:23:45)
- **Distancia:** Kilómetros con 2 decimales (ej: 3.47 km)
- **Velocidad:** km/h con 1 decimal (ej: 4.2 km/h)
- **Altitud:** Metros sin decimales (ej: 445m)
- **Pasos:** Números enteros (ej: 4,250)

#### Sensores requeridos y su uso:
- **GPS:** Coordenadas lat/lng para dibujo de ruta y cálculo de distancia
- **Acelerómetro:** Conteo de pasos en tiempo real
- **Barómetro:** Altitud actual (presión atmosférica → metros sobre nivel del mar)

#### Cálculos específicos:
- **Distancia total:** Suma de distancias entre puntos GPS consecutivos
- **Velocidad actual:** Distancia entre últimos 2 puntos / tiempo transcurrido
- **Velocidad media:** Distancia total / tiempo en movimiento (sin pausas)
- **Velocidad máxima:** Mayor velocidad instantánea registrada
- **Altitud actual:** Directa del sensor barómetro
- **Altitud min/max:** Registrar todas las altitudes durante tracking para calcular al final

---

### 4. ESTADOS DE PAUSA DETALLADOS

#### Al presionar "Pausar":
**Lo que se DETIENE:**
- ✅ Cronómetro se pausa (mantiene tiempo acumulado)
- ✅ Tracking GPS se pausa (NO agrega nuevos puntos a la ruta)
- ✅ Conteo de pasos se pausa
- ✅ Cálculo de velocidades se pausa
- ✅ Dibujo de polyline se detiene

**Lo que CONTINÚA:**
- ✅ Altitud actual sigue actualizándose (sensor siempre activo)
- ✅ Se mantienen todos los datos acumulados hasta ese momento

**Cambios en UI:**
- ✅ Botón "Pausar" cambia a "Reanudar"
- ✅ Botones visibles: [Reanudar] [Detener]

#### Al presionar "Reanudar":
- ✅ Cronómetro continúa desde tiempo acumulado
- ✅ Tracking GPS se reactiva desde posición actual
- ✅ Conteo de pasos se reactiva
- ✅ Cálculo de velocidades se reactiva
- ✅ Dibujo de ruta continúa desde nueva posición
- ✅ **IMPORTANTE:** Habrá gap visual en la ruta si el usuario se movió durante pausa (esto es correcto)
- ✅ Botón vuelve a "Pausar"

#### Comportamiento si usuario se mueve durante pausa:
- El usuario puede caminar/moverse libremente
- Su posición NO se registra en la ruta (no contamina el tracking)
- Al reanudar, la ruta continúa desde la nueva posición
- Resultado: gap visual en el mapa (comportamiento profesional correcto)

---

### 5. DIBUJO DE RUTA CON ANIMACIÓN

#### Comportamiento básico:
- Dibujar polyline azul vibrante (#2196F3) conforme se agregan puntos GPS
- Grosor: 8dp
- Frecuencia: Agregar punto cada 5-10 metros mínimo (optimización)

#### Animación "Trail Magic":
- **Descripción:** La línea se dibuja progresivamente como trazada con marcador luminoso
- **Efecto:** Punto brillante que avanza en la punta de la línea
- **Duración:** Cada nuevo segmento se anima por ~300ms

#### Comportamiento durante estados:
- **Tracking activo:** Se dibuja en tiempo real
- **Pausado:** NO se agregan nuevos puntos (línea se detiene)
- **Reanudado:** Continúa desde nueva posición (gap visual es correcto)

---

### 6. FUNCIONALIDAD CÁMARA COMPLETA

#### Ubicación y visibilidad:
- **Posición:** Esquina superior izquierda del mapa
- **Visibilidad:** Solo durante tracking activo (no en estado inicial)
- **Contador inicial:** 📷 [0]
- **Límite sugerido:** Máximo 10 fotos por recorrido

#### Flujo completo de captura:
1. **Al tocar ícono cámara:**
    - Se abre cámara nativa del dispositivo
    - Tracking GPS continúa funcionando en background
    - Usuario toma foto usando interfaz nativa

2. **Manejo post-captura:**
    - Cámara nativa muestra preview con "Retry" / "OK" automáticamente
    - Si "Retry": permite volver a tomar la foto
    - Si "OK": foto se acepta y guarda para el recorrido

3. **Datos automáticos capturados por foto:**
    - URI de la imagen guardada localmente
    - Coordenadas GPS exactas donde se tomó (lat/lng)
    - Timestamp de captura (momento exacto)
    - Altitud en ese momento (del barómetro)

4. **Actualización inmediata de UI:**
    - Incrementar contador: 📷 [0] → 📷 [1] → 📷 [2], etc.
    - Foto queda almacenada internamente para mostrar después

---

### 7. FINALIZAR TRACKING

#### Al presionar "Detener":
**Cálculos finales automáticos:**
- Tiempo total transcurrido (incluyendo pausas)
- Tiempo en movimiento real (excluyendo pausas)
- Distancia total en km
- Pasos totales contados
- Velocidad media (distancia / tiempo en movimiento)
- Velocidad máxima registrada
- Altitud mínima alcanzada
- Altitud máxima alcanzada
- Ruta completa (array de coordenadas GPS)
- Fotos capturadas con metadatos

**Navegación:**
- Navegar automáticamente a pantalla "Guardar recorrido"
- Pasar todos los datos calculados como parámetros

---

### 8. PANTALLA GUARDAR RECORRIDO COMPLETA

#### Layout exacto:
```
┌─────────────────────────────────────┐
│ ← Guardar recorrido            🗑️   │
├─────────────────────────────────────┤
│ Tiempo de grabación: 01:23:45       │
│ Pasos: 4,250                        │
│ Distancia recorrida: 3.47 km        │
│ Velocidad Media: 4.2 km/h           │
│ Velocidad Máxima: 6.3 km/h          │
│ Altitud mínima: 420m                │
│ Altitud máxima: 465m                │
├─────────────────────────────────────┤
│ Nombre:                             │
│ [_________________________]        │
├─────────────────────────────────────┤
│ Fotos: 3                      [+]   │
│ [📷] [📷] [📷]                      │
├─────────────────────────────────────┤
│           [Guardar recorrido]       │
└─────────────────────────────────────┘
```

#### Funcionalidades específicas:

**Campo nombre:**
- Input de texto obligatorio
- Placeholder: "Ingresa nombre para esta caminata"
- Validación: No permitir guardar si está vacío
- Máximo sugerido: 50 caracteres

**Sección estadísticas:**
- Mostrar todos los datos calculados (solo lectura)
- Formato exacto como se muestra arriba

**Gestión de fotos:**
- Mostrar fotos capturadas durante tracking como thumbnails
- Contador actual: "Fotos: X"
- **Botón [+] para agregar más fotos:**
    - Al tocar [+]: mostrar modal con opciones "Galería" / "Tomar una foto"
    - "Tomar una foto": abre cámara para capturar nueva foto
    - "Galería": abre galería del dispositivo para seleccionar fotos existentes
- Permitir eliminar fotos individuales (ícono X en cada thumbnail)
- Al tocar thumbnail: mostrar foto en tamaño completo

**Botón "Guardar recorrido":**
- Validar que campo nombre no esté vacío
- Guardar todos los datos en base de datos local
- Agregar al historial de actividades del usuario
- Mostrar mensaje: "Recorrido guardado exitosamente"
- Navegar de vuelta al mapa principal

**Botón eliminar (🗑️ en header):**
- Mostrar confirmación: "¿Eliminar este recorrido?"
- Opciones: "Eliminar" / "Cancelar"
- Si confirma: descartar todos los datos y volver al mapa principal

---

### 9. MANEJO DE RETROCESO DURANTE TRACKING

#### Durante tracking activo (no pausado):
- **Al presionar botón atrás:** Mostrar dialog "¿Descartar recorrido?"
- **Opciones:** "Descartar" / "Cancelar"
- **Si "Cancelar":** Vuelve al tracking sin cambios
- **Si "Descartar":** Elimina todos los datos y vuelve al mapa principal

#### Durante tracking pausado:
- **Mismo comportamiento:** Dialog de confirmación
- **Razón:** Hay datos acumulados que se perderían

---

## 🎨 Animaciones Requeridas (5 total)

### 1. Trail Magic - Dibujo progresivo de ruta
**Descripción:** La ruta se dibuja gradualmente como trazada con marcador luminoso
**Trigger:** Cada vez que se agrega nuevo punto GPS
**Efecto:** Punto brillante que avanza en la punta de la línea

### 2. Panel desplegable - Expansión/colapso
**Descripción:** Panel se expande suavemente a mitad de pantalla o se colapsa
**Trigger:** Deslizar hacia arriba/abajo sobre el panel
**Duración:** ~400ms con easing suave

### 3. Contador de fotos - Incremento animado
**Descripción:** Cuando se agrega foto, el número se anima
**Trigger:** Al confirmar nueva foto capturada
**Efecto:** Slide up del número anterior, slide in del nuevo

### 4. Estados de botones - Morphing
**Descripción:** Transición suave entre "Pausar" ↔ "Reanudar" ↔ "Detener"
**Trigger:** Al cambiar estados de tracking
**Duración:** ~250ms

### 5. Dialogs de confirmación - Fade in/out
**Descripción:** Aparición suave de dialogs
**Trigger:** Al presionar atrás durante tracking o botón eliminar
**Efecto:** Fade in con slide up desde abajo

---

## 🏗️ Estructura de Datos Requerida

### Estados básicos del tracking:
```
isTracking: Boolean (false → true al iniciar)
isPaused: Boolean (false → true al pausar)  
isPanelExpanded: Boolean (false → true al expandir)
```

### Estadísticas en tiempo real:
```
tiempo: String (formato "HH:MM:SS")
distancia: Double (en km, 2 decimales)
pasos: Int (número entero)
velocidadMedia: Double (en km/h, 1 decimal)
velocidadMaxima: Double (en km/h, 1 decimal)
altitudActual: Double (en metros, sin decimales)
```

### Datos de ruta:
```
rutaPuntos: List<LatLng> (coordenadas GPS del recorrido)
fotosCapturadas: List<TrackingPhoto>
```

### Estructura de foto:
```
TrackingPhoto:
- uri: String (ruta local de la imagen)
- latitude: Double (coordenada donde se tomó)
- longitude: Double (coordenada donde se tomó)
- timestamp: Long (momento de captura)
- altitude: Double (altitud cuando se tomó)
```

### Resultado final para guardar:
```
TrackingResult:
- tiempoTotal: String (tiempo total con pausas)
- tiempoEnMovimiento: String (tiempo sin pausas)
- distanciaTotal: Double (km totales)
- pasosTotales: Int (pasos totales)
- velocidadMedia: Double (km/h promedio)
- velocidadMaxima: Double (km/h máxima)
- altitudMinima: Double (metros, punto más bajo)
- altitudMaxima: Double (metros, punto más alto)
- rutaCompleta: List<LatLng> (todos los puntos GPS)
- fotosCapturadas: List<TrackingPhoto> (todas las fotos)
- nombreRecorrido: String (ingresado por usuario)
- fechaCreacion: Long (timestamp de creación)
```

---

## 🎯 Implementación por Fases

### FASE 1 - Core básico (CRÍTICO):
1. ✅ Navegación desde mapa principal a pantalla tracking
2. ✅ Estado inicial con botón "Iniciar recorrido" centrado en ubicación
3. ✅ Tracking GPS básico con captura de coordenadas
4. ✅ Dibujo básico de polyline en tiempo real
5. ✅ Cronómetro funcional con formato HH:MM:SS
6. ✅ Estados básicos: start/pause/resume/stop con botones correctos
7. ✅ Panel de datos plegado con información básica

### FASE 2 - Sensores y cálculos (ALTA):
1. ✅ Integración acelerómetro para conteo de pasos
2. ✅ Integración barómetro para altitud actual
3. ✅ Cálculos precisos de distancia y velocidades
4. ✅ Panel expandido con estadísticas completas
5. ✅ Manejo correcto de pausas (detener sensores apropiadamente)

### FASE 3 - Cámara y persistencia (ALTA):
1. ✅ Funcionalidad completa de cámara durante tracking
2. ✅ Contador de fotos con metadatos automáticos
3. ✅ Pantalla "Guardar recorrido" con todas las funcionalidades
4. ✅ Persistencia en base de datos local
5. ✅ Validaciones y manejo de errores básicos

### FASE 4 - Animaciones y UX (MEDIA):
1. ✅ Animación "Trail Magic" del dibujo de ruta
2. ✅ Animaciones del panel desplegable
3. ✅ Transiciones entre estados de botones
4. ✅ Animaciones de contador de fotos
5. ✅ Dialogs de confirmación con animaciones

---

## 💡 Notas de Implementación

### Revisar código existente ANTES de implementar:
- ✅ **BaseGoogleMap.kt** - reutilizar para el mapa base
- ✅ **Navigation** existente - mantener patrones de navegación
- ✅ **ViewModels** existentes - seguir estructura MVVM establecida
- ✅ **Repositorios** de ubicación/sensores - ver qué componentes ya existen
- ✅ **Base de datos** local - usar schema existente, agregar tablas si es necesario
- ✅ **Permisos** - verificar qué permisos ya están configurados

### Mantener implementación sencilla:
- ✅ **No over-engineering** - funcionalidad básica primero
- ✅ **Reutilizar máximo** de componentes existentes
- ✅ **Implementación directa** sin abstracciones innecesarias
- ✅ **Testing mínimo** - solo validaciones esenciales

### Optimizaciones básicas requeridas:
- Actualizar GPS cada 1-2 segundos máximo
- Filtrar puntos GPS muy cercanos (mínimo 10 metros entre puntos)
- Liberar recursos de sensores al salir de la pantalla
- Mantener pantalla encendida durante tracking activo

### Permisos requeridos (verificar si ya existen):
- ACCESS_FINE_LOCATION (para GPS preciso)
- ACCESS_COARSE_LOCATION (fallback de ubicación)
- CAMERA (para captura de fotos)
- WRITE_EXTERNAL_STORAGE (para guardar fotos)
- ACTIVITY_RECOGNITION (para conteo de pasos)

---

## ✅ Checklist de Funcionalidades Completas

### Funcionalidades principales:
- [ ] Navegación desde botón "Nueva ruta" a pantalla tracking
- [ ] Estado inicial con mapa centrado y botón "Iniciar recorrido"
- [ ] Al iniciar: aparecen botones Pausar/Detener, panel datos, ícono cámara
- [ ] Tracking GPS dibuja ruta en tiempo real con polyline azul
- [ ] Cronómetro cuenta tiempo en formato HH:MM:SS
- [ ] Panel de datos plegado muestra: tiempo, distancia, pasos
- [ ] Panel expandido (deslizar arriba) muestra: velocidades y altitud
- [ ] Botón Pausar detiene todo excepto altitud
- [ ] Botón Reanudar continúa desde donde quedó (con gap si se movió)
- [ ] Sensores funcionan: acelerómetro (pasos) y barómetro (altitud)
- [ ] Cálculos correctos: distancia, velocidades media/máxima
- [ ] Cámara funciona con contador incremental 📷 [X]
- [ ] Fotos se guardan con metadatos (coordenadas, timestamp, altitud)
- [ ] Dialog confirmación al presionar atrás durante tracking
- [ ] Al detener: navega a pantalla "Guardar recorrido"
- [ ] Pantalla guardar muestra todas las estadísticas calculadas
- [ ] Campo nombre obligatorio con validación
- [ ] Gestión de fotos: thumbnails + botón [+] con opciones
- [ ] Botón "Guardar recorrido" persiste datos y vuelve al mapa
- [ ] Botón eliminar (🗑️) descarta con confirmación
- [ ] Animación "Trail Magic" en dibujo de ruta
- [ ] Unidades correctas: km (2 dec), km/h (1 dec), metros, HH:MM:SS

### Validaciones técnicas:
- [ ] Reutiliza BaseGoogleMap.kt existente
- [ ] Sigue patrones de navegación del proyecto
- [ ] Usa arquitectura MVVM establecida
- [ ] Integra con base de datos existente
- [ ] Maneja permisos apropiadamente
- [ ] Optimiza performance (GPS cada 2s, filtrar puntos cercanos)
- [ ] Libera recursos al salir

---

**ENFOQUE FINAL:** Implementación completa pero sencilla, reutilizando máximo de componentes existentes, con todas las funcionalidades especificadas pero sin complejidad innecesaria. El documento cubre 100% de lo discutido en la conversación.