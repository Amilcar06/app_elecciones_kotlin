# INFORME DE REVISIÓN DEL PROYECTO
## Sistema de Elecciones - Carrera de Informática UMSA

Fecha de revisión: 2024

---

## RESUMEN EJECUTIVO

La aplicación tiene una **base sólida** con la arquitectura correcta (MVVM + Room + Compose) y las entidades principales bien definidas. Sin embargo, hay **varios aspectos críticos faltantes** según la documentación del proyecto, especialmente relacionados con:

1. **Registro de votos nulos y blancos** (NO implementado)
2. **Pantalla de "Registrar Votos"** según especificación (incompleta)
3. **Cálculo y visualización del ganador** (NO implementado)
4. **Validaciones y reglas de negocio** (parcialmente implementadas)
5. **Cambio de estado a "Cerrada"** automático (NO implementado)
6. **Gráficos visuales** en resultados (solo hay LinearProgressIndicator, falta PieChart)

---

## 1. ENTIDADES Y BASE DE DATOS

### ✅ CUMPLIDO

- **Frente**: Todos los campos requeridos están presentes (`id_frente`, `nombre`, `color`, `logo_url`, `fecha_fundacion`, `descripcion`)
- **Candidato**: Todos los campos requeridos están presentes, incluyendo `ci` con restricción UNIQUE
- **Elección**: Incluye `votos_nulos` y `votos_blancos` con valores por defecto 0 ✅
- **Resultado**: Entidad correctamente definida con constraint único (`id_eleccion`, `id_frente`)
- **Relaciones**: Foreign Keys correctamente definidas
- **AppDatabase**: Configurada correctamente con todas las entidades

### ⚠️ OBSERVACIONES

- El campo `descripcion` en Elección NO está presente en la entidad (solo está en la documentación)
- Falta validación de que `votos_nulos` y `votos_blancos` sean ≥ 0 a nivel de base de datos

---

## 2. DAOs Y OPERACIONES DE BASE DE DATOS

### ✅ CUMPLIDO

- Todos los DAOs están implementados correctamente
- Operaciones CRUD básicas funcionando
- `getFrentesConVotos` implementado con JOIN correcto
- Método `registrarVoto` existe (pero solo incrementa en 1, no permite establecer cantidad)

### ❌ FALTANTE

- No hay método para actualizar múltiples resultados a la vez
- No hay método para actualizar `votos_nulos` y `votos_blancos` en Elección desde el DAO específico
- No hay validaciones a nivel de DAO

---

## 3. REPOSITORIO

### ✅ CUMPLIDO

- Métodos básicos del repositorio implementados
- `getFrentesConVotos` implementado correctamente
- `insertarParticipantes` crea registros con votos = 0 ✅

### ❌ FALTANTE

- No hay método para actualizar votos nulos y blancos de una elección
- No hay método para actualizar múltiples resultados simultáneamente
- No hay método para calcular el ganador
- No hay método para cerrar una elección (cambiar estado a "Cerrada")

---

## 4. VIEWMODELS

### ✅ CUMPLIDO

- `FrenteViewModel`: Implementado correctamente
- `CandidatoViewModel`: Implementado correctamente
- `EleccionViewModel`: Implementado con StateFlows para observación reactiva

### ❌ FALTANTE

- No hay método en `EleccionViewModel` para actualizar votos nulos y blancos
- No hay método para registrar votos masivamente (actualmente solo incrementa en 1)
- No hay método para cerrar elección y calcular ganador automáticamente
- No hay validaciones de reglas de negocio en ViewModels

---

## 5. PANTALLAS UI

### ✅ CUMPLIDO

#### 5.1 Pantalla de Inicio - Lista de Frentes
- ✅ Muestra encabezado "Frentes registrados"
- ✅ Botón flotante "+"
- ✅ Lista tipo card con logo, nombre, chip con color, año de fundación
- ✅ Icono de flecha para ver candidatos
- ⚠️ Falta menú contextual para editar/eliminar

#### 5.2 Pantalla "Registrar Frente"
- ✅ Formulario con todos los campos requeridos
- ✅ Botones Guardar y Cancelar
- ⚠️ Selector de color es un TextField (debería ser un ColorPicker o paleta)
- ⚠️ Selector de imagen no está implementado (logo_url se guarda como null)
- ⚠️ DatePicker no está implementado (solo TextField)

#### 5.3 Pantalla "Candidatos del Frente"
- ✅ Header con nombre del frente
- ✅ Botón flotante "Agregar candidato"
- ✅ Lista de candidatos con información
- ⚠️ No muestra profesión ni cargo postulado en la lista

#### 5.4 Pantalla "Registrar Candidato"
- ✅ Todos los campos requeridos están presentes
- ✅ Validación básica (nombre, paterno, CI obligatorios)
- ⚠️ No muestra el nombre del frente asociado (solo un placeholder)
- ⚠️ Falta campo `anios_experiencia` (está en la entidad pero no en el formulario)
- ⚠️ Falta campo `direccion` (está en la entidad pero no en el formulario)
- ⚠️ Falta validación de formato de email
- ⚠️ Falta validación de CI único (solo se valida a nivel de base de datos)

