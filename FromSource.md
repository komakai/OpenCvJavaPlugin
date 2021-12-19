Running and building from source
================================

### Prepare IntelliJ
1. Download and install IntelliJ
2. Enable the Gradle plugin
3. Install and enable the "Plugin DevKit" plugin

### Get the source
4. `git clone https://github.com/komakai/OpenCvJavaPlugin.git`

### Import the project
5. Import the project: *"File" -> "New" -> "Project from Existing Sources"* and select the `build.gradle` file from the project.
    1. In the import dialog select "_**Use gradle wrapper task configuration**_"
    2. Wait while Gradle and the [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin) work their magic. This can take a while..
6. Open _gradle.properties_ and make sure _isAndroidStudio_ is set to **false**: `isAndroidStudio=false`

### Running
To run IntelliJ with the plugin enabled from sources, start the "RunIde" gradle task: open the Gradle window, choose "Tasks" -> "intellij" and double click on "RunIde".

### Building
Make sure the plugin works when you run it as described above.<br>
To build the plugin, run the "buildPlugin" gradle task. This creates a zip file in the "build/distributions" directory. You can install the plugin in IntelliJ or 
Android Studio by choosing "Install plugin from disk" in the Plugin Settings.

