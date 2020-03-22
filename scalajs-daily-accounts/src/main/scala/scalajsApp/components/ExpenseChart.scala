package scalajsApp.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

@js.native
trait ChartXAxe extends js.Object {
  def stacked: Boolean = js.native
}
object ChartXAxe {
  def apply(stacked : Boolean): ChartXAxe = {
    js.Dynamic.literal(
      stacked = stacked,
    ).asInstanceOf[ChartXAxe]
  }
}

@js.native
trait ChartYAxe extends js.Object {
  def stacked: Boolean = js.native
}
object ChartYAxe {
  def apply(stacked : Boolean): ChartYAxe = {
    js.Dynamic.literal(
      stacked = stacked,
    ).asInstanceOf[ChartYAxe]
  }
}

@js.native
trait ChartScales extends js.Object {
  def xAxes: js.Array[ChartXAxe] = js.native
  def yAxes: js.Array[ChartYAxe] = js.native
}
object ChartScales {
  def apply(xAxes: js.Array[ChartXAxe], yAxes: js.Array[ChartYAxe]): ChartScales = {
    js.Dynamic.literal(
      xAxes = xAxes,
      yAxes = yAxes,
    ).asInstanceOf[ChartScales]
  }
}

@js.native
trait ChartDataset extends js.Object {
  def label: String = js.native
  def data: js.Array[Double] = js.native
  def fillColor: String = js.native
  def strokeColor: String = js.native
}


object ChartDataset {
  def apply(index : Int,
            data: Seq[Double],
            label: String, backgroundColor: String = "#8080FF", borderColor: String = "#404080"): ChartDataset = {
    val backgroundColorList = List("#F7464A","#E2EAE9","#D4CCC5","#949FB1","#4D5360")
    js.Dynamic.literal(
      label = label,
      data = data.toJSArray,
      backgroundColor = backgroundColorList(index),
      borderColor = borderColor
    ).asInstanceOf[ChartDataset]
  }
}

@js.native
trait ChartData extends js.Object {
  def labels: js.Array[String] = js.native
  def datasets: js.Array[ChartDataset] = js.native
}

object ChartData {
  def apply(labels: Seq[String], datasets: Seq[ChartDataset]): ChartData = {
    js.Dynamic.literal(
      labels = labels.toJSArray,
      datasets = datasets.toJSArray
    ).asInstanceOf[ChartData]
  }
}

@js.native
trait ChartOptions extends js.Object {
  def responsive: Boolean = js.native
  def scales : ChartScales = js.native
}

object ChartOptions {
  def apply(scales: ChartScales, responsive: Boolean = true): ChartOptions = {
    js.Dynamic.literal(
      scales = scales,
      responsive = responsive
    ).asInstanceOf[ChartOptions]
  }
}

@js.native
trait ChartConfiguration extends js.Object {
  def `type`: String = js.native
  def data: ChartData = js.native
  def options: ChartOptions = js.native
}

object ChartConfiguration {
  def apply(`type`: String, data: ChartData, options: ChartOptions = ChartOptions(scales =
    ChartScales(xAxes = js.Array(ChartXAxe(stacked = true)), yAxes = js.Array(ChartYAxe(stacked = true)))
    ,responsive = false)): ChartConfiguration = {
    js.Dynamic.literal(
      `type` = `type`,
      data = data,
      options = options
    ).asInstanceOf[ChartConfiguration]
  }
}

// define a class to access the Chart.js component
@js.native
@JSImport("chart.js","Chart")
class JSChart(ctx: js.Dynamic, config: ChartConfiguration) extends js.Object
{
  def update(): Unit = js.native
  def destroy(): Unit = js.native
  def clear(): Unit = js.native
  def config: ChartConfiguration = js.native
}

object Chart {
  // available chart styles
  sealed trait ChartStyle

  case object LineChart extends ChartStyle
  case object BarChart extends ChartStyle

  case class State(
                    var chart: JSChart
                  )

  case class Props(
                    style: ChartStyle, data: ChartData,
                    width: Int = 500, height: Int = 300
                  )

  class Backend($: BackendScope[Props, State]) {

    def  chartType = $.props.map(p => p.style).runNow() match {
      case Chart.BarChart => "bar"
      case Chart.LineChart => "line"
      case _ => "line"

    }

    def mounted: Callback = {
      $.getDOMNode.map { dom =>
        val canvas = dom.toElement.get.asInstanceOf[HTMLCanvasElement]
        val chart = new JSChart(canvas.getContext("2d"), ChartConfiguration(chartType,$.props.map(_.data).runNow()))
        chart
      } >>= (c => $.setState(State(c)))
    }

    def update(np: Props): Callback = {
      for( c <- $.state.map(s=> s.chart))
        yield {
          c.clear()
          for( i <- 0 until c.config.data.labels.length) c.config.data.labels.pop()
          np.data.labels.foreach( newLab => c.config.data.labels.push(newLab))
          for( i <- 0 until c.config.data.datasets.length) c.config.data.datasets.pop()
          np.data.datasets.foreach( newData => c.config.data.datasets.push(newData))

          //c.destroy()

        c.update()
      }
    }

    def render(props: Props): VdomElement = {
      <.canvas(VdomAttr("width") := props.width, VdomAttr("height") := props.height)

    }
  }


  val Component = ScalaComponent.builder[Props]("Chart")
    .initialState(State(null))
     // new JSChart.(null, ChartConfiguration("bar", ChartData(Seq(""),Seq(ChartDataset(0,Seq(0),"")))))))
    .renderBackend[Backend]
    .componentWillReceiveProps(scope => scope.backend.update(scope.nextProps))
    .componentDidMount(_.backend.mounted)
    .build

  def apply(props: Props) = Component(props)

}



