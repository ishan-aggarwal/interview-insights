# interview-insights

Questions, experiences, and interview learnings

## Build

This project uses [Apache Maven](https://maven.apache.org/).

```bash
mvn verify
mvn -q exec:java -Dexec.mainClass=com.interviewinsights.Application
```

Java **21** is the compile target (`maven.compiler.release` in `pom.xml`). Use any JDK that supports release 21 or newer.

### IntelliJ IDEA

1. **File → Reload All from Disk**
2. **Maven** tool window → **Reload All Maven Projects** (links dependencies and syncs the POM)
3. If build still fails: **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner** → uncheck **Delegate IDE build/run actions to Maven** (the project is also set to use the IDE compiler in `.idea/misc.xml`).

Run `Application` from the gutter next to `main`, or the **Application** run configuration.
