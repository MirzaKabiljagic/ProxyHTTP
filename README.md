    # RKN Fixing out code

-fixed Parser.class
The main problem in this class was startParse() method. First "if condition" range was bigger than necessary. 

## Fixes: 

* fixed P3-FakeContent plugin
	* In the main method run() added "if condition", which check whether we test plugins or not
	* In the method plugins() first "if condition" include bigger range 


* fixed P4- JS injection
    * Called JSInject() function correctly only when content_type is html
    * Fixed compress html_Body back to Gzip.	

## New Implementations

* Script for testing of "Inject a JavaScript code"
* P5 - Phishing in the dark
  <br>
  In Proxy client we call a method redirect, which replaces the host, whom we previously parsed. Adress stays the same, but the body of the sites which you pass in are swapped. 
   After we add carriage return line feed to the header and return it. Very interesting plugin.
* P1 - Improved Requests
  <br>
  Added a function which enables adding our own cookie. By passing in the required parameters for the program call, to the response from the server, the cookies with the specified name were able to be saved.
  
  <br>
  Added SOP function.
    
  
 
