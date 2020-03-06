package scalajsApp.diode

import diode.Action

case class AppState (
                      date : Int,
                      foodExpense: Int,
                      transportExpense: Int,
                      utilityExpense: Int,
                      isLoading: Boolean,
                    )

case class AppModel(
                     state: AppState
                   )

case class SetLoadingState() extends Action

case class ClearLoadingState() extends Action

case class AddFoodExpense(date: Int, food: Int) extends Action

case class AddTransportExpense(date: Int, transport: Int) extends Action

case class AddUtilityExpense(date: Int, utility: Int) extends Action

case class RemoveExpense(date : Int) extends Action

