@echo off

rem Copyright 2004 - 2024 Open Text.
rem
rem The only warranties for products and services of Open Text and its
rem affiliates and licensors ("Open Text") are as may be set forth in the
rem express warranty statements accompanying such products and services.
rem Nothing herein should be construed as constituting an additional warranty.
rem Open Text shall not be liable for technical or editorial errors or
rem omissions contained herein. The information contained herein is subject
rem to change without notice.
rem
rem Except as specifically indicated otherwise, this document contains
rem confidential information and a valid license is required for possession,
rem use or copying. If this work is provided to the U.S. Government, consistent
rem with FAR 12.211 and 12.212, Commercial Computer Software, Computer Software
rem Documentation, and Technical Data for Commercial Items are licensed to the
rem U.S. Government under vendor's standard commercial license.

set FORTIFY_HOME=%~dp0..

set JAVA_CMD=%FORTIFY_HOME%\jre\bin\java.exe

if not exist "%JAVA_CMD%" set JAVA_CMD=java.exe

:: Parse out the Java args that start with -X or -D
set USER_VM_OPTS=
set USER_OPTS=
shift
:moreargs
if x%0x == xx goto donewithargs
set arg=%0
set argnq=%arg:"=%
if %argnq:~0,2% == -X goto uservmargs
if %argnq:~0,2% == -D goto uservmargs
set USER_OPTS=%USER_OPTS% %arg%
goto skipvmargs
:uservmargs
set USER_VM_OPTS=%USER_VM_OPTS% %arg%
:skipvmargs
shift
goto moreargs
:donewithargs

if "%USER_VM_OPTS%" == "" set USER_VM_OPTS=-Xmx300m

rem echo FORTIFY_HOME: %FORTIFY_HOME%
rem echo USER_VM_OPTS: %USER_VM_OPTS%
rem echo USER_OPTS:    %USER_OPTS%

"%JAVA_CMD%" %USER_VM_OPTS% -jar "%FORTIFY_HOME%\runReport_lib\runReport.jar"  %USER_OPTS%