#### 5.5 Pantalla "Elecciones"
- ✅ Lista de elecciones con gestión, fecha y estado
- ✅ Botón "Nueva Elección"
- ✅ Menú de cada elección (a través de click en la card)
- ⚠️ El menú contextual no está implementado (solo navega según estado)

#### 5.6 Pantalla "Registrar Elección"
- ✅ Campos: Fecha, Gestión
- ✅ Estado inicial: "Programada" ✅
- ❌ **FALTA** campo "Descripción" (no está en la entidad ni en la pantalla)

#### 5.7 Pantalla "Participantes de la Elección"
- ✅ Muestra todos los frentes
- ✅ Checkbox para seleccionar participantes
- ✅ Al guardar, crea registros en Resultado con votos = 0 ✅
- ❌ **FALTA** validación: solo frentes con candidatos deberían poder participar
- ❌ **FALTA** indicador visual de qué frentes tienen candidatos

#### 5.8 Pantalla "Registrar Votos" ⚠️ **CRÍTICO - NO CUMPLE ESPECIFICACIÓN**

**Según la documentación (7.8), esta pantalla debe:**
- ✅ Mostrar lista de frentes participantes
- ❌ **FALTA** Input numérico para "Votos obtenidos" por cada frente (actualmente solo permite votar 1 vez)
- ❌ **FALTA** Input numérico: "Votos Blancos"
- ❌ **FALTA** Input numérico: "Votos Nulos"
- ❌ **FALTA** Botón "Guardar votos"
- ❌ **FALTA** Al guardar:
  - Actualizar tabla Resultado con los votos de cada frente
  - Actualizar tabla Eleccion con votos_nulos y votos_blancos
  - Recalcular porcentajes
  - Marcar ganador
  - Cambiar estado de elección → "Cerrada"

**Estado actual:** La pantalla `VotacionScreen.kt` solo permite votar 1 vez por frente (incrementa en 1), no permite registrar la cantidad total de votos de cada frente.

#### 5.9 Pantalla "Resultados de la Elección"
- ✅ Solo disponible cuando estado = "Cerrada" (a través de navegación)
- ✅ Muestra lista de resultados ordenados por votos
- ✅ Muestra porcentajes calculados automáticamente
- ✅ Muestra LinearProgressIndicator (gráfico de barras)
- ❌ **FALTA** PieChart (gráfico circular) según especificación
- ❌ **FALTA** Indicador visual del frente ganador
- ❌ **FALTA** Mostrar votos nulos y blancos en los resultados
- ❌ **FALTA** Incluir votos nulos y blancos en el cálculo del total

---

## 6. VALIDACIONES Y REGLAS DE NEGOCIO

### ❌ VALIDACIONES FALTANTES

#### 6.1 FRENTE
- ⚠️ Validaciones básicas de campos presentes (nombre, fecha)
- ❌ No se valida formato de fecha ISO
- ❌ No se valida formato de color hexadecimal
- ❌ **REGLAS DE NEGOCIO FALTANTES:**
  - No se valida si un frente tiene candidatos antes de eliminarlo (solo FK RESTRICT)
  - No se valida si un frente está en elección activa antes de eliminarlo

#### 6.2 CANDIDATO
- ✅ CI único validado a nivel de base de datos
- ⚠️ Validación básica de campos obligatorios
- ❌ **FALTANTE:** Validación de formato de email
- ❌ **FALTANTE:** Validación de formato de teléfono
- ❌ **REGLAS DE NEGOCIO FALTANTES:**
  - No se valida que el correo sea único (solo CI es único)
  - No se impide cambiar el frente después de creado (no hay validación en UI ni ViewModel)

#### 6.3 ELECCIÓN
- ⚠️ Validación básica de campos obligatorios
- ❌ **FALTANTE:** Validación de fecha ≥ hoy
- ❌ **FALTANTE:** Validación de gestión (año válido)
- ❌ **FALTANTE:** Validación de estado válido ("Programada", "Abierta", "Cerrada")
- ❌ **FALTANTE:** Validación votos_nulos ≥ 0
- ❌ **FALTANTE:** Validación votos_blancos ≥ 0
- ❌ **REGLAS DE NEGOCIO FALTANTES:**
  - No se valida que no se pueda cerrar sin participantes
  - No se valida que no se pueda cerrar sin votos
  - No se valida que la gestión sea única por año (sugerido en doc)
  - No se valida que no se pueda revertir estado si ya cerró

#### 6.4 RESULTADO
- ✅ Constraint único (id_eleccion, id_frente) implementado
- ❌ **REGLAS DE NEGOCIO FALTANTES:**
  - No se valida que solo frentes con candidatos puedan participar
  - No se valida mínimo 1 participante
  - No se valida votos ≥ 0
  - No se valida que no se pueda modificar votos si la elección está "Cerrada"

