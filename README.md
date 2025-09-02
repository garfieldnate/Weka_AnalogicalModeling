# Analogical Modeling Weka Plugin

State-of-the-art [analogical modeling](https://en.wikipedia.org/wiki/Analogical_modeling) plugin for [Weka](https://www.cs.waikato.ac.nz/ml/weka/).

* [Issue Tracker](https://github.com/garfieldnate/Weka_AnalogicalModeling/issues)
* [Repository](https://github.com/garfieldnate/Weka_AnalogicalModeling)
* [Documentation](http://garfieldnate.github.io/Weka_AnalogicalModeling/)

[![Build](https://github.com/garfieldnate/Weka_AnalogicalModeling/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/garfieldnate/Weka_AnalogicalModeling/actions/workflows/build.yml?query=branch%3Amaster)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Installation and Use in Weka

1. Download Weka. You need at least 3.8.5 to use this package. You can download Weka here: http://www.cs.waikato.ac.nz/ml/weka/

2. Start up Weka, and in the initial screen ("GUI Chooser") go to the tools menu and select "Package Manager". You'll
   see the screen below. Select "AnalogicalModeling" and click "Install".

![Weka package manager screen](https://user-images.githubusercontent.com/778453/50684191-e9d5bd00-1014-11e9-9b06-b79a53e432d2.png)

3. Close the package manager and click on the "Experimenter" button in the GUI Chooser window. In the "Preprocess" tab,
   open your arff file. If you need an example file, try `data/ch3example.arff` from this repository. (This contains a
   toy example from chapter 3 of Royall Skousen's _Analogical Modeling of Language_).

4. Analogical modeling can only work with nominal data, so if your dataset contains other types of data (e.g. numeric),
   you'll need to pre-process it. For example, to discretize a continuous numeric attribute into bucketed nominal attributes,
   in the "Preprocess" tab you can add the following filter: `filters.unsupervised.attribute.Discretize`. More information
   on this filter is available via the [Weka MOOC](https://www.youtube.com/watch?v=aDMzPC5IO4c&ab_channel=WekaMOOC).
   Screenshot below:

![adding the discretize filter in Weka](https://user-images.githubusercontent.com/778453/115677983-50e81e80-a351-11eb-9f76-9cc11e78c42f.png)

5. In the "Classify" tab, click "Choose" and select the AnalogicalModeling classifier from the "lazy" package.
   Screenshot below:

![Weka classifiers screen](https://user-images.githubusercontent.com/778453/50684348-839d6a00-1015-11e9-8b3e-58e0f75f1072.png)

6. Under "Test options", select "Supplied test set" and open the arff file containing your test set. If you used
   `data/ch2example.arff` earlier, you can use `data/ch3exampleTest.arff` here.

7. Click the "More options..." button, then the "Choose" button labeled "Output predictions". From there, select
   AnalogicalModelingOutput. Please note that this output option can ONLY be used with the Analogical Modeling
   classifier; If you switch to another classifier, you will also need to change this field. Screenshot below:

![Weka classifier evaluation options screen](https://user-images.githubusercontent.com/778453/50684510-1b9b5380-1016-11e9-85e5-30a4b3bfdb2d.png)

8. Click on the `AnalogicalModelingOutput` text that appeared in the field next to the "Choose" button. From here, you
   can configure what information you want printed, including analogical sets and gang effects, as well as the desired
   output format. You can also choose to suppress the output in the window and write it to a file instead. Screenshot below:

![Analogical Modeling Output configuration](https://github.com/user-attachments/assets/8d184dfb-b61e-4d04-b0f3-80aae76d521c)

9. Back on the "Classify" tab again, click "Start". If you used the chapter 3 data and enabled output for analogical
   sets and gang effects, the results should appear as in the below screenshot:

![Weka classifier screen with analogical modeling classifier output](https://user-images.githubusercontent.com/778453/115674201-b2a68980-a34d-11eb-849b-a39c62067d81.png)

## About Analogical Modeling

Analogical Modeling (or AM) was developed as an exemplar-based approach to modeling language usage, and has also been
found useful in modeling other "sticky" phenomena. AM is especially suited to this because it predicts probabilistic
occurrences instead of assigning static labels for instances.

AM was not designed to be a classifier, but as a cognitive theory explaining variation in human behavior. As such,
though in practice it is often used like any other machine learning classifier, there are fine theoretical points in
which it differs. As a theory of human behavior, much of the value in its predictions lies in matching observed human
behavior, including non-determinism and degradations in accuracy caused by paucity of data.

The AM algorithm could be called a probabilistic, instance-based classifier. However, the probabilities given for each
classification are not degrees of certainty, but actual probabilities of occurring in real usage. AM models "sticky"
phenomena as being intrinsically sticky, not as deterministic phenomena that just require more data to be predicted
perfectly.

Though it is possible to choose an outcome probabilistically, in practice users are generally interested in either the
full predicted probability distribution or the outcome with the highest probability.

AM practitioners generally use terminology taken from statistics, most of which has equivalent terminology used by
computer scientists (and most machine learning frameworks in general). Examples are 'exemplar' (training instance),
'outcome' (class label), and 'variable' (feature). This software uses the CS terminology internally, but user-facing
reports use the AM terminology.

The running time for analogical modeling is exponential in the number of features (variables); exact calculation becomes
impractical after about 50 features. Therefore, this tool will automatically use an approximation algorithm when there
are 50 or more features.

## Features

As an evolving project, the most important design principle has been modularity and ease of experimentation with core
algorithms. As such, the system is able to adapt for data of different cardinalities:

* Context labels scale up from `int`s to `long`s and `BigInteger`s
* Very small vectors are placed in a single lattice
* Larger vectors are placed in a distributed lattice, with the number of lattices increasing with size
* Very large vectors (50 or more features) are classified approximately using Monte Carlo simulation

Some algorithmic improvements have been made to the distributed lattice and approximate lattice filling algorithms.
Concurrency is also used extensively so that 8 CPU cores will fill lattices roughly 8 times faster, etc.

## Development

The project JavaDoc is uploaded to GitHub pages automatically via a GitHub Action.
[Browse here](http://garfieldnate.github.io/Weka_AnalogicalModeling/).

An additional GitHub Action builds and tests the project for every branch and pull request, so contributors should get
feedback quickly if a change breaks anything.

### Prerequisites for First-Time Java Developers

#### Understanding Java Project Structure

For developers new to Java, here's what the key directories mean:

- `src/main/java/` - Your Java source code files (.java)
- `src/test/java/` - Unit test files
- `src/main/resources/` - Non-code resources (config files, etc.)
- `build/` - Generated files (compiled code, reports) - don't edit these
- `gradle/` - Gradle wrapper files
- `build.gradle.kts` - Project configuration and dependencies (like package.json in Node.js)

#### Common Java Development Terms

- **JDK (Java Development Kit)**: Tools for developing Java applications (includes compiler)
- **JVM (Java Virtual Machine)**: Runs compiled Java code
- **Classpath**: Where Java looks for compiled code and libraries
- **JAR file**: Java Archive - packaged Java application (like a .zip with compiled code)
- **Gradle**: Build tool that manages dependencies and compilation (like npm/yarn for JavaScript)

### Developing with IntelliJ IDEA (Recommended)

#### Step 1: Install Java 11

**Using SDKMan! (Recommended)**
1. Install SDKMan!:
   ```bash
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   ```

2. Install Java 11:
   ```bash
   sdk install java 11.0.25-tem
   sdk use java 11.0.25-tem
   ```

**Direct Download Alternative**
- Download Eclipse Temurin 11 from [Adoptium](https://adoptium.net/temurin/releases/?version=11)
- Follow the installer for your OS

#### Step 2: Install and Configure IntelliJ IDEA

1. Download IntelliJ IDEA Community Edition (free) from [JetBrains](https://www.jetbrains.com/idea/download/)

2. Open the project:
   - Launch IntelliJ IDEA
   - Click "Open" on the welcome screen
   - Navigate to this project folder and select `build.gradle.kts`

3. Configure the JDK:
   - Go to File → Project Structure (Cmd+; on Mac, Ctrl+Alt+Shift+S on Windows/Linux)
   - Under "Project", set SDK to Java 11
   - If not listed, click "Add SDK" → "JDK" and browse to your Java 11 installation

4. Wait for Gradle sync:
   - IntelliJ will automatically download dependencies (first time takes a few minutes)
   - Look for the progress bar at the bottom of the window

#### Step 3: Working in IntelliJ

**Running the build:**
- Open the Gradle panel (View → Tool Windows → Gradle)
- Navigate to Tasks → build → build
- Double-click to run

**Running tests:**
- To run all tests: In Gradle panel, Tasks → verification → test
- To run a single test: Open the test file, click the green arrow next to the test method
- Test results appear in the Run panel at the bottom

**Debugging:**
1. Set breakpoints by clicking in the left margin of any Java file
2. Right-click a test and select "Debug"
3. Use the Debug panel to step through code and inspect variables

**Common IntelliJ shortcuts:**
- Cmd+Shift+F (Mac) / Ctrl+Shift+F (Win/Linux): Search across all files
- Cmd+Click (Mac) / Ctrl+Click (Win/Linux): Go to definition
- Shift+F6: Rename variable/method everywhere
- Alt+Enter: Show quick fixes for errors

### Option 2: Developing from the Command Line

#### Step 1: Install Java 11

**Using SDKMan!**
```bash
# Install SDKMan
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install and use Java 11
sdk install java 11.0.25-tem
sdk use java 11.0.25-tem

# Verify installation
java -version  # Should show openjdk 11.0.25 or similar
```

**Manual Installation**
1. Download Eclipse Temurin 11 from [Adoptium](https://adoptium.net/temurin/releases/?version=11)
2. Set environment variables:
   ```bash
   # Add to ~/.bashrc or ~/.zshrc
   export JAVA_HOME=/path/to/jdk-11
   export PATH=$JAVA_HOME/bin:$PATH
   ```
3. Reload your shell configuration:
   ```bash
   source ~/.bashrc  # or source ~/.zshrc
   ```

#### Step 2: Building and Testing

This project uses Gradle with an included wrapper, so you don't need to install Gradle separately.

**Available commands:**

```bash
# Build and test the project
./gradlew build

# Run only unit tests
./gradlew test

# Generate JavaDoc documentation
./gradlew javadoc

# Build the Weka plugin package
./gradlew weka_package

# Clean build artifacts
./gradlew clean

# Run with verbose output for debugging
./gradlew build --info
```

**Note for Windows:** Use `gradlew.bat` instead of `./gradlew`

#### Step 3: Viewing Results

After building:
- Compiled classes: `build/classes/java/main/`
- JAR files: `build/libs/`
- Test reports: `build/reports/tests/test/index.html` (open in browser; unnecessary, though, because results are shown in `./gradlew test` output)
- JavaDoc: `build/docs/javadoc/index.html` (open in browser)

#### Troubleshooting Command Line Builds

If the build fails:

1. Check Java version:
   ```bash
   java -version  # Should be version 11
   ./gradlew --version  # Should show JVM version 11
   ```

2. Clear Gradle cache and retry:
   ```bash
   ./gradlew clean build --no-build-cache
   ```

3. Run with more details:
   ```bash
   ./gradlew build --stacktrace --info
   ```

4. Common issues:
   - Wrong Java version: Use `sdk use java 11.0.25-tem` or check JAVA_HOME
   - Permission denied: Run `chmod +x gradlew`
   - Out of memory: Set `export GRADLE_OPTS="-Xmx2g"`


### Releasing

To release a new version of the plugin:
* Update and commit Description.props
    * version number is in several locations
    * date
* Create and push a new git tag with the next version number
* run `./gradlew weka_package`, and upload the resulting artifact (distributions/Weka_AnalogicalModeling-X.Y.Z.zip) to
  the GitHub release
* send the new Description.props file to Mark Hall <mhall at waikato ac nz>

### Running in the Terminal

Under construction; try testing AnalogicalModeling.java with `-t data/ch3example.arff -x 5`.

## License

Released under the Apache 2.0 license (see the LICENSE file for details). Copyright Nathan Glenn, 2021.

## See Also
https://metacpan.org/pod/Algorithm::AM
