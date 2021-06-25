import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

object Producer extends App {

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:29092")
  props.put("value.serializer", "org.apache.kafka.connect.json.JsonSerializer")
  val producer = new KafkaProducer(props, new StringSerializer, new StringSerializer)

  val cashBoxes = List(new CashBox("00001", "000001-0001", "123415", "00001", "sasqwqwq", "MAGAZINES", "ivanov"),
    new CashBox("00002", "000003-0001", "123415", "000003", "sasqwqwq", "MAGAZINES", "petrov"))

  implicit val itemEncode: Encoder[Item] = deriveEncoder[Item]
  implicit val vatEncode: Encoder[VatAmount] = deriveEncoder[VatAmount]
  implicit val saleEncode: Encoder[SaleCheck] = deriveEncoder[SaleCheck]
  implicit val rollbackEncode: Encoder[RollbackCheck] = deriveEncoder[RollbackCheck]
  implicit val manyBackEncode: Encoder[ManyBackCheck] = deriveEncoder[ManyBackCheck]
  implicit val checkEncode: Encoder[Check] = Encoder.instance {
    case sale @ SaleCheck(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_) => sale.asJson
    case manyBack @ ManyBackCheck(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_) => manyBack.asJson
    case rollback @ RollbackCheck(_,_,_,_,_,_,_,_,_,_,_,_,_,_) => rollback.asJson
  }

  while (true) {
    cashBoxes.foreach(cashBox => send(cashBox.generateCheck.sample.get.asJson.noSpaces))
    Thread.sleep(1000)
  }

  private def send(check: String) = {
    producer.send(new ProducerRecord("checks", check))
  }

  producer.close()
}
