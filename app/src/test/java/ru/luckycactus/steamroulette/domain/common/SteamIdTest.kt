package ru.luckycactus.steamroulette.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.luckycactus.steamroulette.domain.common.SteamId.Format.*
import ru.luckycactus.steamroulette.domain.common.SteamId.VanityUrlFormat.Full

class SteamIdTest {

    @Test
    fun testSteam64Pattern() {
        with(Steam64.regex) {
            assertThat(matches("76561197960287930")).isTrue()
            assertThat(!matches("765611979602879301")).isTrue()
            assertThat(!matches("76561197960287930a")).isTrue()
            assertThat(!matches(" 76561197960287930")).isTrue()
            assertThat(!matches("7656119796028793")).isTrue()
        }
    }

    @Test
    fun testVanityPattern() {
        with(SteamId.VanityUrlFormat.Short.regex) {
            assertThat(matches("luckycactus")).isTrue()
            assertThat(matches("-lucky1cactus_")).isTrue()
            assertThat(matches("123")).isTrue()
            assertThat(matches("12345678901234567890123456789012")).isTrue()
            assertThat(!matches("12")).isTrue()
            assertThat(!matches("123456789012345678901234567890123")).isTrue()
            assertThat(!matches("^^%$*#@()#&")).isTrue()
        }
    }

    @Test
    fun testSteam2Pattern() {
        with(Steam2) {
            assertThat(matches("STEAM_0:0:11101")).isTrue()
            assertThat(matches("sTeAm_1:0:1")).isTrue()
            assertThat(!matches("STEAM_5:1:111011")).isTrue()
            assertThat(!matches("STEAM_6:1:111011")).isTrue()
            assertThat(!matches("STEAM_2:2:111011")).isTrue()
            assertThat(!matches("1STEAM_5:1:111011")).isTrue()
        }
    }

    @Test
    fun testSteam2Parsing() {
        with(Steam2) {
            assertThat(parseSteamId("STEAM_0:1:41833838").as64() == 76561198043933405).isTrue()
            assertThat(parseSteamId("STEAM_0:0:11101").as64() == 76561197960287930).isTrue()
        }
    }

    @Test
    fun testSteam3Parsing() {
        with(Steam3) {
            assertThat(parseSteamId("[U:1:83667677]").as64() == 76561198043933405).isTrue()
            assertThat(parseSteamId("[U:1:22202]").as64() == 76561197960287930).isTrue()
        }
    }

    @Test
    fun testSteam3Pattern() {
        with(Steam3.regex) {
            assertThat(matches("[U:1:22202]")).isTrue()
            assertThat(matches("[U:1:22202:1]")).isTrue()
            assertThat(!matches("[U:1:22202:11]")).isTrue()
            assertThat(!matches("[U:1:22202d]")).isTrue()
            assertThat(!matches("[U:0:22202]")).isTrue()
            assertThat(!matches("U:1:22202")).isTrue()
        }
    }

    @Test
    fun testVanityFullUrl() {
        with(Full.regex) {
            assertThat(matches("https://steamcommunity.com/id/gabelogannewell/")).isTrue()
            assertThat(matches("http://steamcommunity.com/id/gabelogannewell")).isTrue()
            assertThat(matches("steamcommunity.com/id/gabelogannewell")).isTrue()
            assertThat(!matches("htt://steamcommunity.com/id/gabelogannewell")).isTrue()
            assertThat(!matches("https://steamcommunity.com/id/gabelogannewell//")).isTrue()
        }
    }

    @Test
    fun testVanityFullUrlGroups() {
        with(Full.regex) {
            val matchResult = find("https://steamcommunity.com/id/gabelogannewell/")
            assertThat(matchResult?.groupValues?.get(2) == "gabelogannewell").isTrue()
        }
    }

    @Test
    fun testSteam64Url() {
        with(Steam64Url.regex) {
            assertThat(matches("https://steamcommunity.com/profiles/76561197960287930/")).isTrue()
            assertThat(matches("http://steamcommunity.com/profiles/76561197960287930")).isTrue()
            assertThat(matches("steamcommunity.com/profiles/76561197960287930")).isTrue()
            assertThat(!matches("htt://steamcommunity.com/profiles/76561197960287930")).isTrue()
            assertThat(!matches("https://steamcommunity.com/profiles/76561197960287930//")).isTrue()
        }
    }

    @Test
    fun testSteam3Url() {
        with(Steam3Url.regex) {
            assertThat(matches("https://steamcommunity.com/profiles/[U:1:22202]/")).isTrue()
            assertThat(matches("https://steamcommunity.com/profiles/[U:1:22202:1]/")).isTrue()
            assertThat(matches("http://steamcommunity.com/profiles/[U:1:22202]")).isTrue()
            assertThat(matches("steamcommunity.com/profiles/[U:1:22202]")).isTrue()
            assertThat(!matches("htt://steamcommunity.com/profiles/[U:1:22202]")).isTrue()
            assertThat(!matches("https://steamcommunity.com/profiles/[U:1:22202]//")).isTrue()
        }
    }
}