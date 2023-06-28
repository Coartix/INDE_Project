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

}