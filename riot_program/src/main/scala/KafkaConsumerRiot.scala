import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecords
import scala.collection.JavaConverters._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.time.{Instant, LocalDateTime, ZoneOffset, Duration}

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

object KafkaConsumerRiot {
  case class Report(id: Int, location: List[Double], citizens: List[String], score: List[Int], words: List[String], timestamp: Instant)

  object Report {
    implicit val reportFormat: Format[Report] = (
      (JsPath \ "id").format[Int] and
      (JsPath \ "location").format[List[Double]] and
      (JsPath \ "citizens").format[List[String]] and
      (JsPath \ "score").format[List[Int]] and
      (JsPath \ "words").format[List[String]] and
      (JsPath \ "timestamp").format[Instant]
    )(Report.apply, unlift(Report.unapply))
  }

  def main(args: Array[String]): Unit = {
    // Create Kafka consumer
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "drone-consumer-riot")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(java.util.Collections.singletonList("drone-message"))

    consumeMessages(consumer)
  }

  def sendReport(report: String): Unit = {
    val httpClient = HttpClientBuilder.create().build()
    val post = new HttpPost("http://localhost:5000/receive-report")
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity(report))

    val response = httpClient.execute(post)
    // Handle response if needed
    httpClient.close()
  }

  def AlertRiot(report: Report): Unit = {
    val score = report.score
    val words = report.words
    val citizens = report.citizens
    val location = report.location

    val alert = score.zip(citizens).filter(_._1 < 25.0).map(_._2).mkString(", ")

    if (alert != "") {
      println(s"Alert: $alert")
      println(s"Location: $location")
      println(s"id: ${report.id}")
      println(s"timestamp: ${report.timestamp}")
      println(s"drone id: ${report.id}")

      try {
        val serializedReport = Json.toJson(report).toString()

        // Send alert to Riot API
        sendReport(serializedReport)
      } catch {
        case e: Exception => println("Failed to send report to Riot API")
      }
    }
  }


  def consumeMessages(consumer: KafkaConsumer[String, String]): Unit = {
    val records: ConsumerRecords[String, String] = consumer.poll(java.time.Duration.ofMillis(100))
    records.asScala.map { record =>
        // Deserialization: Convert the JSON back to the object
        val json: JsValue = Json.parse(record.value())

        json.validate[Report] match {
          case JsSuccess(report, _) =>
            //println(s"Successfully parsed JSON into Report: $report")
            AlertRiot(report)

          case JsError(errors) =>
            println(s"Failed to parse JSON: $errors")
            //println(s"Failed JSON: $json")
        }
      }
    consumeMessages(consumer)
  }
}