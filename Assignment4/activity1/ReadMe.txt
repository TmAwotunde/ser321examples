

a) A description of your project and a detailed description of what it does
Runs the server two different ways while performing functionality describe in the readme.md. ThreadedPool
thats bound and threaded server that is unbounded different clients would have access to the same recources and sharing that data.

b) An explanation of how we can run the program
gradle runTask1 -Pport=8000 runs the threaded pool server
gradle runTask3 -Pport=8000 runs the unbounded server
gradle runTask3 -Pport=8000 also runs the threaded pool server

gradle runClient -Phost=localhost -Pport=8000 to run client

c) Explain how to "work" with your program, what inputs does it expect etc.
When presented a choice
Please select a valid option (1-6). 0 to disconnect the client
1. add <string> - adds a string to the list and display it
2. clear <> - clears the whole list
3. find <string> - display idx of string if found, else -1
4. display <> - display the list
5. delete <int> - delete item at given index
6. prepend <int> <string> - prepends given string to string at idx
0. quit

you will be asked to input a number depending in the choice it will ask to either input a
number or a string, will return an error message if invalid.

d) A short video for each activity (2-4min) showing how you run the program,
showing what works and briefly show your code.

https://youtu.be/f0Hys7P4PUY

e) Design your calls and user interaction in a way that they are easy. Remember
we have many assignments to grade, design it so it is easy for us. There will be
some requirements later you should fulfill.

f) Name the requirements that you think you fulfilled
x 1. add <string> - adds a string to the list and display it
x 2. clear <> - clears the whole list
x 3. find <string> - display idx of string if found, else -1
x 4. display <> - display the list
x 5. delete <int> - delete item at given index
x 6. prepend <int> <string> - prepends given string to string at idx
x 0. quit