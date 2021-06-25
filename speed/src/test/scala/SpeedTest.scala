import Speed.parseCheck
import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class SpeedTest extends AnyFlatSpec {

  it should "parse source" in {
    val source =
      """
        |{
        |  "id": "06WCYfVABpzP3xv9D7Mg5BORQwHZ1r1qNDyQUOmNnGAa4D",
        |  "currency": "AZN",
        |  "docType": 0,
        |  "docNumber": 28,
        |  "cashBoxTaxNumber": "00002",
        |  "storeTaxNumber": "000003-0001",
        |  "operatorTaxNumber": "123415",
        |  "companyTaxNumber": "000003",
        |  "cashRegisterFactoryNumber": "sasqwqwq",
        |  "storeType": "MAGAZINES",
        |  "cashier": "petrov",
        |  "createDate": "2021-06-23",
        |  "sum": 4250,
        |  "cashSum": 4250,
        |  "cashlessSum": 0,
        |  "items": [
        |    {
        |      "itemNumber": 1132,
        |      "itemQuantity": 75,
        |      "itemPrice": 70,
        |      "itemCodeType": 17,
        |      "itemSum": 70,
        |      "itemCode": "KwkMipSD",
        |      "itemVatPercent": 3845
        |    }
        |  ],
        |  "vatAmounts": [
        |    {
        |      "vatPercent": 3669,
        |      "vatSum": 40
        |    }
        |  ]
        |}
        |""".stripMargin
    val result = parseCheck(source)
    assert(result.docType == 0)
    assert(result.createDate == LocalDate.of(2021, 6, 23))
    assert(result.sum == BigDecimal("42.50"))
    assert(result.vatSum == BigDecimal("14.68"))
  }
}
