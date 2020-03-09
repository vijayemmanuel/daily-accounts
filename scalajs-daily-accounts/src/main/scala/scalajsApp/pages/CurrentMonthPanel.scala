package scalajsApp.pages

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.Grid.Lg
import org.rebeam.mui.{Button, Card, DatePicker, FormControl, Grid, InputLabel, MenuItem, NativeSelect, OutlinedInput, Select, TextField, Typography}
import scalajsApp.components.ExpenseField
import scalajsApp.diode.{AddFoodExpense, AddTransportExpense, AddUtilityExpense, AppCircuit, AppState}
import scalajsApp.router.AppRouter

import scala.scalajs.js


object CurrentMonthPanel {

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, Unit]) {

    val days = List("Sat","Sun","Mon","Tues","Wed","Thurs","Fri");

    val day = new js.Date().getDate()
    val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
    val year = new js.Date().getFullYear()

    val dayId = year.toString +
      (if (month.toString.length == 1) "0" + month.toString else month.toString) +
      (if (day.toString.length == 1) "0" + day.toString else day.toString)

    def onExpenseValueChange(value: Int, label: String) =
      CallbackTo {
        label match {
          case "Food Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddFoodExpense(date = dayId.toInt, food = value))
            // Update the local state so as to propagate to props in children
            //$.modState(s => s.copy(foodExp = value)).runNow()
          }
          case "Transport Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddTransportExpense(date = dayId.toInt, transport = value))
            // Update the local state so as to propagate to props in children
            //$.modState(s => s.copy(transportExp = value)).runNow()
          }
          case "Utility Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddUtilityExpense(date = dayId.toInt, utility = value))
            // Update the local state so as to propagate to props in children
            //$.modState(s => s.copy(utilityExp = value)).runNow()
          }

        }
      }

    def mounted: Callback = Callback.log("Mounted Current Month!")

    def render(props: Props): VdomElement = {
      <.div (
        Grid(container = true, direction = Grid.Direction.Column,
          justify = Grid.Justify.Center,
          alignItems = Grid.AlignItems.Center)(
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.Center,
            alignItems = Grid.AlignItems.Center)(
              Typography(align = Typography.Align.Center,color = Typography.Color.Primary,variant = Typography.Variant.H6)("Select Day : "),
              NativeSelect() (
                (1 until day+1).reverse.map (d => <.option (^.key := d, ^.value  := d,d + " - " + days(d%7))).toVdomArray
              )
            ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(

            ExpenseField(ExpenseField.Props("Food Amount",props.proxy.zoom(_.foodExpense).value,onExpenseValueChange, false)),
            ExpenseField(ExpenseField.Props("Cumulative",1000,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
          ExpenseField(ExpenseField.Props("Transport Amount",props.proxy.zoom(_.transportExpense).value,onExpenseValueChange, false)),
          ExpenseField(ExpenseField.Props("Cumulative",1000,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
          ExpenseField(ExpenseField.Props("Utility Amount",props.proxy.zoom(_.utilityExpense).value,onExpenseValueChange, false)),
          ExpenseField(ExpenseField.Props("Cumulative",1000,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Button(variant =  Button.Variant.Contained,color = Button.Color.Primary)(VdomNode("Save"))


        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("CurrentMonthPage")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
