package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Test
import ru.luckycactus.steamroulette.data.repositories.games.details.models.RequiredAgeEntity

class RequiredAgeMoshiAdapterTest {

    private val adapter: JsonAdapter<POJO> =
        Moshi.Builder().add(RequiredAgeMoshiAdapter()).build().adapter(POJO::class.java)

    @Test
    fun `should return null if age == 0`() {
        val json = """{"required_age":0}"""
        val pojo = adapter.fromJson(json)
        assertNull(pojo!!.requiredAge)
    }

    @Test
    fun `should return Int if age is an integer string`() {
        val json = """{"required_age":"18"}"""
        val pojo = adapter.fromJson(json)
        assertEquals(pojo!!.requiredAge!!.age, 18)
    }

    @Test
    fun `should return null if age is a non-integer string`() {
        val json = """{"required_age":"non-numeric"}"""
        val pojo = adapter.fromJson(json)
        assertNull(pojo!!.requiredAge)
    }

    @Test
    fun `should return null if age is null`() {
        val json = """{"required_age":null}"""
        val pojo = adapter.fromJson(json)
        assertNull(pojo!!.requiredAge)
    }

    @JsonClass(generateAdapter = true)
    class POJO(
        @Json(name = "required_age") val requiredAge: RequiredAgeEntity?
    )
}