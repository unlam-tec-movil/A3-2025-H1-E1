package ar.edu.unlam.scaffoldingandroid3

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase base de la aplicación que extiende de Application.
 *
 * Esta clase es fundamental porque:
 * 1. @HiltAndroidApp: Marca esta clase como el punto de entrada para la inyección de dependencias.
 *    Hilt generará el código necesario para el contenedor de dependencias aquí.
 *
 * 2. Ciclo de Vida: Al extender de Application, esta clase se inicializa antes que cualquier
 *    otro componente de la app, permitiendo configuraciones globales.
 *
 * 3. Punto de Entrada: Es el primer componente que se ejecuta al iniciar la aplicación,
 *    ideal para inicializar bibliotecas y configuraciones globales.
 *
 * 4. Configuración Global: Aquí se pueden inicializar componentes como:
 *    - Firebase
 *    - Bases de datos
 *    - Loggers
 *    - Clientes HTTP
 *    - Otras configuraciones globales
 */
@HiltAndroidApp
class RoutesApplication : Application()
