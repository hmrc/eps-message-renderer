import sbt.Keys.*
import sbt.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.*
import DefaultBuildSettings.*
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "eps-message-renderer"

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := "3.3.6"

lazy val plugins: Seq[Plugins] = Seq(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)

val excludedPackages: Seq[String] = Seq(
  "<empty>",
  "Reverse.*",
  ".*Routes.*",
  ".*\\$anon.*",
  "testOnlyDoNotUseInAppConf.*"
)

lazy val scoverageSettings = {
  import scoverage.*
  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(","),
    ScoverageKeys.coverageMinimumStmtTotal := 68.30,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins *)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scoverageSettings.settings *)
  .settings(defaultSettings() *)
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    PlayKeys.playDefaultPort := 9419,
    Test / parallelExecution := false,
    Test / fork := false
  )
  .settings(
    scalacOptions ++= List(
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:src=routes/.*:s"
    )
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.it
  )

Test / test := (Test / test)
  .dependsOn(scalafmtCheckAll)
  .value

it / test := (it / Test / test)
  .dependsOn(scalafmtCheckAll, it / scalafmtCheckAll)
  .value
