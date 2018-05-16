# TSA: Thread Sharing Analysis
This project includes static and dynamic sharing analysis libraries for finding thread shared data access locations in multithreaded Java programs.

The easiest way to use the code is to import the project into Eclipse and run everything inside the IDE.

# Command line usage
* Static TSA:
`java -jar tsa.jar Example`

TSA also supports user-specified entry method, which can be configured in `tsa.conf`: `tsa.entry=test`. `test` is the entry method name. For example, try

`java -jar tsa.jar SharingExample2`


* Dynamic TSA:
`java -javaagent:lib/profile-agent.jar -cp tsa.jar Example`

# External libraries
[Soot](http://sable.github.io/soot/) -- A Java optimization framework

[ASM](http://asm.ow2.org/) -- A Java bytecode manipulation and analysis framework

# Questions
Please contact jeff@cse.tamu.edu for any TSA related questions.
