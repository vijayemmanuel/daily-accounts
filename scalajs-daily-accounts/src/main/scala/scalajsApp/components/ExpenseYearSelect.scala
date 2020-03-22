package scalajsApp.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.NativeSelect

object ExpenseYearSelect {

  case class Props(
                    year: Int,
                    onYearChange: (Int) => CallbackTo[Unit]
                  )

  val Component = ScalaFnComponent[Props]{ props =>

    def onChange(e: ReactEventFromInput): Callback = {
      val yearId = e.target.value
      props.onYearChange(yearId.toInt)
    }

    NativeSelect(onChange = onChange _) (
      (2018 until props.year+1).map (d => <.option (^.key := d, ^.value  := d,d)).toVdomArray
    )

  }


  def apply(props: Props) = Component(props)
}