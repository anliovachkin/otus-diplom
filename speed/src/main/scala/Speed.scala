import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, HCursor, parser}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, current_timestamp, sum, window}
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.slf4j.LoggerFactory

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Speed extends App {
  def logger = LoggerFactory.getLogger("Speed")

  val spark = SparkSession.builder()
    .appName("Lambda speed layer")
    //.config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  def parseCheck(source: String): Revenue = {
    implicit val vatDecoder: Decoder[VatAmount] = deriveDecoder[VatAmount]

    implicit val revenueDecoder: Decoder[Revenue] = (cursor: HCursor) => {
      for {
        docType <- cursor.downField("docType").as[Int]
        createDate <- cursor.downField("createDate").as[String]
        sum <- cursor.downField("sum").as[Int]
        vatAmounts <- cursor.downField("vatAmounts").as[List[VatAmount]]
      } yield Revenue(docType, LocalDate.parse(createDate), BigDecimal(sum / 100.00)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP),
        BigDecimal(vatAmounts.map(vat => vat.vatSum * vat.vatPercent / 10000.00).sum)
          .setScale(2, BigDecimal.RoundingMode.HALF_UP))
    }

    parser.decode[Revenue](source) match {
      case Left(value) =>
        logger.error(s"Error while parse line $source\n $value")
        Revenue.empty
      case Right(value) => value
    }
  }

  private val path: String = "hdfs://namenode:9000/df/revenue/" +
    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

  spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "kafka:9092")
    .option("subscribe", "checks")
    .load()
    .selectExpr("CAST(value as String)")
    .as[String]
    .map(json => parseCheck(json))
    .filter(revenue => revenue.docType > Revenue.emptyId)
    .withColumn("timestamp", current_timestamp())
    .withWatermark("timestamp", "2 minutes")
    .groupBy(window($"timestamp", "2 minutes"),
      col("docType"), col("createDate"))
    .agg(sum("sum").alias("sum"), sum("vatSum").alias("vatSum"))
    .drop($"timestamp")
    .writeStream
    .trigger(Trigger.ProcessingTime("2 minutes"))
    .format("parquet")
    .option("checkpointLocation", "/tmp/checkpoint")
    .outputMode(OutputMode.Append())
    .start(path)
    .awaitTermination()
}
