package scalajsApp.components

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.{FormControl, InputLabel, OutlinedInput}
import scalajsApp.diode.{AddFoodExpense, AppCircuit, AppState}

import scala.scalajs.js

object ExpenseField {

  case class State(var localExpense: Int)

  case class Props(label: String,
                   defaultExpense: Int,
                   onExpenseValueChange: (Int, String) => CallbackTo[Unit])

  class Backend($: BackendScope[Props, State]) {

    def onFocusChange (e: ReactFocusEvent): Callback = {
      // Important to have runNow in the end as 'onExpenseValueChange' returns callback
      $.props.map(p => p.onExpenseValueChange($.state.map(s => s.localExpense).runNow(),p.label)).runNow()
    }

    def onValueChange(e: ReactEventFromInput): CallbackTo[Unit] = {
      val newValue = e.target.value
      $.modState((s,p) => {
        if (newValue != "") {
          s.copy(localExpense = newValue.toInt)
        }
        else {
          s.copy(localExpense = 0)
        }
      })
    }

    def mounted: Callback = {
      Callback.log("Mounted ExpenseField")
    }

    def recieveProps: Callback = {
      Callback.log("Receive Props Update to ExpenseField")
      $.modState((s, p) => s.copy(p.defaultExpense))
    }

    def render(props: Props, state: State): VdomElement = {
      FormControl(fullWidth = false, variant = FormControl.Variant.Outlined)(
        InputLabel()(props.label),
        OutlinedInput(
          startAdornment = VdomNode("\u20B9"),
          `type` = "number",
          value = js.Any.fromInt(state.localExpense),
          readOnly = false,
          labelWidth = 100,
          onChange = onValueChange _,
          onBlur = onFocusChange _
          )
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
