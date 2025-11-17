# Propuesta de Mejora de UX para el Flujo de Elecciones

## Problema Actual
El flujo actual es muy fragmentado y requiere múltiples navegaciones:
1. Crear Elección
2. Ir a Puestos
3. Crear Puesto
4. Ir a Detalle del Puesto
5. Agregar Candidatos (uno por uno)
6. Registrar Votos
7. Ver Resultados

## Propuesta: Flujo Asistido Paso a Paso

### Opción 1: Asistente de Configuración Rápida (Recomendado)

**Pantalla: "Configurar Elección"**
Un asistente paso a paso que guía al usuario:

1. **Paso 1: Información Básica**
   - Fecha de elección
   - Gestión
   - Descripción

2. **Paso 2: Puestos Electorales**
   - Lista de puestos a crear
   - Botón "Agregar Puesto" que muestra un diálogo
   - Lista de puestos agregados con opción de eliminar

3. **Paso 3: Candidatos por Puesto**
   - Para cada puesto, mostrar lista de candidatos disponibles
   - Selección múltiple de candidatos
   - Vista previa de quién está postulado a qué puesto

4. **Paso 4: Revisión y Confirmación**
   - Resumen de toda la configuración
   - Botón "Crear Elección" que crea todo de una vez

**Ventajas:**
- Todo en un solo lugar
- Menos navegación
- Vista clara del proceso completo
- Menos errores

### Opción 2: Dashboard Mejorado con Acciones Rápidas

**Mejoras en la pantalla de Puestos:**
- Botón "Configuración Rápida" que permite:
  - Crear múltiples puestos a la vez
  - Agregar candidatos a múltiples puestos
  - Vista de estado general de la elección

### Opción 3: Flujo Híbrido (Más Flexible)

**Mantener el flujo actual pero agregar:**
- Botón "Configuración Rápida" en la pantalla de Elecciones
- Acciones rápidas desde cada pantalla:
  - Desde Puestos: "Agregar múltiples puestos"
  - Desde Detalle Puesto: "Agregar múltiples candidatos"
- Indicadores de progreso: "2/5 puestos configurados"

## Recomendación: Implementar Opción 1 + Mejoras Incrementales

### Fase 1: Arreglar Crashes (URGENTE)
- ✅ Ya corregido: Manejo de errores en inserción de postulaciones
- ✅ Ya corregido: Manejo de errores en registro de votos

### Fase 2: Mejoras Inmediatas de UX
1. **Indicadores de Progreso**
   - Mostrar en cada pantalla cuántos pasos faltan
   - Badges con estado: "Pendiente", "Completo"

2. **Acciones Rápidas**
   - Botón "Agregar Múltiples Candidatos" en DetallePuestoScreen
   - Selección múltiple en SeleccionarCandidatoScreen

3. **Navegación Mejorada**
   - Breadcrumbs para mostrar dónde estás
   - Botón "Volver a Elección" visible siempre

### Fase 3: Asistente Completo (Futuro)
- Implementar la Opción 1 completa

## Cambios Propuestos Inmediatos

1. **SeleccionarCandidatoScreen**: Permitir selección múltiple
2. **DetallePuestoScreen**: Agregar botón "Agregar Múltiples"
3. **PuestosElectoralesScreen**: Agregar indicador de progreso
4. **Navegación**: Agregar breadcrumbs o indicador de ruta

