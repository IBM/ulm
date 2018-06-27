# The Universal Logfile Multiplexer (ULM)

The Universal Logfile Multiplexer (ULM) is a java tool which solves the problem of monitoring large number of log files simultaneously, such as those produced during software installations - the use case which the tool was originally developed for. It makes it easier to view all the logs in one terminal session, prefixed by identifiers to let you know where they came from.

## Executive Overview

ULM is a compact java tool for monitoring live logs on your system. You give
it a list of logfiles, and it shows them merged together in a single output.
This makes it easier for you to monitor multiple log files on your system
for any errors or unusual activity. This is particularly usefule when
running on a software stack, where each product in the stack produces its
own log files. The default output is similar to running the UNIX "tail -f"
on multiple files simultaneously. The two main advantages of using ULM are:

1. ULM shows you each file tagged with a user-definable prefix that identifies the file it came from
2. It has another ability to let you log to a file on disk (optionally
    compressed) and split out the log files based on a regular expression in
    the file. This lets you split out a product log from a long running test
    suite into the separate logs for individual test cases.

The application is written in java and is multithreaded. One thread is
created to read each input file, and another thread is used to manage the
output. ULM was initially developed to be used in conjunction with the
[qinstall](https://github.com/sxa555/qinstall) tool.

## How do I use it?

ULM requires a Java SDK to be available in the path and make to be able to
use the supplied (very basic) makefile. Download the code and run

```
   make
```

This will compile the code and create a jar file of the result in the same
directory as the makefile.  You can compile manually if you do not have make
available - look at the makefile for the underlying commands.

Here is an exmaple of how to invoke ULM:

```
   java -jar ulm.jar [/logfiles/outputlog.log] /mytest/log1.log L1 /mytest/log2.log L2
```

You can specify as many files as you want. L1 and L2 in this example are the
prefixes that are used in the output to indicate which file any given line
came from. The first optional parameter is used if you want to send the
multiplexed output to a file.

You can specify a directory name instead of a filename.  In this case, it
will monitor the specified directory for new files.  If it detects any, then
they will automatically be monitored.  You can use this if your product has
a log directory that creates files with unknown names in it so you can get
better notification when new errors show up.  Using this method, the
'prefixes' will have number added to them to indicate which file is being
displayed.  The first line of the output with that prefix will be a message
from ULM indicating the detected file name.

## Advanced use

If you want to run with the same configuration over and over again (a likely
scenario for a test suite) then you can store the parameters in a custom
configuration file. This file should have a .properties extension, and will
contain the options which would normally be passed on the command line. To
use this mode of operation, you should supply ULM with only one parameter,
which is the name of the properties file. This mode also allows you to do
the multiplexed file spliting described earlier.<p>

Here is an example of a properties file:

```
  file1 = /mytest/log1.log
  name1 = L1
  file2 = /mytest/log2.log
  name2 = L2
  logDest = /logfiles/outputlog.log
  delay = 50
  pattern1 = ^Test start.*07$
  pattern2 =
  compress = true
```

Most of the parameters are reasonably clear. The pattern1/2 are the regular
expressions which will trigger writing to a new output file (The matched
line will appear in both files!) The compress option will cause the output
to be stored in a gzip format. Finally, the delay option specifies how long
in milliseconds each thread sleeps for between polling each logfile.
