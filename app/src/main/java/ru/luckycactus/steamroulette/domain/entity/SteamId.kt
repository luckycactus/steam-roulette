package ru.luckycactus.steamroulette.domain.entity

import java.util.regex.Matcher
import java.util.regex.Pattern

class SteamId private constructor(
    private val value: Long
) {
    override fun toString(): String {
        return value.toString()
    }

    fun asSteam64(): Long = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SteamId

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


    companion object {

        fun getFormat(input: String): Format {
            Format.values().forEach {
                if (it.pattern.matcher(input).matches())
                    return it
            }
            return Format.Invalid
        }

        fun getVanityUrlFormat(input: String): VanityUrlFormat {
            VanityUrlFormat.values().forEach {
                if (it.pattern.matcher(input).matches())
                    return it
            }
            return VanityUrlFormat.Invalid
        }

        fun tryGetVanityUrl(input: String): String? {
            if (VanityUrlFormat.Short.pattern.matcher(input).matches())
                return input

            VanityUrlFormat.Full.pattern.matcher(input).apply {
                if (matches()) {
                    find()
                    return group(2)
                }
            }

            return null
        }

        fun tryParse(input: String): SteamId? {
            val format = getFormat(input)

            if (format == Format.Invalid)
                return null

            return try {
                format.parseSteamId(input)
            } catch (e: Exception) {
                null
            }

        }

        fun fromSteam64(steam64: Long): SteamId =
            SteamId(steam64)

        fun parse(input: String): SteamId =
            tryParse(input) ?: throw IllegalArgumentException("Cannot parse string: \"$input\" as SteamId")

        fun parse(input: String, format: Format): SteamId {
            return format.parseSteamId(input)
        }

        private fun from(
            universe: Universe,
            type: AccountType,
            instance: Instance,
            accountId: Long
        ): SteamId {
            var value = accountId
            value = value or (instance.value.toLong() shl 32)
            value = value or (type.value.toLong() shl 52)
            value = value or (universe.value.toLong() shl 56)
            return fromSteam64(value)
        }

    }

    enum class Universe(
        val value: Int
    ) {
        Individual(0),
        Public(1),
        Beta(2),
        Internal(3),
        Dev(4),
        RC(5)
    }

    enum class AccountType(
        val value: Int,
        val letter: Char
    ) {
        Invalid(0, 'I'),
        Individual(1, 'U'),
        Multiseat(2, 'M'),
        GameServer(3, 'G'),
        AnonGameServer(4, 'A'),
        Pending(5, 'P'),
        ContentServer(6, 'C'),
        Clan(7, 'g'),
        Chat(8, 'T'),
        AnonUser(10, 'a'),
    }

    enum class Instance(
        val value: Int
    ) {
        Desktop(1),
        Console(2),
        Web(4)
    }

    /**
     * @see <a href="https://developer.valvesoftware.com/wiki/SteamID#Format">More about SteamId</a>
     */
    enum class Format(
        regExp: String
    ) {
        /**
         * Example: 76561197960287930
         */
        Steam64("\\d{17}") {
            override fun parseSteamId(input: String): SteamId {
                return fromSteam64(input.toLong())
            }
        },

        /**
         * Example: STEAM_0:0:11101
         */
        Steam2("(?i)(STEAM)_([0-1]):([0-1]):(\\d+)") {
            private val v = 0x0110000100000000

            override fun parseSteamId(input: String): SteamId {
                /*
                Let V be SteamID64 identifier of the account type (0x0110000100000000 for individual).
                Using the formula W=Z*2+V+Y, a SteamID can be converted to the following link:
                http or https://steamcommunity.com/path/W
                As for the 32-bit method, the path can be found in the table above, again after the slash.
                Example: http://steamcommunity.com/profiles/76561197960287930
                 */
                return matcherFind(input) {
                    val y = it.group(3).toLong()
                    val z = it.group(4).toLong()
                    fromSteam64(z * 2 + v + y)
                }
            }
        },

        /**
         * Example: [U:1:22202]
         */
        Steam3("\\[U:1:(\\d+)(:1)?\\]") {
            override fun parseSteamId(input: String): SteamId {
                val accountId = matcherFind(input) {
                    it.group(1).toLong()
                }
                return from(
                    Universe.Public,
                    AccountType.Individual,
                    Instance.Desktop,
                    accountId
                )
            }
        },

        /**
         * Example: https://steamcommunity.com/profiles/76561197960287930/
         */
        Steam64Url("(https?://)?steamcommunity\\.com/profiles/(${Steam64.pattern.pattern()})/?") {
            override fun parseSteamId(input: String): SteamId {
                return matcherFind(input) {
                    Steam64.parseSteamId(it.group(2))
                }
            }
        },

        /**
         * Example: https://steamcommunity.com/profiles/[U:1:22202]/
         */
        Steam3Url("(https?://)?steamcommunity\\.com/profiles/(${Steam3.pattern.pattern()})/?") {
            override fun parseSteamId(input: String): SteamId {
                return matcherFind(input) {
                    Steam3.parseSteamId(it.group())
                }
            }
        },

        Invalid("\$a") {
            override fun parseSteamId(input: String): SteamId {
                throw IllegalStateException("${Format::class.java.simpleName}.${Invalid.name} cannot parse anything!")
            }
        };

        var pattern: Pattern = Pattern.compile(regExp)


        fun matches(input: String) = pattern.matcher(input).matches()

        abstract fun parseSteamId(input: String): SteamId

        protected fun <T> matcherFind(input: String, block: (Matcher) -> T): T {
            val matcher = pattern.matcher(input)
            matcher.find()
            return block(matcher)
        }
    }

    enum class VanityUrlFormat(
        regExp: String
    ) {
        /**
         * Example: gabelogannewell
         */
        Short("[\\d\\w_-]{3,32}"),

        /**
         * Example: https://steamcommunity.com/id/gabelogannewell/
         */
        Full("(https?://)?steamcommunity\\.com/id/([\\d\\w_-]{3,32})/?"),

        Invalid("\$a");

        val pattern: Pattern = Pattern.compile(regExp)
    }
}