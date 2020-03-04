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
    case AddExpense(date, foodExpense, transportExpense, utilityExpense) => updated(value.copy(date = date, foodExpense = foodExpense, transportExpense = transportExpense, utilityExpense = utilityExpense))
    case RemoveExpense(date) => updated(value.copy(date = date, foodExpense = 0, transportExpense = 0, utilityExpense = 0 ))
    case SetLoadingState() => updated(value.copy(isLoading = true))
    case ClearLoadingState() => updated(value.copy(isLoading = false))
  }
}
