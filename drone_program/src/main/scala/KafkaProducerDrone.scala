import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps


object KafkaProducerDrone {

  case class Report(id: Int, location: (Double, Double), citizens: List[String], score: List[Double], words: List[String])

  def main(args: Array[String]): Unit = {
    // Print hello
    println("Hello, world!")

    // Create Kafka producer
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

    val producer = new KafkaProducer[String, String](props)

    /* val position = "latitude: 37.7749, longitude: -122.4194"
    val number = 42

    val record = new ProducerRecord[String, String](
      "drone-message",
      position,
      number.toString
    )*/

    val topic = "drone-message"

    val obj = Report(69, (42.12, 42.23), List("Pierre", "Hugo", "Param", "DarkSasuke"), List(75.3, 75.3, 50.4, 6.9), List("love", "peace", "happy", "hate"))
    val json: Json = obj.asJson

    val record = new ProducerRecord[String, String](topic, "1", json.toString)

    producer.send(record)

    producer.close()
  }
}
