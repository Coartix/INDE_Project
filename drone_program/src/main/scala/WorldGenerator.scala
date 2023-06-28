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

    def pickRandomWord(filePath: String): String = {
        val lines: List[String] = Source.fromFile(filePath).getLines().toList
        Random.shuffle(lines).headOption.getOrElse("")
    }

    def getCitizenWord(harmonyScore: Int): String = Random.nextInt(100) < harmonyScore match {
        case true => pickRandomWord("../data/good_words.txt")
        case false => pickRandomWord("../data/bad_words.txt")
    }

    def getCitizenWordList(harmonyScore: Int, n : Int): List[String] = n match {
        case 0 => Nil
        case e => getCitizenWord(harmonyScore) :: getCitizenWordList(harmonyScore, e - 1)
    }

    def getWordList(citizenList: List[(String, Double, Double, Int)]): List[String] = {
        citizenList.flatMap { case (_, _, _, harmonyScore) =>
            getCitizenWordList(harmonyScore, 3)
        }
    }
}