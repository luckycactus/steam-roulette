package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Test
import ru.luckycactus.steamroulette.data.repositories.games.details.models.SystemRequirementsEntity


class SystemRequirementsMoshiAdapterTest {

    private val adapter = Moshi.Builder()
        .add(SystemRequirementsMoshiAdapter())
        .build()
        .adapter(Pojo::class.java)

    @Test
    fun testNormal() {
        val json = """{"pc_requirements":{"minimum":"minimum","recommended":"recommended"}}"""
        val pojo = adapter.fromJson(json)!!
        val reqs = pojo.reqs!!
        assertEquals(reqs.minimum, "minimum")
        assertEquals(reqs.recommended, "recommended")
    }

    @Test
    fun testNull() {
        val json = """{"pc_requirements":null}"""
        val pojo = adapter.fromJson(json)!!
        assertNull(pojo.reqs)
    }

    @Test
    fun testEmptyArray() {
        val json = """{"pc_requirements":[]}"""
        val pojo = adapter.fromJson(json)!!
        assertNull(pojo.reqs)
    }

    @JsonClass(generateAdapter = true)
    class Pojo(
        @Json(name = "pc_requirements") val reqs: SystemRequirementsEntity?
    )
}