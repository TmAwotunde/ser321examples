# Assignment 4 Activity 1
## Description
The initial Performer code only has one function for adding strings to an array: 

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=clear, 3=find, 4=display, 5=delete, 6=prepend
0=quit>, "data": <thing to send>}

  add: data <string> -- a string to add to the list, this should already mostly work -- might neet changes though
  clear: data <> -- no data given, clears the whole list
  find: data <string> -- display index of string if found, else -1
  display: data <> -- no data given, displays the whole list
  delete: data <int> -- int of index which entry in list should be deleted
  prepend: data <int> <string> -- index and string, prepends given string to the string that is already at that index (so it changes that entry), e.g. "data":"1 hello"
  quit: data <> -- no data given, will quit the connection

### Responses

success response: {"ok" : true, type": <String>, "data": <thing to return> }

type <String>: echoes original selected from request
data <string>: 
    add: return current list
    clear: return empty list
    find: return integer value of index where that string was found or -1
    display: return current
    delete: return current list
    prepend: return current list


error response: {"ok" : false, "message"": <error string> }
error string: Should give good error message of what went wrong which can be displayed as is to the user in the Client

The program you are given should run out of the box, but it is not threaded nor does it include all the functionality, nor are the things that are already given necessarily complete. You should make all necessary changes while still adhering to the given protocol. 

## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Server, run "gradle runServer -Pport=9099 -q --console=plain"
```
```   
    For Client, run "gradle runClient -Phost=localhost -Pport=9099 -q --console=plain"
```   



