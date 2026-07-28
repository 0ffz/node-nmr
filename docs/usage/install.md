# Installation

Install Java 25 or later, ex. from [here](https://adoptium.net/).

Download the latest jar from our GitHub releases. On Windows this can be run by double-clicking if Java is installed, on other platforms run with

```bash
java -jar path/to/app.jar
```

## Linux

[OpenBLAS](https://github.com/OpenMathLib/OpenBLAS) needs to be installed for Cadzow filtering to work, currently we only bundle the dependency directly on Windows.

Ex. on Fedora use

```bash
sudo dnf install openblas-devel
```