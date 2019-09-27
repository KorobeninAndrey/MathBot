MathBotServer
=============
* JDK 1.8
* maven 3

###Build
Before building it you have to install maven
##### 1) to build project with tests
- mvn clean install 
##### 2) to build project without tests
- mvn -DskipTests=true clean install 

###Run
Before running, check system environments and windows paths (there should be %JAVA_HOME%\bin in Path)
#####1) for running use jar file with parameter (port number)
- java -jar math-bot-1.0.jar 9090
#####2) for connection to the server use telnet (for example: telnet "address where server was launched" "port where server was launched") 

###Description
Program receives 1 input parameter which has contained a port number
where you would like to launch the math-bot server.
The math-bot server listens to the port, analyzes every line. 
- If input is equal to "hello" then the server returns "world"
- If input is equal to "author" then the server returns "Andrey Korobenin"
- If input is equal to "help" then the server returns command list
- Otherwise the server does a math evaluation
    * Server accepts math signs (=, *, /, +, -), latin letters and numbers
    * Server saves value in memory if input is equal to "letter" = "number" (for example x=1)
    * Server removes values from memory if input is equal to "remove" + "letter" (for example remove x)
    * Server will evaluate and return result 
        - If input equals "number" + "math sign" + "number" (for example 1 + 1)
        - If input equals "letter" + "math sign" + "number" or "number" + "math sign" + "letter" if "letter" was assigned before (for example x = 2, x + 1, result 3)
        
         
