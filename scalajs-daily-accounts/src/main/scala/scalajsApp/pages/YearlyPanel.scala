package scalajsApp.pages

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.{Button, Card, DatePicker, FormControl, Grid, InputLabel, MenuItem, OutlinedInput, Select, Snackbar, TextField, Typography}
import org.scalajs.dom
import scalajsApp.components.{Chart, ChartData, ChartDataset, ExpenseYearSelect, JSChart}
import scalajsApp.config.Config
import scalajsApp.diode.{AddFoodExpense, AddTransportExpense, AddUtilityExpense, AppCircuit, AppState, ClearLoadingState, SetLoadingState}
import scalajsApp.models.{Expense, ExpenseRequest, ExpenseResponse, NotifType}
import scalajsApp.router.AppRouter
import io.circe.parser.decode
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.scalajs.js


object YearlyPanel {

  case class State(
                    yearId: Int,
                    labelMonth: Seq[String],
                    yearlyFoodExp: Seq[Double],
                    yearlyTransportExp: Seq[Double],
                    yearlyUtilityExp: Seq[Double],
                    yearlyOtherExp: Seq[Double]
                  )
  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, State]) {

    val host = Config.AppConfig.apiHost
    val currentYear = new js.Date().getFullYear()

    val monthNames = List("Jan","Feb","Mar","Apr","May","June","Jul","Aug","Sep","Oct","Nov","Dec")

    def updateState(selectedYear: Int) = {

      AppCircuit.dispatch(SetLoadingState())
      // Launch the API
      def getData(): Future[List[Expense]] = {
        println(s"Requesting expenses for $selectedYear")
        // Note that we have added additional header to enable CORS policy in the request
        dom.ext.Ajax.get(url = s"$host/dev/expense?date=$selectedYear").map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => List(Expense("0", "0", "0", "0","0"))
            case Right(data) => data.message
          }
        })
      }

      def modPartialState(data: Future[List[Expense]]) = {

        // Create group of expenses for same month
        val preGroup = data.map { value =>
            value.map (exp => Expense(exp.Date.substring(0,6),exp.Food,exp.Transport,exp.Utility,exp.Other))
          }

        val group = preGroup.map ( value => value.groupBy( exp => exp.Date) )

        val groupSum = group.map ( value => value.map {exp =>

            val foodOnly = exp._2.map(p => p.Food.toInt)
            var foodSum = foodOnly.fold(0)((a: Int, b: Int) => a + b)

            val transportOnly = exp._2.map(p => p.Transport.toInt)
            var transportSum = transportOnly.fold(0)((a: Int, b: Int) => a + b)

            val utilityOnly = exp._2.map(p => p.Utility.toInt)
            var utilitySum = utilityOnly.fold(0)((a: Int, b: Int) => a + b)

          val otherOnly = exp._2.map(p => p.Other.toInt)
          var otherSum = otherOnly.fold(0)((a: Int, b: Int) => a + b)

          Expense (exp._1,foodSum.toString, transportSum.toString, utilitySum.toString, otherSum.toString)
        })

        Callback.future(

        groupSum.map { value =>
          // Sort the Expense based on date
            val sorted = value.toSeq.sortWith(_.Date < _.Date)
            val xLabel = sorted.map(exp => exp.Date.toString)
            val yFood = sorted.map(exp => exp.Food)
            val yTransport = sorted.map(exp => exp.Transport)
            val yUtility = sorted.map(exp => exp.Utility)
            val yOther = sorted.map(exp => exp.Other)

            AppCircuit.dispatch(ClearLoadingState())

            $.modState(s => s.copy(
              labelMonth = xLabel,
              yearlyFoodExp = yFood.map(x => x.toDouble),
              yearlyTransportExp = yTransport.map(x => x.toDouble),
              yearlyUtilityExp = yUtility.map(x => x.toDouble),
              yearlyOtherExp = yOther.map(x => x.toDouble),
            ))
         }
        )
      }
      modPartialState(getData())
    }

    def onExpenseYearChange(selectedYear: Int): Callback = {
      // Need to pass selectedDay to updateState, as it is not updated within modState
      $.modState(s => s.copy(yearId = selectedYear)) >> updateState(selectedYear)
    }

    def mounted: Callback = {
      Callback.log("Mounted Yearly!")
      //updateState(0)
    }


    def render(props: Props, state: State): VdomElement = {

      <.div(
        Grid(container = true, direction = Grid.Direction.Column,
          justify = Grid.Justify.Center,
          alignItems = Grid.AlignItems.Center)(
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.Center,
            alignItems = Grid.AlignItems.Center)(
            Typography(align = Typography.Align.Center, color = Typography.Color.Primary)("Select Year : "),
            ExpenseYearSelect(ExpenseYearSelect.Props(currentYear, onExpenseYearChange))
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceEvenly,
            //alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4, xs = Grid.Xs._12, md = Grid.Md._6, spacing = Grid.Spacing._24, sm = Grid.Sm._8)(
            Typography(align = Typography.Align.Center, color = Typography.Color.Primary)("Yearly Spend Dashboard"),
            <.div(
              Chart(Chart.Props(
              Chart.BarChart,
              ChartData(
                state.labelMonth.map(x => monthNames(x.substring(4).replace("0","").toInt - 1)),
                Seq(ChartDataset(0, state.yearlyFoodExp, "Food"),
                  ChartDataset(1, state.yearlyTransportExp, "Transport"),
                  ChartDataset(2, state.yearlyUtilityExp, "Utility"),
                  ChartDataset(3, state.yearlyOtherExp, "Other"))
                ))

              ).when(!state.labelMonth.isEmpty),
              <.div(
                Typography(align = Typography.Align.Center, color = Typography.Color.Primary)("No Data available")
                .when(state.labelMonth.isEmpty)
              )
            )
          )
        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("YearlyPage")
    .initialState(State(0,Seq(),Seq(),Seq(),Seq(),Seq()))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
