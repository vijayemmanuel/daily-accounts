
enablePlugins(ScalaJSPlugin)
//enablePlugins(WorkbenchPlugin)
enablePlugins(ScalaJSBundlerPlugin)

name := "scalajs-daily-accounts"

version := "0.1"

scalaVersion := "2.12.4"



// This is an application with a main method
scalaJSUseMainModuleInitializer := true
scalaJSModuleKind := ModuleKind.CommonJSModule

emitSourceMaps := false


libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.8"
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.6.0"
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "1.6.0"
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "1.6.0"
libraryDependencies += "io.circe" %%% "circe-core" % "0.8.0"
libraryDependencies += "io.circe" %%% "circe-parser" % "0.8.0"
libraryDependencies += "io.circe" %%% "circe-generic" % "0.8.0"
libraryDependencies += "io.circe" %%% "circe-generic-extras" % "0.8.0"

libraryDependencies += "org.rebeam" %%% "scalajs-react-material-ui" % "0.0.1-SNAPSHOT"

npmDependencies in Compile ++= Seq(
  "react" -> "16.8.0",
  "react-dom" -> "16.8.0",
  "@material-ui/core" -> "3.9.3",
  "@material-ui/styles" -> "3.0.0-alpha.10",
  "@material-ui/icons" -> "3.0.2"
  //"material-ui-pickers" -> "2.2.4"
)