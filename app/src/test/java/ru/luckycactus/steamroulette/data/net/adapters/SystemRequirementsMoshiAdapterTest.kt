package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.luckycactus.steamroulette.data.repositories.games.details.models.SystemRequirementsEntity


class SystemRequirementsMoshiAdapterTest {

    private val adapter = Moshi.Builder()
        .add(SystemRequirementsMoshiAdapter())
        .build()
        .adapter(Pojo::class.java)

    @Test
    fun `when json contains object with system requirements - should return parsed object`() {
        val json = """{"pc_requirements":{"minimum":"minimum","recommended":"recommended"}}"""
        val pojo = adapter.fromJson(json)!!
        val reqs = pojo.reqs!!
        assertThat(reqs.minimum).isEqualTo("minimum")
        assertThat(reqs.recommended).isEqualTo("recommended")
    }

    @Test
    fun `when json field is null - should return null`() {
        val json = """{"pc_requirements":null}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.reqs).isNull()
    }

    @Test
    fun `when json field is array - should return null`() {
        val json = """{"pc_requirements":[]}"""
        val pojo = adapter.fromJson(json)!!
        assertThat(pojo.reqs).isNull()
    }

    @JsonClass(generateAdapter = true)
    class Pojo(
        @Json(name = "pc_requirements") val reqs: SystemRequirementsEntity?
    )
}