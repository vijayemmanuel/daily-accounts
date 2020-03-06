package scalajsApp.pages

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.Grid.Lg
import org.rebeam.mui.{Button, Card, DatePicker, FormControl, Grid, InputLabel, MenuItem, NativeSelect, OutlinedInput, Select, TextField, Typography}
import scalajsApp.components.ExpenseField
import scalajsApp.diode.AppState
import scalajsApp.router.AppRouter

import scala.scalajs.js


object CurrentMonthPanel {

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, Unit]) {

    /*def getCities(props: Props) = {
      val proxy = props.proxy()
      proxy.favCitiesWeather.map {city =>
        <.div(
          ^.key := city.id,
          ^.marginBottom := 10.px,
          WeatherBox(WeatherBox.Props(Some(city), props.ctl, proxy.userInfo, isRemoveBtn = true))
        )
      }
    }*/

    val days = List("Sun","Mon","Tues","Wed","Thurs","Fri","Sat");

    def mounted: Callback = Callback.log("Mounted Current Month!")

    def render(props: Props): VdomElement = {

      val year =  new js.Date().getFullYear()
      val day = new js.Date().getDate()
      val dayOfWeek = days(new js.Date().getDay())
      val month = new js.Date().getMonth()

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
                (1 until day+1).map (d => <.option (^.key := d, ^.value  := d,d) ).toVdomArray
              )
            ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            //TODO Chnage value to current date
            ExpenseField(ExpenseField.Props(0, "Food Amount",0)),
            FormControl(fullWidth = false,variant = FormControl.Variant.Outlined,disabled = true)(
              InputLabel()("Cumulative"),
              OutlinedInput(startAdornment = VdomNode("\u20B9"  + "1000"),labelWidth = 100)
            ),
          ),
          <.br(),
          <.br(),
          //TODO Chnage value to current date
          ExpenseField(ExpenseField.Props(0, "Transport Amount",0)),
          <.br(),
          <.br(),
          //TODO Chnage value to current date
          ExpenseField(ExpenseField.Props(0, "Utility Amount",0)),
          <.br(),
          <.br(),
          //TODO Chnage value to current date
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
