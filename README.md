## Analogical Modeling Weka Plugin

This project is a Weka plugin containing state-of-the-art algorithms for classification using [analogical modeling](https://en.wikipedia.org/wiki/Analogical_modeling).

* [Issue Tracker](https://github.com/garfieldnate/Weka_AnalogicalModeling/issues)
* [Repository](https://github.com/garfieldnate/Weka_AnalogicalModeling)
* [Documentation](http://garfieldnate.github.io/Weka_AnalogicalModeling/)

### Installation and Use in Weka

1. Download Weka. You need at least 3.7.2 to use the package manager. You can download it here: http://www.cs.waikato.ac.nz/ml/weka/

2. Start up Weka, and in the initial scren ("GUI Chooser") go to the tools menu and select "Package Manager". You'll see the screen below. Select "AnalogicalModeling" and click "Install".

![weka package manager screen](https://user-images.githubusercontent.com/778453/50684191-e9d5bd00-1014-11e9-9b06-b79a53e432d2.png)

3. Close the package manager and click on the "Experimenter" button in the GUI Chooser window. In the "Preprocess" tab, your arff file. If you need an example file, try `data/ch3example.arff` from this repository. (This contains a toy example from chapter 3 of Royall Skousen's _Analogical Modeling of Language_).

4. In the "Classify" tab, click "Choose" and select the AnalogicalModeling classifier from the "lazy" package. Screen shot below:

![weka classifiers screen](https://user-images.githubusercontent.com/778453/50684348-839d6a00-1015-11e9-8b3e-58e0f75f1072.png)

5. Under "Test options", select "Supplied test set" and open the arff file containing your test set. If you used `data/ch2example.arff` earlier, you can use `data/ch3exampleTest.arff` here.

6. Click the "More options..." button, then the "Choose" button labeled "Output predictions". From there, select the AnalogicalModelingOutput class (requiring this was the only kludge I could get to work for custom output). Screenshot below:

![weka classifier evaluation options screen](https://user-images.githubusercontent.com/778453/50684510-1b9b5380-1016-11e9-85e5-30a4b3bfdb2d.png)

7. Back on the "Classify" tab again, click "Start". If you used the chapter 3 data, the results should appear as in the below screenshot:

![weka classifier screen with analogical modeling classifier output](https://user-images.githubusercontent.com/778453/50684600-6f0da180-1016-11e9-9238-6bb4c1318fd0.png)

### About Analogical Modeling

Analogical Modeling (or AM) was developed as an exemplar-based approach to modeling language usage, and has also been found useful in modeling other "sticky" phenomena. AM is especially suited to this because it predicts probabilistic occurrences instead of assigning static labels for instances.

AM was not designed to be a classifier, but as a cognitive theory explaining variation in human behavior. As such, though in practice it is often used like any other machine learning classifier, there are fine theoretical points in which it differs. As a theory of human behavior, much of the value in its predictions lies in matching observed human behavior, including non-determinism and degradations in accuracy caused by paucity of data.

The AM algorithm could be called a probabilistic, instance-based classifier. However, the probabilities given for each classification are not degrees of certainty, but actual probabilities of occurring in real usage. AM models "sticky" phenomena as being intrinsically sticky, not as deterministic phenomena that just require more data to be predicted perfectly.

Though it is possible to choose an outcome probabilistically, in practice users are generally interested in either the full predicted probability distribution or the outcome with the highest probability.

AM practitioners generally use terminology taken from statistics, most of which has equivalent terminology used by computer scientists (and most machine learning frameworks in general). Examples are 'exemplar' (training instance), 'outcome' (class label), and 'variable' (feature). This software uses the CS terminology internally, but user-facing reports use the AM terminology.

The running time for analogical modeling is exponential in nature and practice, and thus it is not suitable for very large datasets; exact calculation becomes impractical after about 50 features. Therefore, this tool will automatically use an approximation algorithm when there are 50 or more features.

### Features

As an evolving project, the most important design principle has been modularity and ease of experimentation with core algorithms. As such, the system is able to adapt for data of different cardinalities:

* Context labels scale up from `int`s to `long`s and `BigInteger`s
* Very small vectors are placed in a single lattice
* Larger vectors are placed in a distributed lattice, with the number of lattices increasing with size
* Very large vectors (50 or more features) are classified approximately using Monte Carlo simulation

Some algorithmic improvements have been made to the distributed lattice and approximate lattice filling algorithms. Concurrency is also used extensively so that 8 CPU cores will fill lattices roughly 8 times faster, etc.

### Building

This project is managed with [Gradle](https://gradle.org/). The following build commands are then available:

    # compile Java source into a jar
    ./gradlew build
    # run unit tests
    ./gradlew test
    # generate HTML documentation
    ./gradlew javadoc
    # build the project archive for release as a Weka plugin
    ./gradlew dist
    # bump the minor version number (update Description.props, git commit/tag/push)
    ./gradlew bumpMinor

### Releasing

To release a new version of the plugin:

* run `gradle bumpMinor`
* create a new release on GitHub corresponding to the new tag
* upload the distribution to the GitHub release
* send the new Description.props file to Mark Hall <mhall at pentaho.com>

### Running

Under construction; try testing AnalogicalModeling.java with `-t data/ch3example.arff -x 5`.

### License

Released under the Apache 2.0 license (see the LICENSE file for details). Copyright Nathan Glenn, 2014.

### See Also
https://metacpan.org/pod/Algorithm::AM
