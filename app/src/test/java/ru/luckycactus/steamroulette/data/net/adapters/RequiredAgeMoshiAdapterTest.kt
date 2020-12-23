package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.luckycactus.steamroulette.data.net.RequiredAge

class RequiredAgeMoshiAdapterTest {

    private val adapter: JsonAdapter<Pojo> = Moshi.Builder()
        .add(RequiredAgeMoshiAdapter())
        .build()
        .adapter(Pojo::class.java)

    @Test
    fun `when age == 0 - should return null`() {
        val json = """{"required_age":0}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.requiredAge).isNull()
    }

    @Test
    fun `when age is an integer string - should return Int`() {
        val json = """{"required_age":"18"}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.requiredAge).isEqualTo(18)
    }

    @Test
    fun `when age is a non-integer string - should return null`() {
        val json = """{"required_age":"non-numeric"}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.requiredAge).isNull()
    }

    @Test
    fun `when age is null - should return null`() {
        val json = """{"required_age":null}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.requiredAge).isNull()
    }

    @JsonClass(generateAdapter = true)
    class Pojo(
        @RequiredAge @Json(name = "required_age") val requiredAge: Int?
    )
}