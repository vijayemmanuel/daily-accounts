package scalajsApp.components

import diode.react.ModelProxy
import scalajsApp.diode.AppState
import japgolly.scalajs.react.ScalaFnComponent
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.LinearProgress

object ExpenseLoadingIndicator {
  case class Props(proxy: ModelProxy[AppState])

  val Component = ScalaFnComponent[Props](props => {
    val proxy = props.proxy()
    if (proxy.isLoading) {
      println("Loading is True")
      <.div(
        ^.width := "100%",
        //^.marginTop := 2.px,
        <.div(LinearProgress(color = LinearProgress.Color.Secondary))
      )
    } else {
      println("Loading is False")
      <.div()
    }
  })

  def apply(props: Props) = Component(props)
}

