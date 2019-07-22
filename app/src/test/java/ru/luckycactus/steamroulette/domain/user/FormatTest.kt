package ru.luckycactus.steamroulette.domain.user

import org.junit.Test
import ru.luckycactus.steamroulette.domain.user.SteamId.Format.*
import ru.luckycactus.steamroulette.domain.user.SteamId.VanityUrlFormat.Full

class FormatTest {

    @Test
    fun testSteam64Pattern() {
        with(Steam64.pattern) {
            assert(matcher("76561197960287930").matches())
            assert(!matcher("765611979602879301").matches())
            assert(!matcher("76561197960287930a").matches())
            assert(matcher(" 76561197960287930").matches())
            assert(matcher("7656119796028793").matches())
        }
    }

    @Test
    fun testVanityPattern() {
        with(SteamId.VanityUrlFormat.Short.pattern) {
            assert(matcher("luckycactus").matches())
            assert(matcher("-lucky1cactus_").matches())
            assert(matcher("123").matches())
            assert(matcher("12345678901234567890123456789012").matches())
            assert(!matcher("12").matches())
            assert(!matcher("123456789012345678901234567890123").matches())
            assert(!matcher("^^%$*#@()#&").matches())
        }
    }

    @Test
    fun testSteam2Pattern() {
        with(Steam2) {
            assert(matches("STEAM_0:0:11101"))
            assert(matches("sTeAm_1:0:1"))
            assert(matches("STEAM_5:1:111011"))
            assert(!matches("STEAM_6:1:111011"))
            assert(!matches("STEAM_2:2:111011"))
            assert(!matches("1STEAM_5:1:111011"))
        }
    }

    @Test
    fun testSteam2Parsing() {
        with(Steam2) {
            assert(parseSteamId("STEAM_0:1:41833838").asSteam64() == 76561198043933405)
            assert(parseSteamId("STEAM_0:0:11101").asSteam64() == 76561197960287930)
        }
    }

    @Test
    fun testSteam3Parsing() {
        with(Steam3) {
            assert(parseSteamId("[U:1:83667677]").asSteam64() == 76561198043933405)
            assert(parseSteamId("[U:1:22202]").asSteam64() == 76561197960287930)
        }
    }

    @Test
    fun testSteam3Pattern() {
        with(Steam3.pattern) {
            assert(matcher("[U:1:22202]").matches())
            assert(matcher("[U:1:22202:1]").matches())
            assert(!matcher("[U:1:22202:11]").matches())
            assert(!matcher("[U:1:22202d]").matches())
            assert(!matcher("[U:0:22202]").matches())
            assert(!matcher("U:1:22202").matches())
        }
    }

    @Test
    fun testVanityFullUrl() {
        with(Full.pattern) {
            assert(matcher("https://steamcommunity.com/id/gabelogannewell/").matches())
            assert(matcher("http://steamcommunity.com/id/gabelogannewell").matches())
            assert(matcher("steamcommunity.com/id/gabelogannewell").matches())
            assert(!matcher("htt://steamcommunity.com/id/gabelogannewell").matches())
            assert(!matcher("https://steamcommunity.com/id/gabelogannewell//").matches())
        }
    }

    @Test
    fun testVanityFullUrlGroups() {
        with(Full.pattern) {
            val matcher = matcher("https://steamcommunity.com/id/gabelogannewell/")
            matcher.find()
            assert(matcher.group(2) == "gabelogannewell")
        }
    }

    @Test
    fun testSteam64Url() {
        with(Steam64Url.pattern) {
            assert(matcher("https://steamcommunity.com/profiles/76561197960287930/").matches())
            assert(matcher("http://steamcommunity.com/profiles/76561197960287930").matches())
            assert(matcher("steamcommunity.com/profiles/76561197960287930").matches())
            assert(!matcher("htt://steamcommunity.com/profiles/76561197960287930").matches())
            assert(!matcher("https://steamcommunity.com/profiles/76561197960287930//").matches())
        }
    }

    @Test
    fun testSteam3Url() {
        with(Steam3Url.pattern) {
            assert(matcher("https://steamcommunity.com/profiles/[U:1:22202]/").matches())
            assert(matcher("https://steamcommunity.com/profiles/[U:1:22202:1]/").matches())
            assert(matcher("http://steamcommunity.com/profiles/[U:1:22202]").matches())
            assert(matcher("steamcommunity.com/profiles/[U:1:22202]").matches())
            assert(!matcher("htt://steamcommunity.com/profiles/[U:1:22202]").matches())
            assert(!matcher("https://steamcommunity.com/profiles/[U:1:22202]//").matches())
        }
    }
}