import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object KafkaProducerDrone {

  case class Report(id: Int, location: (Int, Int), citizens: List[String], score: List[Int], words: List[String])

  case class Dummy(s1: String, s2: String, s3: String)

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

    val obj = Dummy("Hello", "World", "Scala")
    val json: Json = obj.asJson

    val record = new ProducerRecord[String, String](topic, "1", json)

    producer.send(record)

    producer.close()
  }
}
