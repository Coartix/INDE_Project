import scala.io.Source
import scala.util.Random
import java.text.DecimalFormat

object WorldGenerator {
    def getCitizenList(filePath : String): List[(String, Double, Double, Double)] = {
        val decimalFormat = new DecimalFormat("#.##")
        Source.fromFile(filePath)
            .getLines()
            .map { line =>
                val Array(name, x, y) = line.split(" ")
                (name, x.toDouble, y.toDouble, decimalFormat.format(Random.nextDouble() * 25 + 75).toDouble)
            }.toList
    }

    def pickRandomWord(filePath: String): String = {
        val lines: List[String] = Source.fromFile(filePath).getLines().toList
        Random.shuffle(lines).headOption.getOrElse("")
    }

    def getCitizenWord(harmonyScore: Double): String = Random.nextInt(100) < harmonyScore match {
        case true => pickRandomWord("../data/good_words.txt")
        case false => pickRandomWord("../data/bad_words.txt")
    }

    def getCitizenWordList(harmonyScore: Double, n : Int): List[String] = n match {
        case 0 => Nil
        case e => getCitizenWord(harmonyScore) :: getCitizenWordList(harmonyScore, e - 1)
    }

    def getWordList(citizenList: List[(String, Double, Double, Double)]): List[String] = {
        citizenList.flatMap { case (_, _, _, harmonyScore) =>
            getCitizenWordList(harmonyScore, 3)
        }
    }
}