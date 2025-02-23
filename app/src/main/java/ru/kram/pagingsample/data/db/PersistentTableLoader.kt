package ru.kram.pagingsample.data.db

import android.content.Context
import com.github.javafaker.Faker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import ru.kram.pagingsample.core.CatDispatchers
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentDao
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentEntity
import timber.log.Timber
import java.util.UUID

class PersistentTableLoader(
    private val context: Context,
    private val faker: Faker,
    private val gson: Gson,
    private val catPersistentDao: CatPersistentDao,
    private val dispatchers: CatDispatchers,
) {

    suspend fun loadCats() = dispatchers.io {
        val fileNames = (0..20).map { "cat_page${it}.json" }

        fileNames.forEach { fileName ->
            try {
                val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val cats: List<CatJson> = gson.fromJson(
                    json,
                    (object : TypeToken<List<CatJson>>() {}).type
                )

                cats.mapIndexed { _, cat ->
                    CatPersistentEntity(
                        id = UUID.randomUUID().toString(),
                        name = faker.cat().name(),
                        breed = faker.cat().breed(),
                        age = faker.number().numberBetween(1, 20),
                        imageUrl = cat.url,
                    )
                }.apply {
                    catPersistentDao.insertAll(this)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    private data class CatJson(val url: String)
}