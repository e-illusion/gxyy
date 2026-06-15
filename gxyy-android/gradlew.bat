@echo off
set DIRNAME=%~dp0
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
"%JAVA_HOME%\bin\java.exe" -classpath "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
