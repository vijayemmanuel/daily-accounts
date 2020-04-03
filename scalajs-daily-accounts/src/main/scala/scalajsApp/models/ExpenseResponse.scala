package scalajsApp.models


case class Expense(
                   Date: String,
                   Food: String,
                   Transport: String,
                   Utility: String,
                   Other: String
                  )

case class ExpenseResponse (
                             message: List[Expense]
                           )

case class ExpenseRequest (
                            in: Expense
                          )

object NotifType extends Enumeration {
  type NotifType = Value
  val Success, Error, Severe = Value
}