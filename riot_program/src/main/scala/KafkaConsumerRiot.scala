import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecords
import scala.collection.JavaConverters._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.time.{Instant, LocalDateTime, ZoneOffset, Duration}

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

  def AlertRiot(report: Report): Unit = {
    val score = report.score
    val words = report.words
    val citizens = report.citizens
    val location = report.location

    val alert = score.zip(citizens).filter(_._1 > 85.0).map(_._2).mkString(", ")

    if (alert != "") {
      println(s"Alert: $alert")
      println(s"Location: $location")
      println(s"id: ${report.id}")
      println(s"timestamp: ${report.timestamp}")
      println(s"drone id: ${report.id}")

      // Send alert to Riot API

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