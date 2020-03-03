package scalajsApp.models


case class Expense(
                   Date: String,
                   Food: String,
                   Transport: String,
                   Utility: String
                  )

case class ExpenseResponse (
                             message: Expense
                           )

case class ExpenseRequest (
                            in: Expense
                          )