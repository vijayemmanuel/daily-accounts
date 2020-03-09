package scalajsApp.components

import scalajs.js
import scalajs.js.annotation._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.NativeSelect

object ExpenseDaySelect {

  case class Props(
                    day: Int,
                    onDayChange: (Int) => CallbackTo[Unit]
                  )

  val Component = ScalaFnComponent[Props]{ props =>

    def onChange(e: ReactEventFromInput): Callback = {
      val day = e.target.value
      val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
      val year = new js.Date().getFullYear()
      val dayId = year.toString +
        (if (month.toString.length == 1) "0" + month.toString else month.toString) +
        (if (day.toString.length == 1) "0" + day.toString else day.toString)
      //Callback.log(dayId) >>
      props.onDayChange(dayId.toInt)
    }

    val days = List("Sat","Sun","Mon","Tues","Wed","Thurs","Fri")

    NativeSelect(onChange = onChange _) (
      (1 until props.day+1).reverse.map (d => <.option (^.key := d, ^.value  := d,d + " - " + days(d%7))).toVdomArray
    )

  }


  def apply(props: Props) = Component(props)
}
