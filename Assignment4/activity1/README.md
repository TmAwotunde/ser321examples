# Assignment 4 Activity 1
## Description
The initial Performer code only has one function for adding strings to an array: 
Other functions: display, sort and switch have been added.

To run the Server and the Client, see the below under the heading "How to run the program".

Once the connection is established between the Server and the Client, user has options to choose from.
1. add
2. display
3. sort
4. switch.

The menu will tell user the available options and the expected input from the Client side.

See the link below for the video on running the program (Activity one, still working on activity two):
https://www.youtube.com/watch?v=dgSecwItMIU

## Protocol

### General Requests
request: { "selected": <int: 1=add, 2=display, 3=sort, 4=switch,
0=quit>, "data": <thing to send>}

### General Response
success response: {"type": <int>, "ok": true, "data": <thing to return> }

Error message should detail what went wrong and should be a string so that the client can just read this string and
Display it in the console, so the user will know what went wrong
  error response: {"type": <int>, "ok": false, "data": {"error": <message>, "details": <string>}}

Some error response messages you should use when appropriate:
- "index out of bounds" (if request index is not in list)
- "unknown request" (when the request int is out of range)
- "required data missing" (when required fields are missing)
- "not specified" (when none of the above but something went wrong)
"error" should be short while details should provide more insight

details:
In this string include details like, which data was missing, which required field was missing, or if "not specified" what went wrong. 
Should be a string so easy to read for any client.

#### add
  Adds the given String to the end of the list
  
  request:
    {"selected": 1, "data": {"string": <string>}}
  
    Example: {"selected": 1, "data": {"string": "NewString"}}
  
  response:
    {"type": 1, "ok": true, "data": Current List as String }

#### display
  Displays the current list, is ok if list is empty.
  
  request:
    {"selected": 2}

    Example: {"selected": 2}
  
  response:
    {"type": 2, "ok": true, "data": Current List as String }


#### sort
  Sorts the elements in the list
  
  request:
    {"selected": 3}
  
      Example: {"selected": 3}
  
  response:
    {"type": 3, "ok": true, "data": Current List as String }


#### switch
  Switches the two Strings at the given index

  request:
   {"selected": 4, "data": {"index1": <int>, "index2": <int>}}
  
    Example: {"selected": 4, "data": {"index1": 2, "index2": 3}} -- switches string 2 with string 3
  
  response:
   {"type": 4, "ok": true, "data": Current List as String }

#### quit
  Tells the server the user wants to disconnect the client

  request:
    {"selected": 0}

    Example: {"selected": 0} 

  response:
    {"type": 0, "ok": true, "data": "Bye" }


## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Server:
    (a) run "gradle runTask1"
    (b) run "gradle runTask2"
    (c) run "gradle runTask3"
```
```   
    For Client:
    run "gradle runClient"
```   



