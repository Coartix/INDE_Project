import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps

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
import WorldGenerator.getCitizenList


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

    def simulateIteration(generation: Int, counter: Int, drones: List[Drone], originTimestamp: Instant, citizenList: List[(String, Double, Double, Int)], producer: KafkaProducer[String, String]): Unit = counter match {
        case counter if (counter == generation) => {
            val messages = drones.map(drone => (drone.id.toString, getMessage(drone.id, moveDrone(drone.location), citizenList, originTimestamp, counter)))
            messages.foreach { case (droneId, message) => sendReport(droneId, message, producer) } 
        }
        case counter => {
            val messages = drones.map(drone => (drone.id.toString, getMessage(drone.id, moveDrone(drone.location), citizenList, originTimestamp, counter)))
            messages.foreach { case (droneId, message) => sendReport(droneId, message, producer) } 

            simulateIteration(generation, counter + 1, drones, originTimestamp, citizenList, producer)
        }
    }

    def main(args: Array[String]) : Unit = {

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
        val citizenList = getCitizenList("../data/citizens.txt")

        simulateIteration(50, 0, drones, originTimestamp, citizenList, producer)

        // Close producer
        producer.close()
    }
}