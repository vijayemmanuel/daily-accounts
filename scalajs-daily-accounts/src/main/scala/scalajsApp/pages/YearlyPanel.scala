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
import scalajsApp.components.{Chart, ChartData, ChartDataset, ExpenseDaySelect, ExpenseField, ExpenseSnackBar}
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
import scala.util.{Random, Try}


object YearlyPanel {

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, Unit]) {

    // create dummy data for the chart
    val cp = Chart.ChartProps(
      "Test chart",
      Chart.BarChart,
      ChartData(
        Random.alphanumeric.map(_.toUpper.toString).distinct.take(10),
        Seq(ChartDataset(Iterator.continually(Random.nextDouble() * 10).take(10).toSeq, "Data1"))
      )
    )


    def mounted: Callback = {
      Callback.log("Mounted Yearly!")
      //updateState
    }


    def render(props: Props): VdomElement = {
      <.div(
        <.h2("Dashboard"),
        Chart(cp)
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("YearlyPage")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
