import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps
import io.circe.parser.decode


import scala.util.Random

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

import java.util.concurrent.{ScheduledExecutorService, TimeUnit, Executors}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.time.Instant

import KafkaProducerDrone.getMessage
import KafkaProducerDrone.Report
import WorldGenerator.getCitizenList

import scala.io.Source


object DroneGenerator {

    case class Drone(id: Int, location: List[Double])

    def moveDrone(location : List[Double]): List[Double] = location match {
        case Nil => Nil
        case e::tail => e + Random.nextInt(7) - 3 :: moveDrone(tail)
    }

    def generateDrone(n : Int): List[Drone] = n match {
        case 0 => Nil
        case n => Drone(n, List(Random.nextInt(100), Random.nextInt(100))) :: generateDrone(n - 1)
    }

    def sendReport(droneId: String, message: Json, producer: KafkaProducer[String, String]): Unit = {
        // Create Producer Record
        val record = new ProducerRecord[String, String]("drone-message", droneId, message.toString)

        // Send Record
        producer.send(record)
    }

    def jsonToReport(message: Json): Report = {
        val report: Either[Error, Report] = decode[Report](message.toString)

        report match {
            case Right(result) => result// Successfully deserialized
            case _ => Report(1, List(0.0, 0.0), List("ERROR", "ERROR", "ERROR"), List(0), List("ERROR"), Instant.now())
        }
        
    }

    def changeValue(words: List[String], goodWords: List[String]): Int = {
        val res = words.foldLeft(0) { (acc, element) =>
            if (goodWords.contains(element)) {
                acc + 2
            }
            else {
                acc - 3
            }
        }
        if (res > -3 && res < 3) {
            0
        }
        else if (res > 3) {
            1
        }
        else {
            -1
        }
    }

    def updateCitizenList(message: Report, citizenList: List[(String, Double, Double, Int)], goodWords: List[String]): List[(String, Double, Double, Int)] = {

        citizenList.map { case (name, x, y, harmonyScore) =>
            if (message.citizens.contains(name)) {
                val change = changeValue(message.words, goodWords)
                if (harmonyScore + change > 100) {
                    (name, x, y, 100)
                }
                else if (harmonyScore + change < 0) {
                    (name, x, y, 0)
                }
                else {
                    (name, x, y, harmonyScore + change)
                }
                
            }
            else {
                (name, x, y, harmonyScore)}
            }
    }

    def simulateIteration(generation: Int, counter: Int, drones: List[Drone], originTimestamp: Instant, citizenList: List[(String, Double, Double, Int)], producer: KafkaProducer[String, String], goodWords: List[String], badWords: List[String]): Unit = counter match {
        case counter if (counter == generation) => {
            val messages = drones.map(drone => (drone.id.toString, getMessage(drone.id, moveDrone(drone.location), citizenList, originTimestamp, counter, goodWords, badWords)))
            messages.foreach { case (droneId, message) => sendReport(droneId, message, producer) } 
        }
        case counter => {
            val messages = drones.map(drone => (drone.id.toString, getMessage(drone.id, moveDrone(drone.location), citizenList, originTimestamp, counter, goodWords, badWords)))
            messages.foreach { case (droneId, message) => sendReport(droneId, message, producer) } 

            val reports = messages.map { case (droneId, message) => jsonToReport(message) }

            val newCitizenList = reports.foldLeft(citizenList)((acc, num) => updateCitizenList(num, acc, goodWords))

            simulateIteration(generation, counter + 1, drones, originTimestamp, newCitizenList, producer, goodWords, badWords)
        }
    }

    def main(args: Array[String]) : Unit = {

        // val goodWords: List[String] = Source.fromFile("../data/good_words.txt").getLines().toList

        // val badWords: List[String] = Source.fromFile("../data/bad_words.txt").getLines().toList


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

        val drones = generateDrone(2)
        
        val originTimestamp: Instant = Instant.now()

        // Get citizen list

        val citizenList = getCitizenList(List(("Param", 0, 0), ("Hugo", 50, 50), ("Pierre", 99, 99)))

        simulateIteration(1000, 0, drones, originTimestamp, citizenList, producer, List("Good", "Happy"), List("Bad", "Angry"))

        // Source.fromFile("../data/good_words.txt").close()
        // Source.fromFile("../data/bad_words.txt").close()

        // Close producer
        producer.close()
    }
}