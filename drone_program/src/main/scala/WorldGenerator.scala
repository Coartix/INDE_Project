import scala.io.Source


object WorldGenerator {
    def getCitizenList(filePath : String): List[(String, Double, Double)] = {
        Source.fromFile(filePath)
            .getLines()
            .map { line =>
                val Array(name, x, y) = line.split(" ")
                (name, x.toDouble, y.toDouble)
            }.toList
    }
}