package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.data.repositories.games.models.RequiredAgeEntity

class RequiredAgeMoshiAdapterTest {

    private val adapter: JsonAdapter<POJO> =
        Moshi.Builder().add(RequiredAgeMoshiAdapter()).build().adapter(POJO::class.java)

    @Test
    fun testRequiredAgeZero() {
        val json = """{"required_age":0}"""
        val pojo = adapter.fromJson(json)
        assertNull(pojo!!.requiredAge)
    }

    @Test
    fun testJsonRequiredAgeString() {
        val json = """{"required_age":"18"}"""
        val pojo = adapter.fromJson(json)
        assertEquals(pojo!!.requiredAge!!.age, 18)
    }

    @Test(expected = Exception::class)
    fun testJsonRequiredAgeNull() {
        val json = """{"required_age":null}"""
        adapter.fromJson(json)
    }

    @Test
    fun testJsonRequiredAgeMiss() {
        val json = """{}"""
        val pojo = adapter.fromJson(json)
        assertNull(pojo!!.requiredAge)
    }

    @JsonClass(generateAdapter = true)
    class POJO(
        @Json(name = "required_age") val requiredAge: RequiredAgeEntity?
    )
}