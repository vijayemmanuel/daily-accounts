package scalajsApp.components

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{BackendScope, CallbackTo, ScalaComponent}
import japgolly.scalajs.react.vdom.html_<^.{<, VdomNode, ^}
import org.rebeam.mui.Snackbar
import scalajsApp.models.NotifType
import scalajsApp.models.NotifType.NotifType

object ExpenseSnackBar {


  case class State (
                     localNotifStatus: Boolean,
                     localNotifMessage: VdomNode
                   )

  case class Props(
                    saveNotifStatus: Boolean,
                    saveNotifType: NotifType
                  )

  class Backend($: BackendScope[Props, State]) {

    def onNotifClose  = {
      $.modState(s => s.copy(localNotifStatus = false))
    }

    def recieveProps = {
      $.modState((s, p) => s.copy(
        localNotifStatus = p.saveNotifStatus,
        localNotifMessage = p.saveNotifType match {
          case NotifType.Success => VdomNode("Saved Expenses")
          case NotifType.Error => VdomNode("Unable to Save Expenses. Check API")
          case _ => VdomNode("Unable to Save Expenses. Serious Issue !")
        }
      ))
    }

    def render(props: Props, state: State): VdomElement = {
     Snackbar(open = state.localNotifStatus,
        autoHideDuration = 3000,
        message = state.localNotifMessage,
        onClose = onNotifClose
      )()
    }
  }

  val Component = ScalaComponent.builder[Props]("ExpenseSnackBar")
    .initialState(State(false, VdomNode("")))
    .renderBackend[Backend]
    .componentWillReceiveProps(scope => scope.backend.recieveProps)
    //.componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props).vdomElement

}