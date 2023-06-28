import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecords
import scala.collection.JavaConverters._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.{S3Client, S3Configuration}
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, PutObjectResponse}

import java.time.{Instant, LocalDateTime, ZoneOffset, Duration}

object KafkaConsumerStore {
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
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "drone-consumer-store")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(java.util.Collections.singletonList("drone-message"))

    consumeMessages(consumer)
  }

  def consumeMessages(consumer: KafkaConsumer[String, String]): Unit = {
    val s3Client = S3Client.builder()
      .region(software.amazon.awssdk.regions.Region.EU_WEST_3)
      .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
      .build()

    val bucketName = "publiccoartixbucket"

    val records: ConsumerRecords[String, String] = consumer.poll(java.time.Duration.ofMillis(100))
    records.asScala.foreach { record =>
      // Deserialization: Convert the JSON back to the object
      val json: JsValue = Json.parse(record.value())

      json.validate[Report] match {
        case JsSuccess(report, _) =>
          // Serialize the report back to JSON
          val serializedReport: String = Json.toJson(report).toString()

          // Generate a unique object key for each report
          val objectKey = java.util.UUID.randomUUID().toString

          // Upload the report JSON to S3
          val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .contentEncoding("UTF-8")
            .contentType("application/json")
            .build()

          val requestBody = RequestBody.fromString(serializedReport)
          val response: PutObjectResponse = s3Client.putObject(putRequest, requestBody)
          println(s"Report uploaded to S3: s3://$bucketName/$objectKey")
        case JsError(errors) =>
          println(s"Failed to parse JSON: $errors")
          //println(s"Failed JSON: $json")
      }
    }
    consumeMessages(consumer)
  }
}