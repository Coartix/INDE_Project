import scala.io.Source
import scala.util.Random


object WorldGenerator {

    def convertToLongitude(x: Double): Double = {
        val gridWidth = 100

        val longitudeRange = (-180.0, 180.0)

        ((x.toDouble / gridWidth) * (longitudeRange._2 - longitudeRange._1)) + longitudeRange._1
    }
    
    def convertToLatitude(y: Double): Double = {
        val gridHeight = 100

        val latitudeRange = (-90.0, 90.0)

        ((y.toDouble / gridHeight) * (latitudeRange._2 - latitudeRange._1)) + latitudeRange._1
    }

    def getCitizenList(filePath : String): List[(String, Double, Double, Int)] = {
        Source.fromFile(filePath)
            .getLines()
            .map { line =>
                val Array(name, _, _) = line.split(" ")
                (name, Random.nextInt(360).toDouble - 180 , Random.nextInt(180).toDouble - 90, Random.nextInt(20) + 40)
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