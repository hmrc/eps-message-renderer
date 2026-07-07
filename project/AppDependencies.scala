import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val playVersion = "play-30"
  private val bootstrapVersion = "10.7.0"
  private val domainVersion = "13.0.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"domain-$playVersion"            % domainVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "mockito-4-11"                 % "3.2.17.0"       % Test,
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion" % bootstrapVersion % Test,
    "uk.gov.hmrc"       %% s"domain-test-$playVersion"    % domainVersion    % Test
  )
  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"domain-test-$playVersion" % domainVersion % "it/test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
