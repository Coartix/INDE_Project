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
import WorldGenerator.getWordList

import java.time.{Instant, LocalDateTime, ZoneOffset, Duration}

import java.time.temporal.ChronoUnit


object KafkaProducerDrone {

  case class Report(id: Int, location: List[Double], citizens: List[String], score: List[Int], words: List[String], timestamp: Instant)

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

  def getTimestamp(originTimestamp: Instant, nbIter: Int): Instant = {
    originTimestamp.plus(nbIter, ChronoUnit.MINUTES)
  }

  def getMessage(droneId : Int, location: List[Double], citizenList: List[(String, Double, Double, Int)], originTimestamp: Instant, nbIter: Int, goodWords: List[String], badWords: List[String]): Json = {
    // Define the maximum distance
    
    val maxDistance = 3.0

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
    val obj = Report(droneId, location, filteredCitizens.map(_._1) , filteredCitizens.map(_._4), getWordList(filteredCitizens, goodWords, badWords), getTimestamp(originTimestamp, nbIter))
    // val json: Json = obj.asJson

    obj.asJson
  }
}
