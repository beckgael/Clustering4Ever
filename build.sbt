import sbt._
import Keys._
//import com.typesafe.sbt.SbtGit.GitKeys._

val sparkVersion = "2.2.0"

lazy val mergeStrategyC4E = assemblyMergeStrategy in assembly := {
	case PathList("org", "xmlpull", xs @ _*) => MergeStrategy.last
	case PathList("META-INF", "io.netty", xs @ _*) => MergeStrategy.last
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.last
    case x => val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
	}

lazy val sparkDeps = libraryDependencies ++= Seq(
	   	"org.apache.spark" %% "spark-core" % sparkVersion % "provided",
		"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
		"org.apache.spark"  %% "spark-mllib"  % sparkVersion % "provided"
//		"org.scalaz" %% "scalaz-core" % "7.2.18"
	)

lazy val commonCredentialsAndResolvers = Seq(
		resolvers += Resolver.sonatypeRepo("releases"),
		resolvers += "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven",
		resolvers += "Sbt plugins" at "https://dl.bintray.com/sbt/sbt-plugin-releases",
		resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
		credentials += Credentials(Path.userHome / ".sbt" / "credentials"),
		assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheUnzip = true),
		assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
		)

lazy val commonSettingsC4E = Seq(
		organization := "clustering4ever",
		bintrayRepository := "Clustering4Ever",
	 	version := "0.2.3",
		scalaVersion := "2.11.8",
		autoAPIMappings := true,
		licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
		bintrayOrganization := Some("clustering4ever"),
		credentials += Credentials(Path.userHome / ".bintray" / ".credentials")
	)

lazy val core = (project in file("core"))
	.settings(commonSettingsC4E:_*)
	.settings(mergeStrategyC4E)

lazy val clusteringScala = (project in file("clustering/scala"))
	.settings(commonSettingsC4E:_*)
	.settings(mergeStrategyC4E)
	.dependsOn(core)

lazy val clusteringSpark = (project in file("clustering/spark"))
	.settings(commonSettingsC4E:_*)
	.settings(mergeStrategyC4E)
	.settings(
		sparkDeps
	)
	.dependsOn(core, clusteringScala)

lazy val documentation = (project in file("Documentation"))
	.settings(commonSettingsC4E: _*)
  	.settings( name := "documentation" )
	.enablePlugins(ScalaUnidocPlugin)
	.aggregate(core, clusteringScala, clusteringSpark)

lazy val clustering4ever = (project in file("Clustering4Ever"))
	.settings(commonSettingsC4E: _*)
  	.settings(
  		name := "Clustering4Ever"
  	)
	.aggregate(core, clusteringScala, clusteringSpark)
	.dependsOn(core, clusteringScala, clusteringSpark)
	//.enablePlugins(ScalaUnidocPlugin)
