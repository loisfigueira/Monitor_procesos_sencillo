# Monitor de procesos sencillo

## Índice
1. Introducción
2. Tecnologías y arquitectura
    1. Tecnologías
    2. Arquitectura
3. Funcionalidades
4. Manual de usuario
    1. Abrir desde un IDE
    2. Instalar en Windows
    3. Instalar en Linux/Mac
    4. Manual de uso
5. Pruebas
6. Conclusiones y dificultades encontradas

## 1. Introducción
Esta aplicación es un monitor de procesos que se encarga de listar procesos, mostrar resúmenes y finalizar procesos entre otras cosas. Esta está disponible para distintos sistemas operativos como Windows, Linux y Mac.

## 2. Tecnologías y arquitectura
### 2.1 Tecnologías
- **Kotlin**
- **IntelliJ IDEA**
- **Gradle**
- **OSHI**
- **Git**
- **GitHub**
- **JDK 17**
- **Compose Wizard**
- **Compose**
### 2.2 Arquitectura
La arquitectura del programa se basa en un MVVM dividiendo de forma clara la lógica, la interfaz y los modelos.

## 3. Funcionalidades
- Listar procesos en ejecución con PID, nombre y usuario. 
- Eliminar o matar procesos seleccionados. 
- Actualizar la lista de procesos. 
- Filtrar procesos por nombre o usuario. 
- Mostrar resúmenes visuales de CPU y RAM.

## 4. Manual de usuario
### 4.1 Abrir desde un IDE
1. Para hacer uso del proyecto desde un IDE como IntelliJ IDEA, se deberá clonar el repositorio en el enlace proporcionado en el anexo. Desde un terminal se haría “git clone enlace_al_repo”. 
2. Una vez clonado, si es necesario se configurará el archivo “gradle.properties” y en la línea “#org.gradle.java.home=C:\\Program Files\\Java\\jdk-17.0.4.1” se se descomenta y se pondrá la ruta al JDK, preferiblemente JDK 17 para compatibilidad. 
3. Una vez todo listo se ejecuta.
### 4.2 Instalar en Windows
1. Se accede al repositorio y en el apartado releases se descarga el .msi. 
2. Luego se sigue el proceso de instalación. 
3. Por último, se busca una carpeta con el nombre “es.lfigueira”, donde se encuentra la aplicación.
### 4.3 Instalar en Linux/Mac
1. Se accede al repositorio y en el apartado releases se descarga el .deb. 
2. Una vez hecho se localiza y se ejecuta: “sudo dpkg –i rutaAlDirectorio/ProcessMonitor_Package_Linux.deb”. 
3. Una vez hecho, la aplicación ya aparecerá entre las aplicaciones y en la tienda.
### 4.4 Manual de uso
1. Al abrir la aplicación se mostrará una interfaz con múltiples elementos. 
2. En la zona superior debajo del encabezado de la app serán visibles dos campos de búsqueda, una para filtrar los procesos por nombre y otro para filtrarlos por usuario. 
3. A la derecha de los filtros, estará el botón actualizar para actualizar la lista de procesos en ejecución. 
4. En la zona izquierda se mostrarán los resúmenes de CPU y RAM en uso por los procesos activos. 
5. En la parte derecha se mostrará la lista completa de procesos en ejecución con un encabezado con las distintas columnas de cada proceso PID, nombre, CPU... 
6. A la derecha de cada proceso aparecerá la opción de finalizar el proceso seleccionado, esto solo ocurrirá si el proceso no es crítico o si se tienen suficientes permisos para eliminarlo.

## 5. Pruebas
- Pruebas de filtrado
- Pruebas de finalización de procesos con/sin permisos
- Validación del uso en tiempo real de CPU y RAM
- Pruebas de generación de instaladores con GitHub Actions
- Pruebas del funcionamiento en Windows y Linux
- Pruebas de interfaz en distintas resoluciones

## 6.Conclusiones y dificultades encontradas
- **Aprendizajes adquiridos**
  - Conocimiento de la librería Oshi para mostrar información de procesos.
  - Uso de Compose para crear interfaces gráficas modernas con colores y estilos personalizados.
  - Identificación de los sistemas operativos en los que se usa la aplicación.
  - Comprensión básica del trabajo con procesos y su interacción con el sistema operativo.
- **Dificultades encontradas**
  - Problemas de compatibilidad entre Gradle y el JDK utilizado.
  - Resolución de errores de Gradle relacionados con dependencias o tareas.
  - Dificultad en el uso de Compose, debido a la falta de conocimiento previo.
  - Complejidad en la lógica del programa, por falta de práctica con procesos.
  - Tediosidad y fallos frecuentes al usar Gradle, especialmente al cambiar de dispositivo o JDK.
