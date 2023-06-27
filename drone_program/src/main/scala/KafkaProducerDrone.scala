import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax.EncoderOps


object KafkaProducerDrone {

  case class Report(id: Int, location: List[Double], citizens: List[String], score: List[Double], words: List[String])

  def sendReport(droneId : Int, location: List[Double]): Unit = {
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
    val obj = Report(droneId, location, List("Pierre", "Hugo", "Param", "DarkSasuke"), List(75.3, 75.3, 50.4, 6.9), List("love", "peace", "happy", "hate"))
    val json: Json = obj.asJson


    // Create Producer Record
    val record = new ProducerRecord[String, String](topic, droneId.toString, json.toString)

    // Send Record
    producer.send(record)

    // Close producer
    producer.close()
  }
}
