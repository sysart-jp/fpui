ThisBuild / name := "functional-ui"
ThisBuild / scalaVersion := "2.13.2"
lazy val circeVersion = "0.13.0"
lazy val CatsEffectVersion = "2.3.1"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "me.shadaj" %%% "slinky-web" % "0.6.5",
    "me.shadaj" %%% "slinky-hot" % "0.6.5",
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-generic" % circeVersion,
    "io.circe" %%% "circe-parser" % circeVersion,
    "org.scalatest" %%% "scalatest" % "3.1.1" % Test,
    "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "16.13.1",
    "react-dom" -> "16.13.1",
    "react-proxy" -> "1.1.8"
  ),
  npmDevDependencies in Compile ++= Seq(
    "file-loader" -> "6.0.0",
    "style-loader" -> "1.2.1",
    "css-loader" -> "3.5.3",
    "html-webpack-plugin" -> "4.3.0",
    "copy-webpack-plugin" -> "5.1.1",
    "webpack-merge" -> "4.2.2"
  ),
  scalacOptions += "-Ymacro-annotations",
  version in webpack := "4.43.0",
  version in startWebpackDevServer := "3.11.0",
  scalaJSUseMainModuleInitializer := true,
  webpackResources := baseDirectory.value / "webpack" * "*",
  webpackConfigFile in fastOptJS := Some(
    baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
  ),
  webpackConfigFile in fullOptJS := Some(
    baseDirectory.value / "webpack" / "webpack-opt.config.js"
  ),
  webpackConfigFile in Test := Some(
    baseDirectory.value / "webpack" / "webpack-core.config.js"
  ),
  webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
  webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
  requireJsDomEnv in Test := true
)

lazy val concept = project
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)

addCommandAlias(
  "conceptDev",
  ";concept/fastOptJS::startWebpackDevServer;~concept/fastOptJS"
)

lazy val exampleTodo = (project in file("examples/todo"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .dependsOn(concept)

addCommandAlias(
  "todoDev",
  ";exampleTodo/fastOptJS::startWebpackDevServer;~exampleTodo/fastOptJS"
)

lazy val exampleMultipage = (project in file("examples/multipage"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "tech.sparse" %%% "trail" % "0.3.0"
    )
  )
  .dependsOn(concept)

addCommandAlias(
  "multipageDev",
  ";exampleMultipage/fastOptJS::startWebpackDevServer;~exampleMultipage/fastOptJS"
)
