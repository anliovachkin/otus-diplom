import org.apache.spark.sql.functions.{col, sum}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.LoggerFactory

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Batch extends App {
  def logger = LoggerFactory.getLogger("Batch")

  val spark = SparkSession.builder()
    .appName("Lambda speed layer")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  val path = if (args.length > 0) {
    args(0)
  } else {
    val now = LocalDate.now()
    s"year=${now.getYear}/month=${"%02d".format(now.getMonthValue)}/day=${now.getDayOfMonth}/*"
  }

  private val input = spark.read
    .format("json")
    .load("hdfs://namenode:9000/stg/checks/" + path)
    .dropDuplicates("id")
    .drop("storeType", "storeTaxNumber", "operatorTaxNumber", "items", "docNumber", "currency", "companyTaxNumber", "cashlessSum", "cashier", "cashSum", "cashRegisterFactoryNumber", "cashBoxTaxNumber", "id")
    .as[(String, Long, Long, List[VatAmount])]
    .map { case (createDate, docType, sum, vatAmounts: List[VatAmount]) => (createDate, docType,
      BigDecimal(sum / 100.00).setScale(2, BigDecimal.RoundingMode.HALF_UP),
      BigDecimal(vatAmounts.map(vat => vat.vatSum * vat.vatPercent / 10000.00).sum)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP))
    }
    .toDF("createDate", "docType", "sum", "vatSum")

  private val outputPath: String = "hdfs://namenode:9000/df/revenue/batch/" +
    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

  private val frame: DataFrame = input
    .groupBy(col("docType"), col("createDate"))
    .agg(sum("sum").alias("sum"), sum("vatSum").alias("vatSum"))
  frame
    .write
    .format("parquet")
    .option("checkpointLocation", "/tmp/checkpoint")
    .mode(SaveMode.Overwrite)
    .save(outputPath)
}
