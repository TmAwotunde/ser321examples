a) A description of your project and a detailed description of what it does
Given 3 options to see leaderboard, run game, or quit. When playing the game given a board find matching tile characters to uncover, win by uncovering all tiles.
Two or more clients can play the game on a server and client side thats threaded.
b) An explanation of how we can run the program
to run server Gradle runServer -Pport=port
to run client Gradle runClient -Pport=port -Phost='localhost'
c) Explain how to "work" with your program, what inputs does it expect etc.
When started it asked for your name entering your name
The will prompt for what choice you want leaderboard, game, or quite.
1 - leaderboard
2 - game 
3 - quit
taking in integers. when game starts pick characters and number in format of the board.
So <a-b><1-4>, <character><number>, example a1.
d) A short video for each activity (2-4min) showing how you run the program,
showing what works and briefly show your code.

https://youtu.be/cruw32DBA8k

e) Design your calls and user interaction in a way that they are easy. Remember
we have many assignments to grade, design it so it is easy for us. There will be
some requirements later you should fulfill.

f) Name the requirements that you think you fulfilled