import scala.io.Source
import scala.util.Random


object WorldGenerator {
    def getCitizenList(filePath : String): List[(String, Double, Double, Double)] = {
        Source.fromFile(filePath)
            .getLines()
            .map { line =>
                val Array(name, x, y) = line.split(" ")
                (name, x.toDouble, y.toDouble, Random.nextDouble() * 25 + 75)
            }.toList
    }

}