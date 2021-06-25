
case class ManyBackCheck(id: String, currency: String, docType: Int, docNumber: Long,
                         cashBoxTaxNumber: String, storeTaxNumber: String, operatorTaxNumber: String,
                         companyTaxNumber: String, cashRegisterFactoryNumber: String, storeType: String,
                         cashier: String, createDate: String, sum: Int, cashSum: Int, cashlessSum: Int,
                         items: List[Item], vatAmounts: List[VatAmount]) extends Check