package scalajsApp.pages

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.Grid.Lg
import org.rebeam.mui.{Button, Card, DatePicker, FormControl, Grid, InputLabel, MenuItem, OutlinedInput, Select, Snackbar, TextField, Typography}
import org.scalajs.dom
import scalajsApp.components.{ExpenseDaySelect, ExpenseField, ExpenseSnackBar}
import scalajsApp.config.Config
import scalajsApp.diode.{AddFoodExpense, AddTransportExpense, AddUtilityExpense, AppCircuit, AppState, ClearLoadingState, SetLoadingState}
import scalajsApp.models.{Expense, ExpenseRequest, ExpenseResponse, NotifType}
import scalajsApp.router.AppRouter
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import scalajsApp.models.NotifType.NotifType

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Try


object LastMonthPanel {

  case class State (var foodSum : Int,
                    var transportSum : Int,
                    var utilitySum: Int,
                    var otherSum: Int,
                   )

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, State]) {

    val host = Config.AppConfig.apiHost

    val months = List("Jan", "Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

    val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
    val year = new js.Date().getFullYear()

    val lastmonthId = if (month == 12) (year-1).toString + "01"
    else year.toString + (if ((month - 1).toString.length == 1) "0" + (month - 1).toString else (month - 1).toString)

    def updateState = {
      AppCircuit.dispatch(SetLoadingState())

      // Launch the API
      def getData(): Future[List[Expense]] = {
        println(s"Requesting expenses for $lastmonthId")
        // Note that we have added additional header to enable CORS policy in the request
        dom.ext.Ajax.get(url = s"$host/dev/expense?date=$lastmonthId").map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => List(Expense("0", "0", "0", "0","0"))
            case Right(data) => data.message
          }
        })
      }

      def modPartialState(data: Future[List[Expense]]) = {

        Callback.future(
          data.map { value =>

            val foodOnly = value.map(p => p.Food.toInt)
            var foodSum = foodOnly.fold(0)((a: Int, b: Int) => a + b)

            val transportOnly = value.map(p => p.Transport.toInt)
            var transportSum = transportOnly.fold(0)((a: Int, b: Int) => a + b)

            val utilityOnly = value.map(p => p.Utility.toInt)
            var utilitySum = utilityOnly.fold(0)((a: Int, b: Int) => a + b)

            val otherOnly = value.map(p => p.Other.toInt)
            var otherSum = otherOnly.fold(0)((a: Int, b: Int) => a + b)

            AppCircuit.dispatch(ClearLoadingState())
            $.modState(s => s.copy(foodSum = foodSum, transportSum = transportSum, utilitySum = utilitySum, otherSum = otherSum))
          }
        )
      }

      modPartialState(getData())
    }


    def mounted: Callback = {
      Callback.log("Mounted Current Month!")
      updateState
    }


    def render(props: Props, state: State): VdomElement = {

      <.div (
        Grid(container = true, direction = Grid.Direction.Column,
          justify = Grid.Justify.Center,
          alignItems = Grid.AlignItems.Center)(
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.Center,
            alignItems = Grid.AlignItems.Center)(
            Typography(align = Typography.Align.Center,color = Typography.Color.Primary,variant = Typography.Variant.H3)(
              months(s"$lastmonthId".splitAt(4)._2.toInt-1) + " " + s"$lastmonthId".splitAt(4)._1),

          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            ExpenseField(ExpenseField.Props("Food Expense",state.foodSum,( _:Int,  _:String) => Callback.empty, true))
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            ExpenseField(ExpenseField.Props("Transport Expense",state.transportSum,( _:Int,  _:String) => Callback.empty, true))
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            ExpenseField(ExpenseField.Props("Utility Expense",state.utilitySum,( _:Int,  _:String) => Callback.empty,true))
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            ExpenseField(ExpenseField.Props("Other Expense",state.otherSum,( _:Int,  _:String) => Callback.empty, true))
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceAround,
            alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4)(
            ExpenseField(ExpenseField.Props("Total Monthly Expense",state.foodSum + state.transportSum + state.utilitySum + state.otherSum,( _:Int,  _:String) => Callback.empty,true))
          )
        )

      )

    }
  }

  val Component = ScalaComponent.builder[Props]("LastMonthPage")
    .initialState(State(0,0,0,0))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