---

## 7. FLUJO COMPLETO DE LA APLICACIÓN

### ✅ FLUJOS IMPLEMENTADOS

1. ✅ Registrar frentes → Funcional
2. ✅ Agregar candidatos dentro de cada frente → Funcional
3. ✅ Crear una elección → Funcional (falta descripción)
4. ✅ Seleccionar frentes participantes → Funcional (falta validación de candidatos)
5. ⚠️ Registrar votos → **PARCIALMENTE IMPLEMENTADO** (no cumple especificación)
6. ⚠️ Calcular porcentajes → Funcional pero incompleto (no incluye nulos/blancos)
7. ❌ Determinar al ganador → **NO IMPLEMENTADO**
8. ❌ Cambiar estado a "Cerrada" → **NO IMPLEMENTADO AUTOMÁTICAMENTE**
9. ✅ Visualizar resultados → Funcional pero incompleto (falta gráfico circular y ganador)
10. ✅ Consultar histórico de elecciones → Funcional

---

## 8. COMPONENTES REUTILIZABLES

### ✅ IMPLEMENTADOS
- `CardFrente` ✅
- `CardCandidato` ✅
- `CardEleccion` ✅

### ❌ FALTANTES
- `InputTextField` (no existe como componente reutilizable, se usa directamente)
- `DatePicker` (no implementado)
- `ColorSelector` (no implementado, solo TextField)
- `PieChart` (no implementado, solo LinearProgressIndicator)
- Dialogs de confirmación (no implementados)

---

## 9. ARQUITECTURA

### ✅ CUMPLIDO
- Estructura de carpetas correcta (`data/`, `repository/`, `viewmodel/`, `ui/`)
- Uso de MVVM correcto
- Room para persistencia ✅
- Jetpack Compose para UI ✅
- Material 3 design ✅
- StateFlow para observación reactiva ✅

---

## 10. PRIORIDADES PARA COMPLETAR EL PROYECTO

### 🔴 PRIORIDAD ALTA (Crítico para funcionamiento)

1. **Implementar pantalla "Registrar Votos" según especificación 7.8**
   - Inputs numéricos para votos de cada frente
   - Inputs para votos nulos y blancos
   - Botón guardar que actualice Resultado y Eleccion
   - Cambiar estado a "Cerrada" automáticamente
   - Calcular y marcar ganador

2. **Implementar cálculo del ganador**
   - Lógica en Repository/ViewModel para determinar frente con más votos
   - Almacenar o calcular dinámicamente el ganador

3. **Validar reglas de negocio en ParticipantesScreen**
   - Solo permitir seleccionar frentes que tengan candidatos

4. **Actualizar ResultadosScreen**
   - Incluir votos nulos y blancos en visualización
   - Mostrar indicador visual del ganador
   - Agregar PieChart además del LinearProgressIndicator

### 🟡 PRIORIDAD MEDIA (Importante para completitud)

5. **Agregar validaciones faltantes**
   - Validar formato de fechas
   - Validar formato de email
   - Validar estados de elección
   - Validar que no se pueda modificar elección cerrada

6. **Mejorar UI de Registrar Frente**
   - Implementar DatePicker real
   - Implementar ColorPicker/selector de color
   - Implementar selector de imagen para logo

7. **Agregar campo "Descripción" a Elección**
   - En la entidad
   - En la pantalla de registro
   - En la visualización

8. **Implementar menú contextual en FrentesScreen**
   - Editar frente
   - Eliminar frente (con validaciones)

### 🟢 PRIORIDAD BAJA (Mejoras opcionales)

9. **Mejorar formulario de Candidato**
   - Agregar campos faltantes (dirección, años experiencia)
   - Mostrar nombre del frente asociado
   - Validación de email único (si se requiere)

10. **Implementar componente PieChart**
    - Usar biblioteca como `MPAndroidChart` o `com.github.PhilJay:MPAndroidChart`

11. **Implementar Dialogs de confirmación**
    - Para eliminar frentes
    - Para cerrar elecciones
    - Para confirmar guardado de votos

---

## CONCLUSIÓN

La aplicación tiene una **base arquitectónica sólida** y muchas funcionalidades implementadas correctamente. Sin embargo, la **funcionalidad crítica de registro de votos no cumple con la especificación documentada**. Es necesario implementar la pantalla "Registrar Votos" completa según el punto 7.8 de la documentación, que es el corazón del flujo electoral.

**Estado general: 65% completo**

- ✅ Arquitectura y estructura: 95%
- ✅ Entidades y base de datos: 90%
- ✅ Pantallas básicas: 70%
- ❌ Pantalla de registro de votos: 30%
- ❌ Validaciones y reglas de negocio: 40%
- ✅ Visualización de resultados: 60%

---

**Recomendación:** Priorizar la implementación de la pantalla "Registrar Votos" completa y el flujo de cierre de elección, ya que son funcionalidades críticas sin las cuales el sistema no cumple su propósito principal.

