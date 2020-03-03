package scalajsApp.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.{FormControl, InputLabel, OutlinedInput}


import scala.scalajs.js

object ExpenseField {

  case class State(localExpense: Int)

  case class Props(label: String, defaultExpense: Int)

  class Backend($: BackendScope[Props, State]) {

    def onValueChange(e: ReactEventFromInput) = {
      $.modState(s => s.copy(e.target.value.toInt))
    }

    def recieveProps: Callback = {
      Callback.log("Recieve Props Update to ExpenseField")
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
    .build

  def apply(props: Props) = Component(props).vdomElement
}
