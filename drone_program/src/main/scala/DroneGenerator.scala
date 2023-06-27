import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

import java.util.concurrent.{ScheduledExecutorService, TimeUnit, Executors}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import KafkaProducerDrone.sendReport

import scala.util.Random

object DroneGenerator {

    case class Drone(id: Int, location: List[Double]) {
        
        // Method to schedule script execution every minute
        def scheduleScriptExecution(executorService: ScheduledExecutorService)(implicit ec: ExecutionContext): Future[Unit] = {
            val promise = Promise[Unit]()
            // Schedule the script execution every minute
            executorService.scheduleAtFixedRate(
                () => {
                    try {
                        // Call the script method
                        sendReport(id, moveDrone(location))
                    } catch {
                            case ex: Exception => promise.failure(ex)
                    }
                },
                0, // Initial delay before the first execution
                1.minute.toSeconds, // Delay between consecutive executions (1 minute in this case)
                TimeUnit.SECONDS
            )
            promise.future
        }
        
    }

    def moveDrone(location : List[Double]): List[Double] = location match {
        case Nil => Nil
        case e::tail => e + Random.nextInt(7) - 3 :: moveDrone(tail)
    }

    def generateDrone(n : Int): List[Drone] = n match {
        case 0 => Nil
        case n => Drone(n, List(Random.nextInt(100), Random.nextInt(100))) :: generateDrone(n - 1)
    }

    def main(args: Array[String]) : Unit = {
        val drones = generateDrone(3)
        
        // Create a single-threaded executor service
        val executorService = Executors.newSingleThreadScheduledExecutor()

        // Schedule script execution for each drone
        val futures: List[Future[Unit]] = drones.map(_.scheduleScriptExecution(executorService))

        // Wait for all futures to complete
        val allFutures: Future[List[Unit]] = Future.sequence(futures)
        Await.result(allFutures, Duration.Inf)

        // Shutdown the executor service
        executorService.shutdown()
    }
}