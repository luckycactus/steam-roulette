package ru.luckycactus.steamroulette.domain.common

import org.junit.Test
import ru.luckycactus.steamroulette.domain.common.SteamId.Format.*
import ru.luckycactus.steamroulette.domain.common.SteamId.VanityUrlFormat.Full

class SteamIdTest {

    @Test
    fun testSteam64Pattern() {
        with(Steam64.regex) {
            assert(matches("76561197960287930"))
            assert(!matches("765611979602879301"))
            assert(!matches("76561197960287930a"))
            assert(!matches(" 76561197960287930"))
            assert(!matches("7656119796028793"))
        }
    }

    @Test
    fun testVanityPattern() {
        with(SteamId.VanityUrlFormat.Short.regex) {
            assert(matches("luckycactus"))
            assert(matches("-lucky1cactus_"))
            assert(matches("123"))
            assert(matches("12345678901234567890123456789012"))
            assert(!matches("12"))
            assert(!matches("123456789012345678901234567890123"))
            assert(!matches("^^%$*#@()#&"))
        }
    }

    @Test
    fun testSteam2Pattern() {
        with(Steam2) {
            assert(matches("STEAM_0:0:11101"))
            assert(matches("sTeAm_1:0:1"))
            assert(!matches("STEAM_5:1:111011"))
            assert(!matches("STEAM_6:1:111011"))
            assert(!matches("STEAM_2:2:111011"))
            assert(!matches("1STEAM_5:1:111011"))
        }
    }

    @Test
    fun testSteam2Parsing() {
        with(Steam2) {
            assert(parseSteamId("STEAM_0:1:41833838").as64() == 76561198043933405)
            assert(parseSteamId("STEAM_0:0:11101").as64() == 76561197960287930)
        }
    }

    @Test
    fun testSteam3Parsing() {
        with(Steam3) {
            assert(parseSteamId("[U:1:83667677]").as64() == 76561198043933405)
            assert(parseSteamId("[U:1:22202]").as64() == 76561197960287930)
        }
    }

    @Test
    fun testSteam3Pattern() {
        with(Steam3.regex) {
            assert(matches("[U:1:22202]"))
            assert(matches("[U:1:22202:1]"))
            assert(!matches("[U:1:22202:11]"))
            assert(!matches("[U:1:22202d]"))
            assert(!matches("[U:0:22202]"))
            assert(!matches("U:1:22202"))
        }
    }

    @Test
    fun testVanityFullUrl() {
        with(Full.regex) {
            assert(matches("https://steamcommunity.com/id/gabelogannewell/"))
            assert(matches("http://steamcommunity.com/id/gabelogannewell"))
            assert(matches("steamcommunity.com/id/gabelogannewell"))
            assert(!matches("htt://steamcommunity.com/id/gabelogannewell"))
            assert(!matches("https://steamcommunity.com/id/gabelogannewell//"))
        }
    }

    @Test
    fun testVanityFullUrlGroups() {
        with(Full.regex) {
            val matchResult = find("https://steamcommunity.com/id/gabelogannewell/")
            assert(matchResult?.groupValues?.get(2) == "gabelogannewell")
        }
    }

    @Test
    fun testSteam64Url() {
        with(Steam64Url.regex) {
            assert(matches("https://steamcommunity.com/profiles/76561197960287930/"))
            assert(matches("http://steamcommunity.com/profiles/76561197960287930"))
            assert(matches("steamcommunity.com/profiles/76561197960287930"))
            assert(!matches("htt://steamcommunity.com/profiles/76561197960287930"))
            assert(!matches("https://steamcommunity.com/profiles/76561197960287930//"))
        }
    }

    @Test
    fun testSteam3Url() {
        with(Steam3Url.regex) {
            assert(matches("https://steamcommunity.com/profiles/[U:1:22202]/"))
            assert(matches("https://steamcommunity.com/profiles/[U:1:22202:1]/"))
            assert(matches("http://steamcommunity.com/profiles/[U:1:22202]"))
            assert(matches("steamcommunity.com/profiles/[U:1:22202]"))
            assert(!matches("htt://steamcommunity.com/profiles/[U:1:22202]"))
            assert(!matches("https://steamcommunity.com/profiles/[U:1:22202]//"))
        }
    }
}