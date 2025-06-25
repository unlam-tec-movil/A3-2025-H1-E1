package ar.edu.unlam.scaffoldingandroid3

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ar.edu.unlam.scaffoldingandroid3.data.local.AppDatabase
import ar.edu.unlam.scaffoldingandroid3.data.local.dao.RouteDao
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.RouteEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var routeDao: RouteDao

    @Before
    fun setUp() {
        // Crea una base de datos en memoria para pruebas
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider
                    .getApplicationContext(),
                AppDatabase::class.java,
            ).allowMainThreadQueries() // Solo para testing
                .build()

        routeDao = database.routeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

//    @Test
//    fun databaseOpensAndCloses() {
//        // No hace nada, solo prueba la apertura y cierre de la base
//        assertNotNull(database)
//    }

    @Test
    fun insertAndGetRoute_savesAndReadsCorrectly() =
        runBlocking {
            // Arrange: Crea una ruta de prueba
            val routeId = "route1"
            val points =
                listOf(
                    Route.Point(1.0, 2.0, 1234567890),
                    Route.Point(3.0, 4.0, 1234567891),
                )
            val entity =
                RouteEntity(
                    id = routeId,
                    name = "Ruta de prueba",
                    points = points,
                    distance = 1000.0,
                    duration = 60000L,
                    photoUri = "photo",
                )

            // Act: Inserta y recupera la entidad
            routeDao.insert(entity)
            val loaded = routeDao.getRoute(routeId)

            // Assert: Los datos coinciden
            assertNotNull(loaded)
            assertEquals(entity.id, loaded?.id)
            assertEquals(entity.name, loaded?.name)
            assertEquals(entity.distance, loaded?.distance)
            assertEquals(entity.duration, loaded?.duration)
            // Verifica que los puntos se serializaron y deserializaron correctamente
            val loadedPoints: List<Route.Point> = loaded!!.points
            assertEquals(points, loadedPoints)
        }

    @Test
    fun insertAndDeleteRoute_deletesCorrectly() =
        runBlocking {
            val entity =
                RouteEntity(
                    id = "route2",
                    name = "Ruta Borrar",
                    points =
                        listOf(
                            Route.Point(1.0, 2.0, 1234567890),
                            Route.Point(3.0, 4.0, 1234567891),
                        ),
                    distance = 0.0,
                    duration = 0L,
                    photoUri = "photo",
                )
            routeDao.insert(entity)
            routeDao.deleteRoute("route2")
            val loaded = routeDao.getRoute("route2")
            assertNull(loaded)
        }

    @Test
    fun getAllRoutes_returnsAllInsertedRoutes() =
        runBlocking {
            val entity1 =
                RouteEntity(
                    id = "route3",
                    name = "Ruta 3",
                    points =
                        listOf(
                            Route.Point(1.0, 2.0, 1044567120),
                            Route.Point(3.0, 4.0, 7856934987),
                        ),
                    distance = 500.0,
                    duration = 1000L,
                    photoUri = "photo",
                )
            val entity2 =
                RouteEntity(
                    id = "route4",
                    name = "Ruta 4",
                    points =
                        listOf(
                            Route.Point(123.0, 2345.0, 84938561938),
                            Route.Point(3.0, 4.0, 1234567891),
                        ),
                    distance = 1200.0,
                    duration = 3000L,
                    photoUri = "photo",
                )
            routeDao.insert(entity1)
            routeDao.insert(entity2)
            val allRoutes = routeDao.getAllRoutes().first()
            assertTrue(allRoutes.any { it.id == "route3" })
            assertTrue(allRoutes.any { it.id == "route4" })
        }
}
