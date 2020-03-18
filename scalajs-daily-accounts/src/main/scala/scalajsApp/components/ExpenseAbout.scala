package scalajsApp.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.{Button, Dialog, DialogContent, FormControl, InputLabel, OutlinedInput, Typography}


object ExpenseAbout {

  case class State(
                    var open: Boolean // state to manage if the About dialog is open or closed
                  )

  case class Props(
                    open: Boolean,
                    onClose : (Boolean) => Unit// prop field to get the value from parent
                  )

  class Backend($: BackendScope[Props, State]) {

    def onButtonClick(e: ReactMouseEvent) = {
      $.props.map(p => p.onClose(false))
    }

    def recieveProps: Callback = {
      $.modState((s, p) => s.copy(p.open))
    }

    def render(props: Props, state: State): VdomElement = {
      Dialog(open = props.open)(
        DialogContent()(
          Typography(align = Typography.Align.Center,variant = Typography.Variant.Body1)(
        VdomNode("Made with <3, Powered with ScalaJS and ScalaJS React"))),
        Button(color = Button.Color.Primary,variant = Button.Variant.Contained, size = Button.Size.Small, onClick = onButtonClick _)(VdomNode("Ok"))
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("ExpenseAbout")
    .initialState(State(false))
    .renderBackend[Backend]
    .componentWillReceiveProps(scope => scope.backend.recieveProps)
    .build

  def apply(props: Props) = Component(props).vdomElement
}
