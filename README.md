# MiniJava-to-LLVM-Parser-Compilers
This project converts MiniJava code into LLVM language. The project was for the Compilers K31 course at the University of Athens.

## Execution:
For the compilation of the program there is make, which
creates the executable Main and to execute it is the command “java Main
X ”where X is all the java files we want to execute. The
application provides the ability for multiple files together.

## Structures:
A list of classes is created to implement the Symbol Table
Symbol table, which is static in the MyVisitor class, for each class which
contains its name, a map for the class variables with key its name and element its type and 2 lists of methods and a class symbol table. THE
each method contains its name and type, as well as 2 maps for
parameters and its variables. While the second map from Symbol table
is used to implement classes that extend the class
she. In the MyVisitor class there is a second list of Ext_class structures, which
consists of 2 strings for the names of the class and the extended class, the
which helps to identify a child's class.
<br>
In addition, each class of the program creates a V_table structure which
has as data the name of the class, the number of methods and 2 maps for
the storage of offset values of variables and methods. During the
MyVisitor creation also initializes a list of V_tables for each class
in the java program.

## Generally:
In main runs the MyVisitor class 2 times for each file with
different arguments, the first for creating the Symbol Table while the
second executes the writing in the ll files. At the beginning of the 2nd time it writes in the file
ll the v_tables and finds the offsets for each method or variable, saving them
in the list from V_tables. After writing the ready-made functions for print, calloc,
exit, runs the java program linearly and prints them in the file in format
llvm. Finally, in addition to the global counters for storing his number
registrar and labels there is also a string operation that shows the operation
in the previous stage.


