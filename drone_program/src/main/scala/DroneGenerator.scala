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

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import KafkaProducerDrone.sendReport

object DroneGenerator {

    case class Drone(id: Int, location: (Double, Double)) {
        // Method to launch a script at a certain path
        def launchScript(path: String): Unit = {
            // Execute the script logic here
            sendReport(id)
            // println(s"Drone $id launched script at path: $path")
        }
        
        // Method to schedule script execution every minute
        def scheduleScriptExecution(scriptPath: String, executorService: ScheduledExecutorService)(implicit ec: ExecutionContext): Future[Unit] = {
            val promise = Promise[Unit]()
            // Schedule the script execution every minute
            executorService.scheduleAtFixedRate(
                () => {
                    try {
                        // Call the script method
                        launchScript(scriptPath)
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

    def generateDrone(n : Int): List[Drone] = {
        List(Drone(0, (0.0, 0.0)), Drone(1, (50.0, 50.0)), Drone(2, (100.0, 100.0)))
    }

    def main(args: Array[String]) : Unit = {
        val drones = generateDrone(3)
        // Create a single-threaded executor service
        val executorService = Executors.newSingleThreadScheduledExecutor()

        // Schedule script execution for each drone
        val futures: List[Future[Unit]] = drones.map(_.scheduleScriptExecution("/path/to/script", executorService))

        // Wait for all futures to complete (if needed)
        // val allFutures: Future[List[Unit]] = Future.sequence(futures)

        /*allFutures.onComplete {
            case Success(_) => println("All script executions completed successfully")
            case Failure(ex) => println(s"Script execution failed: ${ex.getMessage}")
        }*/

        // Wait for all futures to complete
        val allFutures: Future[List[Unit]] = Future.sequence(futures)
        Await.result(allFutures, Duration.Inf)

        // Shutdown the executor service
        executorService.shutdown()
    }
}