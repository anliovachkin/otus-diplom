
case class RollbackCheck(id: String, currency: String, docType: Int, docNumber: Long,
                         cashBoxTaxNumber: String, storeTaxNumber: String, operatorTaxNumber: String,
                         companyTaxNumber: String, cashRegisterFactoryNumber: String, storeType: String,
                         cashier: String, createDate: String, sum: Int, vatAmounts: List[VatAmount])
  extends Check
