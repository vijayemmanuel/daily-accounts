package scalajsApp.diode

import diode._
import diode.react.ReactConnector

object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {
  def initialModel = AppModel(
    AppState(
      date = 0,
      foodExpense = 0,
      transportExpense = 0,
      utilityExpense = 0,
      isLoading = false,
    )
  )

  override protected val actionHandler = composeHandlers(
    new ExpenditurePageHandler(zoomTo(_.state))
  )
}

class ExpenditurePageHandler[M](modelRW: ModelRW[M, AppState]) extends ActionHandler(modelRW) {
  override def handle = {
    case AddFoodExpense(date, foodExpense) => {
      println("Add Food Expense - " + date +  " - " + foodExpense)
      updated(value.copy(date = date, foodExpense = foodExpense))
    }
    case AddTransportExpense(date, transportExpense) => updated(value.copy(date = date, transportExpense = transportExpense))
    case AddUtilityExpense(date, utilityExpense) => updated(value.copy(date = date, utilityExpense = utilityExpense))
    case RemoveExpense(date) => updated(value.copy(date = date, foodExpense = 0, transportExpense = 0, utilityExpense = 0 ))
    case SetLoadingState() => updated(value.copy(isLoading = true))
    case ClearLoadingState() => updated(value.copy(isLoading = false))
  }
}
