sourceSets {
  val test by getting {
    java.srcDir("src/test/kotlin/")
  }
}

dependencies {
  implementation(Dependencies.kotlinStdLibJdk8)
  implementation(Dependencies.kotlinReflection)
  implementation(Dependencies.moshiCore)
  implementation(Dependencies.moshiKotlin)
  implementation(Dependencies.moshiAdapters)
  api(project(":wisp-feature"))

  testImplementation(Dependencies.assertj)
  testImplementation(Dependencies.kotlinTest)
}

afterEvaluate {
  project.tasks.dokka {
    outputDirectory = "$rootDir/docs/0.x"
    outputFormat = "gfm"
  }
}

apply(from = "$rootDir/gradle-mvn-publish.gradle")
