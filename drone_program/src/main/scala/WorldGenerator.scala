import scala.io.Source
import scala.util.Random


object WorldGenerator {

    def getCitizenList(filePath : String): List[(String, Double, Double, Int)] = {
        Source.fromFile(filePath)
            .getLines()
            .map { line =>
                val Array(name, x, y) = line.split(" ")
                (name, x.toDouble, y.toDouble, Random.nextInt(26) + 75)
            }.toList
    }

    def pickRandomWord(words: List[String]): String = {
        Random.shuffle(words).headOption.getOrElse("")
    }

    def getCitizenWord(harmonyScore: Int, goodWords: List[String], badWords: List[String]): String = Random.nextInt(120) < harmonyScore match {
        case true => pickRandomWord(goodWords)
        case false => pickRandomWord(badWords)
    }

    def getCitizenWordList(harmonyScore: Int, n : Int, goodWords: List[String], badWords: List[String]): List[String] = n match {
        case 0 => Nil
        case e => getCitizenWord(harmonyScore, goodWords, badWords) :: getCitizenWordList(harmonyScore, e - 1, goodWords, badWords)
    }

    def getWordList(citizenList: List[(String, Double, Double, Int)], goodWords: List[String], badWords: List[String]): List[String] = {
        citizenList.flatMap { case (_, _, _, harmonyScore) =>
            getCitizenWordList(harmonyScore, 3, goodWords, badWords)
        }
    }
}