import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps
import scala.io.Source

import scala.math.{pow, sqrt}

import WorldGenerator.getCitizenList


object KafkaProducerDrone {

  case class Report(id: Int, location: List[Double], citizens: List[String], score: List[Double], words: List[String])

  def getX(location: List[Double]): Double = location match {
    case Nil => 0
    case e :: tail => e
  }
  def getY(location: List[Double]): Double = location match {
    case Nil => 0
    case e :: Nil => e
    case e :: tail => getY(tail)
  }

  // Calculate the distance between two coordinates
  def calculateDistance(coord1: (Double, Double), coord2: (Double, Double)): Double = {
    val (x1, y1) = coord1
    val (x2, y2) = coord2
    sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2))
  }

  def sendReport(droneId : Int, location: List[Double], citizenList: List[(String, Double, Double, Double)]): Unit = {

    // Define the maximum distance
    val maxDistance = 10.0

    // Filter the citizenList based on the distance
    val filteredCitizens = citizenList.filter { case (_, x, y, _) =>
      calculateDistance((getX(location), getY(location)), (x, y)) <= maxDistance
    }


    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    // Create producer
    val producer = new KafkaProducer[String, String](props)
    
    // Topic name
    val topic = "drone-message"


    // Create Report and serialize
    val obj = Report(droneId, location, filteredCitizens.map(_._1) , filteredCitizens.map(_._4), List("love", "peace", "happy", "hate"))
    val json: Json = obj.asJson


    // Create Producer Record
    val record = new ProducerRecord[String, String](topic, droneId.toString, json.toString)

    // Send Record
    producer.send(record)

    // Close producer
    producer.close()
  }
}
