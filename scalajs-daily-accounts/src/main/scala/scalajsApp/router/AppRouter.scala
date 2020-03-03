package scalajsApp.router

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import scalajsApp.components.Layout
import scalajsApp.pages.{ExpenditurePanel,CurrentMonthPanel}


object AppRouter {
  sealed trait Page
  case object LastMonthExpenses extends Page
  case object Expenditure extends Page
  case object CurrentMonthExpenses extends Page


  //val connection = AppCircuit.connect(_.state)

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._
    (trimSlashes
      | staticRoute(root, Expenditure) ~> renderR(renderExpenditurePage)
      | staticRoute("#currentmonth", CurrentMonthExpenses) ~> renderR(renderCurrentMonthPage)
    )
      .notFound(redirectToPage(Expenditure)(Redirect.Replace))
      .renderWith(layout)
  }

  def renderExpenditurePage(ctl: RouterCtl[Page]) = {
    ExpenditurePanel(ExpenditurePanel.Props(ctl))
  }

  def renderCurrentMonthPage(ctl: RouterCtl[Page]) = {
    CurrentMonthPanel(CurrentMonthPanel.Props(ctl))
  }

  def layout (c: RouterCtl[Page], r: Resolution[Page]) = Layout(Layout.Props(c,r))

  val baseUrl = BaseUrl.fromWindowOrigin_/

  val router = Router(baseUrl, routerConfig.logToConsole)
}
