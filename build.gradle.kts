import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar
import java.util.Properties

plugins {
    java
    id("com.adarshr.test-logger") version "2.1.1"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val versionFromDescriptionProps: String by lazy {
    val descriptionProps = Properties()
    file("Description.props").inputStream().use { descriptionProps.load(it) }
    descriptionProps.getProperty("Version")
}

version = versionFromDescriptionProps

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

val copyPropsToResources by tasks.registering(Copy::class) {
    from("Description.props")
    into("src/main/resources/weka/classifiers/lazy/AM")
    doLast {
        logger.lifecycle("Copied Description.props to src/main/resources/")
    }
}

tasks.named("processResources") {
    dependsOn(copyPropsToResources)
}

tasks.withType<JavaCompile> {
    dependsOn(copyPropsToResources)
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:19.0")
    implementation("com.jakewharton.picnic:picnic:0.5.0")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("nz.ac.waikato.cms.weka:weka-dev:3.9.5")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.mockito:mockito-core:5.17.0")
    testImplementation("nz.ac.waikato.cms.weka:weka-dev:3.9.5") {
        artifact {
            name = "weka-dev"
            classifier = "tests"
            type = "jar"
        }
    }
}

tasks.withType<Test> {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    testLogging {
        events = setOf(TestLogEvent.FAILED)
        showStandardStreams = true
    }
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}

tasks.named<Javadoc>("javadoc") {
    source = sourceSets["main"].allJava
    classpath = sourceSets["main"].compileClasspath

    options {
        (this as StandardJavadocDocletOptions).apply {
            addBooleanOption("-allow-script-in-comments", true)
            header = """
            <script>
                MathJax = {
                tex: {
                    inlineMath: [['$', '$'], ['\\(', '\\)']]
                },
                svg: {
                    fontCache: 'global'
                }
                };
            </script>
            <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
            """.trimIndent()
            links("http://weka.sourceforge.net/doc.dev/")
        }
    }
}

tasks.register<Zip>("weka_package") {
    from(tasks.named("shadowJar").get().outputs.files)
    from(tasks.named("javadoc").get().outputs.files) {
        into("doc")
    }
    from(projectDir) {
        include(
            "src/**/*",
            "data/**/*",
            "Description.props",
            "LICENSE",
            "NOTICE",
            "README.mkdn",
            "todo.txt"
        )
    }
}
