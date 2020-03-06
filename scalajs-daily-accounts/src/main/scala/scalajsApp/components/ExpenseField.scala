package scalajsApp.components

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.{FormControl, InputLabel, OutlinedInput}
import scalajsApp.diode.{AddFoodExpense, AppCircuit, AppState}

import scala.scalajs.js

object ExpenseField {

  case class State(localExpense: Int)

  case class Props(date: Int, label: String, defaultExpense: Int)

  class Backend($: BackendScope[Props, State]) {

    def getCurrentDate = {
      $.props.map(_.date).runNow()
    }

    def getCurrentLabel = {
      $.props.map(_.label).runNow()
    }

    def updateDiodeState(value: Int) = {
      Callback(
        getCurrentLabel match {
        case "Food Amount" => AppCircuit.dispatch(AddFoodExpense(date = getCurrentDate, food = value.toInt))
      }).runNow()
    }

    def onValueChange(e: ReactEventFromInput) = {
      e.preventDefaultCB
      val newValue = e.target.value
      //val date = $.props.map(_.date)
      //Callback.log ($.props.map(_.label).runNow()) >>
      //Callback.log ($.state.map(_.localExpense).runNow())
      $.modState(s => {

             s.copy(if (newValue != "") newValue.toInt else 0)
        AppCircuit.dispatch(AddFoodExpense(date = 111, food = 0))
        s
      }
        )




      //$.modState(s => s.copy(e.target.value.toInt))
    }

    def mounted : Callback = {
      Callback.log("Mounted ExpenseField")
    }

    def recieveProps: Callback = {
      Callback.log("Receive Props Update to ExpenseField")
      $.modState((s,p) => s.copy(p.defaultExpense))

    }

    def render(props: Props, state: State): VdomElement = {
      FormControl(fullWidth = false, variant = FormControl.Variant.Outlined)(
        InputLabel()(props.label),
        OutlinedInput(startAdornment = VdomNode("\u20B9"),`type` = "number",
          value = js.Any.fromInt(state.localExpense), readOnly = false,
          labelWidth = 100, onChange = onValueChange _)
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("ExpenseField")
    .initialState(State(0))
    .renderBackend[Backend]
    .componentWillReceiveProps(scope => scope.backend.recieveProps)
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props).vdomElement
}
