How to use:
===========
1. Copy the runReport_lib directory to your Fortify_Apps_and_Tools directory
2. Copy the fpr_report.bat file to bin directory inside your Fortify_Apps_and_Tools directory
3. That's it. you open command prompt and enter the command fpr_report

How to build:
============
1. Setup this fortify-reports as a eclipse project (by importing existing eclipse project)
2. Make sure no compilation issues with java 11
3. Visit the build.xml to replace the maven repo dir where you can find extra jars. Most of the jars are from fortify home dir
4. Run the build.xml to build and the runReport_lib dir will be updated with the built artifacts
