# ExamSystem Manual de usuario

## Requisitos del sistema
- Java 8 o superior
- Entorno gráfico compatible con OpenGL (Linux: instalar mesa-utils)

## Cómo iniciar
./start-exam.sh
o
java -jar ExamSystem.jar

## Características
- Seleccionar plan de aprendizaje (8 módulos sistemáticos)
- Tipos de preguntas: programación, opción múltiple, rellenar, emparejar, depuración, completar
- Guardado automático del progreso
- Sistema de logros y estrellas
- Interfaz multilingüe (7 idiomas intercambiables)
- Editor de código integrado (resaltado de sintaxis, autocompletado)

## Primer uso
1. Aparece la pantalla de bienvenida
2. Haga clic en "Importar plan" para cargar los planes oficiales (o saltar si ya existen)
3. Elija la dificultad (Principiante/Intermedio/Avanzado)
4. Comience a responder

## Atajos de teclado
- Ctrl+S: Guardar progreso
- Ctrl+R: Ejecutar verificación de código
- Ctrl+←/→: Pregunta anterior/siguiente
- Esc: Volver a la selección de sección

## Archivos de configuración
El progreso se guarda en .exam_progress.json, .exam_unlocks.json, .exam_achievements.json.

## Solución de problemas
- Si falta una biblioteca nativa, asegúrese de que los controladores OpenGL estén instalados.
- Si no se inicia, verifique la versión de Java: java -version
