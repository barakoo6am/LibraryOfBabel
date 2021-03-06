import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and


class LibraryOfBabel(
    val lengthOfPage: Int = 3200,
    val lengthOfTitle: Int = 31,
    val digs: String = "abcdefghijklmnopqrstuvwxyz012",
    val alphabet: String = "abcdefghijklmnopqrstuvwxyz, .",
    val wall: Int = 4,
    val shelf: Int = 5,
    val volume: Int = 32,
    val page: Int = 410
) {
    private var seed = 13L
    private val alphabetIndexes: MutableMap<Char, Int> = mutableMapOf()
    private val digsIndexes: MutableMap<Char, Int> = mutableMapOf()

    init {
        if(alphabet.length != digs.length){
            throw Exception("Alphabet and Digs have to have the same length.")
        }

        alphabet.forEachIndexed { index, c -> alphabetIndexes[c] = index }
        digs.forEachIndexed { index, c -> digsIndexes[c] = index }
    }


    fun search(searchText: String): String {
        val random = Random()
        val wall = (random.nextInt(wall) + 1).toString()
        val shelf = (random.nextInt(shelf) + 1).toString()
        val volume = (random.nextInt(volume) + 1).toString().padStart(2, '0')
        val page = (random.nextInt(page) + 1).toString().padStart(3, '0')
        var depth = 0

        if(lengthOfPage - searchText.length > 0) depth = random.nextInt(lengthOfPage - searchText.length)

        val locHash = getHash("$wall$shelf$volume$page")
        var hex = ""

        var padding = ""
        for (x in 0 until depth) {
            padding += alphabet[random.nextInt(alphabet.length)]
        }

        val searchStr = padding + searchText

        seed = generateSeed(locHash)

        for (i in 0 until searchStr.length) {
            val index = alphabetIndexes.getOrDefault(searchStr[i], -1)
            val rand = getRand(alphabet.length)
            val newIndex = mod(index + rand, digs.length.toLong())
            val newChar = digs[newIndex.toInt()]
            hex += newChar
        }

        return "$hex:$wall:$shelf:$volume:$page"
    }

    fun searchExactly(text: String) : String {
        val random = Random()
        val depth = random.nextInt(lengthOfPage - text.length)
        val result = text.padStart(text.length+depth, ' ').padEnd(lengthOfPage, ' ')

        return search(result)
    }

    fun getPage(address: String): String {
        val addressArray = address.split(":")

        val hex = addressArray[0]

        val wall = addressArray[1]
        val shelf = addressArray[2]
        val volume = addressArray[3].padStart(2, '0')
        val page = addressArray[4].padStart(3, '0')

        val locHash = getHash("$wall$shelf$volume$page")

        seed = generateSeed(locHash)
        var result = ""
        for (i in 0 until hex.length) {
            val index = digsIndexes[hex[i]]
            val rand = getRand()
            val newIndex = mod(index!! - rand, digs.length.toLong())
            val newChar = alphabet[newIndex.toInt()]
            result += newChar
        }

        while (result.length < this.lengthOfPage) {
            result += alphabet[getRand(alphabet.length).toInt()]
        }

        return result
    }

    fun searchTitle(text: String) : String{
        val random = Random()
        val wall = (random.nextInt(wall) + 1).toString()
        val shelf = (random.nextInt(shelf) + 1).toString()
        val volume = (random.nextInt(volume) + 1).toString().padStart(2, '0')

        val locHash = getHash("$wall$shelf$volume")
        var hex = ""
        var searchText = text
        if(searchText.length > lengthOfTitle)
            searchText = searchText.substring(0, lengthOfTitle)

        seed = generateSeed(locHash)
        for (i in 0 until searchText.length) {
            val index = alphabetIndexes.getOrDefault(searchText[i], -1)
            val rand = getRand(alphabet.length)
            val newIndex = mod(index + rand, digs.length.toLong())
            val newChar = digs[newIndex.toInt()]
            hex += newChar
        }

        return "$hex:$wall:$shelf:$volume"
    }

    fun getTitle(address: String) : String {
        val addressArray = address.split(":")

        val hex = addressArray[0]

        val wall = addressArray[1]
        val shelf = addressArray[2]
        val volume = addressArray[3].padStart(2, '0')

        val locHash = getHash("$wall$shelf$volume")

        seed = generateSeed(locHash)

        var result = ""
        for (i in 0 until hex.length) {
            val index = digsIndexes[hex[i]]
            val rand = getRand()
            val newIndex = mod(index!! - rand, digs.length.toLong())
            val newChar = alphabet[newIndex.toInt()]
            result += newChar
        }

        while (result.length < this.lengthOfTitle) {
            result += alphabet[getRand(alphabet.length).toInt()]
        }

        return result.substring(result.length - lengthOfTitle)
    }

    private fun generateSeed(str: String): Long {
        var summa = 0L
        for (char in str) {
            summa += char.toInt()
        }

        return summa
    }

    fun getHash(string: String): String {

        val md = MessageDigest.getInstance("SHA-512")

        val bytes = md.digest(string.toByteArray(StandardCharsets.UTF_8))
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(Integer.toString((bytes[i] and 0xff.toByte()) + 0x100, 16).substring(1))
        }

        return sb.toString().slice(0..7)
    }

    fun mod(a: Long, b: Long) = ((a % b) + b) % b

    fun getRand(max: Int = Int.MAX_VALUE): Long {
        val a = 523600286213591
        val c = 121353125126
        val m = 1325125812
        seed = (a * seed + c) % m

        var result = seed % max

        if(result < 0)
            result *= -1

        return result
    }

}