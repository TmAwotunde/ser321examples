## Protocol Elaboration

*Note:* **CL == client, SV == server**

### Logging in

See the link belwo for the video:
https://youtu.be/pamN9KKd79Q

1) CL Request
Client sends over their name

Running the Client: gradle runClient -Pport=9099 -Phost='localhost'

Client will display menu option for the user/player to select from:
1. See the leaderboard
2. Enter the gane
3. Quit

```
OperationType: NAME
Required Fields: name
```
2. SV Response
Server responds with a greeting message

Running the Server: gradle runServer -Pport=9099

Server displays the phrase to be completed. User types in one character at a time
```
ResponseType: WELCOME
Require Fields: hello -- this is just a greeting message to the client, you can write any greeting you like
```

### View Leader Board (from main menu)
CL Request
Clients wants the leaderboard
```
OperationType: LEADERBOARD
Required Fields: *none*
```
SV Response
Server responds with a repeated field of all past players
```
ResponseType: LEADERBOARD
Required Fields: leaderboard (repeated field) including everyone on the leaderboard
```
### Play Game (from main menu)
Client wants a game to be started/joined
CL Request
```
OperationType: START
Required Fields: *none*
```
SV Response
Server responds with a message specifying if the game is joined or started. "phrase" represents the current phrase (hiddenPhrase from the Game), while "task" just lets the user know what to do, e.g. "Guess a letter"
```
ResponseType: TASK
Required Fields: phrase, task
```
CL Request
While in game the client sends a guess to the server, the server expects one letter
```
OperationType: GUESS
Required Fields: guess
```
SV Response
If game is not yet won the server will reply with a phrase and task again
```
	ResponseType: TASK
	Required Fields: phrase, task, eval
```
	eval will either be true/false
	- if false then the phrase will be the same as the prvious one (no letter turned), if true letters will be turned

OR if game is won the current finished phrase will be sent and a message that the game has been won. message field has some winning comment.
```
	ResponseType: WON
	Required Fields: phrase, message
```

### Quit Game 
Client wants to quit the game 
CL Request
```
OperationType: QUIT
Required Fields: *none*
```
SV Response
```
OperationType: BYE
Required Fields: message
```
message field includes some good bye message. 

### Errors
*These can be generated by any malformed or unexpected request, e.g. the client sends over many letters instead of just one. The client*
*is responsible for keeping track of state to continue.*

SV Response
```
ResponseType: ERROR
RequiredFields: message (description of error), type
```
Some error types to use:
1 - required field missing -- in message name the field
2 - request not supported -- in message name the request that is not supported
3 - request got wrong type -- in message name what was expected
0 - any other errors, in this case the message will just be displayed

*NOTE: The client should display the error message that is sent by the server so the client can display it to the player.*
