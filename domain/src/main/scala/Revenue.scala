import java.time.LocalDate

case class Revenue(docType: Int, createDate: LocalDate, sum: BigDecimal, vatSum: BigDecimal)

object Revenue {

  val emptyId = -1

  def empty: Revenue = {
    Revenue(emptyId, LocalDate.MIN, BigDecimal.valueOf(0), BigDecimal.valueOf(0))
  }
}