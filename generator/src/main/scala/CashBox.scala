import org.scalacheck.Gen

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

class CashBox(val taxNumber: String, storeTaxNumber: String, operatorTaxNumber: String,
              companyTaxNumber: String, cashRegisterFactoryNumber: String, storeType: String,
              cashier: String) {

  private val docNumber = new AtomicLong();
  private val strGen = (n: Int) => Gen.listOfN(n, Gen.alphaNumChar).map(_.mkString)
  private val currencyGen = Gen.const("AZN")
  private val taxNumberGen = Gen.const(taxNumber)
  private val storeTaxNumberGen = Gen.const(storeTaxNumber)
  private val operatorTaxNumberGen = Gen.const(operatorTaxNumber)
  private val companyTaxNumberGen = Gen.const(companyTaxNumber)
  private val cashRegisterFactoryNumberGen = Gen.const(cashRegisterFactoryNumber)
  private val storeTypeGen = Gen.const(storeType)
  private val cashierGen = Gen.const(cashier)
  private val idGen = strGen(46)
  private val itemsGen: Gen[List[Item]] = Gen.nonEmptyListOf(itemGen)
  private val vatAmountsGen: Gen[List[VatAmount]] = Gen.nonEmptyListOf(vatAmountGen)

  def generateCheck: Gen[Check] = {
    Gen.frequency(
      5 -> saleGen, 3 -> manyBackGen, 2 -> rollbackGen
    )
  }

  private def manyBackGen(): Gen[ManyBackCheck] =
    for {
      id <- idGen
      currency <- currencyGen
      taxNumber <- taxNumberGen
      storeTaxNumber <- storeTaxNumberGen
      operatorTaxNumber <- operatorTaxNumberGen
      companyTaxNumber <- companyTaxNumberGen
      cashRegisterFactoryNumber <- cashRegisterFactoryNumberGen
      storeType <- storeTypeGen
      cashier <- cashierGen
      items <- itemsGen
    } yield {
      val sum = items.map(_.itemSum).sum
      ManyBackCheck(id, currency, 3, docNumber.getAndIncrement(), taxNumber, storeTaxNumber, operatorTaxNumber,
        companyTaxNumber, cashRegisterFactoryNumber, storeType, cashier,
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), sum, sum, 0, items,
        vatAmounts(items))
    }

  private def rollbackGen(): Gen[RollbackCheck] = {
    for {
      id <- idGen
      currency <- currencyGen
      taxNumber <- taxNumberGen
      storeTaxNumber <- storeTaxNumberGen
      operatorTaxNumber <- operatorTaxNumberGen
      companyTaxNumber <- companyTaxNumberGen
      cashRegisterFactoryNumber <- cashRegisterFactoryNumberGen
      storeType <- storeTypeGen
      cashier <- cashierGen
      sum <- Gen.posNum[Int]
      vatAmounts <- vatAmountsGen
    } yield {
      RollbackCheck(id, currency, 4, docNumber.getAndIncrement(), taxNumber, storeTaxNumber, operatorTaxNumber,
        companyTaxNumber, cashRegisterFactoryNumber, storeType, cashier,
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),sum, vatAmounts)
    }
  }

  private def saleGen(): Gen[SaleCheck] = {
    for {
      id <- idGen
      currency <- currencyGen
      taxNumber <- taxNumberGen
      storeTaxNumber <- storeTaxNumberGen
      operatorTaxNumber <- operatorTaxNumberGen
      companyTaxNumber <- companyTaxNumberGen
      cashRegisterFactoryNumber <- cashRegisterFactoryNumberGen
      storeType <- storeTypeGen
      cashier <- cashierGen
      items <- itemsGen
    } yield {
      val sum = items.map(_.itemSum).sum
      SaleCheck(id, currency, 0, docNumber.getAndIncrement(), taxNumber, storeTaxNumber, operatorTaxNumber,
        companyTaxNumber, cashRegisterFactoryNumber, storeType, cashier,
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), sum, sum, 0, items,
        vatAmounts(items))
    }
  }

  private def itemGen: Gen[Item] = {
    val index = new AtomicInteger(0)
    for {
      quantity <- Gen.posNum[Int]
      price <- Gen.posNum[Int]
      codeType <- Gen.posNum[Int]
      code <- strGen(8)
      vatPercent <- Gen.choose(1000, 4000)
    } yield
      Item(index.getAndIncrement(), quantity, price, codeType, price, code, vatPercent)
  }

  private def vatAmountGen: Gen[VatAmount] = {
    for {
      percent <- Gen.choose(1000, 4000)
      sum <- Gen.posNum[Int]
    } yield
      VatAmount(percent, sum)
  }

  private def vatAmounts(items: List[Item]): List[VatAmount] = {
    items
      .groupBy(item => item.itemVatPercent)
      .map(pair => VatAmount(pair._1, pair._2.map(_.itemSum).sum))
      .toList
  }
}


