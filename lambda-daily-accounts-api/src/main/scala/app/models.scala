package app

case class Expense(Date: String, Food: String, Transport: String, Utility: String)

object ApiModels {
  case class Req(in: Expense)
  case class Resp(message: List[Expense])

}
