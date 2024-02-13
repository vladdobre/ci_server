# Assignment 2: Continuous Integration (DD2480)
Repository for DD2480 Continuous Integration assignment - group 28.

## Description
The link to get the builds history : https://dd30-2001-6b0-1-1e30-250-56ff-feb6-5974.ngrok-free.app/


## Running the project locally
In order to Run the project locally, you need to have the following installed:
1) Java 17
2) Maven
3) Ngrok (Check the section "Starting the Server" for more information)


Once you have the above installed, you can run the project by doing the following:

1) Clone the repository
2) Create a folder for saving the build history using the following command only the first time you run the project:
```
mkdir ../build_history
```
2) Open the project in your favorite IDE
3) Run the following command in the terminal to compile the project:
```
mvn clean install
```
4) Run the following command to start the server:
```
mvn exec:java
```
5) The server should now be running on localhost:8028
6) Run ngrok to make the server visible online by using the following command 
```
ngrok http 8028
```
7) The server should now be visible online and you can access it using the url provided by ngrok
8) You can use the url to use the server as a webhook in Github 

*Note*: To run the project, there is a variable in the Main class called "command" and "mavenCommand" that is used to build and compile the project. It depends on the operating system you are using. If you are using Windows, or Linux you have to select the right command. The default command is for Linux. If you are using Windows, you have to change the command by commenting the current command and uncommenting the other command.

## Running the project in the KTH server
In order to run the project in the KTH server, you need to do the following steps:
1) Connect to the KTH server via ssh using the following command:
```
ssh student-shell.sys.kth.se
```
2) Install the prerquisites 'Ngrok' and 'Maven' using the following commands:
```
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok-stable-linux-amd64.zip
wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
tar -xvf apache-maven-3.6.3-bin.tar.gz
```
Adding the path to the environment variables:
```
echo 'export PATH="/home/l/a/USERNAME/apache-maven-3.6.3/bin:$PATH"' >> ~/.bashrc
echo 'export PATH="/home/l/a/USERNAME:$PATH"' >> ~/.bashrc
source ~/.bashrc
```
3) Clone the repository using the following command
4) Run the project using the following commands:
```
mvn clean install
mvn exec:java
```
5) Run ngrok to make the server visible online by using the following command 
```
ngrok http 8028
```
6) The server should now be visible online and you can access it using the url provided by ngrok
7) You can use the url to use the server as a webhook in Github

*Note* : To keep the server running after you close the terminal, we will use 'screen' to run the server in the background. You can do the following commande before running the project:
```
screen -S server_CI
```
Then you can run the project and detach the screen using the following command:
```
Ctrl + A + D
``` 
To reattach the screen, you can use the following command:
```
screen -r server_CI
```

## Running the tests
In order to run the tests, you can do the following:
1) Run the following command in the terminal to run the tests:
```
mvn test
```
2) The tests should now be running and you should see the results in the terminal

## Starting the Server
In order to make the server visible online we need to do the following:
1) Run ngrok.exe and use the following command "ngrok http --domain thrush-engaging-monkfish.ngrok-free.app 8028"

Note: The domain is registered to my ngrok account. If anyone needs to access it, you can go to https://thrush-engaging-monkfish.ngrok-free.app/

Note 2: If the domain is changed for any reason, make sure to update the url in the webhook in Github

2) Add the jars found in the folder with the same name to the dependencies of the project.
3) Compile the project

## Statement of contributions
#### Ilias Lachiri (lachiri)
* Implemented `cloneRepository`, `handlePushEvent`, `handlePullRequestEvent`, diffrent function to extract the information from the payload and test for `cloneRepository` and `handlePushEvent`.
* Wrote the part of the README that explains how to run the project.
* Setup the server on the KTH server to make the ngrok domain visible online.
* Ussing badge to show the build status in the README file. (which is the Group remarkable achivement)
#### Johann Biörck (jbiorck)
* Implemented `compileMavenProject`, `sendBuildResultEmail`, `extractEmail` with assistance from others.
* Added some JavaDoc
* Created tests for null payload with Max and tests for compilation and test execuction with Vlad and Johann, and did some bugfixing in the process.
* Worked on README
#### Kristian Fatohi (kfatohi)

#### Max Israelsson (maxisr)
* Implemented `compileMavenProject`, `generateSummaryFile`, `sendBuildResultEmail`, `extractEmail`, and `removeClonedRepository` with assistance from others.
* I commented on some functions for JavaDoc.
* Created tests for null payload with Johann and tests for compilation and test execution with Vlad and Johann.
* Worked on README


#### Vlad Dobre (dobre)
* Helped implement webhook listener and payload translator from json to Map object
* Helped implement tests for compilation and testing on the CI server
* Wrote the Team assessment based on Essence

## Compilation

### Implementation
Our implementation features a function named compileMavenProject responsible for executing the mvn clean install command and redirecting the output to mavenOutput.txt. Subsequently, we parse the contents of the mavenOutput.txt file to generate a build_summary.json file, presenting the Maven output in a structured JSON format.
### Unit-Tested
To validate the functionality of our implementation, we've developed two distinct projects. The first project is intentionally crafted to compile successfully, while the second project is deliberately designed to fail compilation. Through unit tests, we thoroughly assess the behavior of our function across various project scenarios.
## Notification

### Implementation
We have a function called extractEmail which extracts the commiters email adress from the payload and with the help of another function sendBuildResultEmail sends an email from an email adress we created to the commiter. For this to work it is required that the github user has their email settings as public. 
### Unit-Tested
We united tested the email by writing tests, one called estExtractEmailWithNullPayload which simply feeds the function a null payload to check that the email is null in that case. The method is further tested in our compile tests which are fed an email adress in their dummy payload. 

## Test Execution
### Implementation
For our implementation, we utilize the mvn clean install command, which not only compiles the project but also executes tests and packages the project artifacts.

### Unit-Tested
In our unit tests, we created two distinct test cases: Test_Fail and Test_Success. Test_Fail represents a scenario where the test is expected to fail, while Test_Success represents a scenario where the test is expected to succeed.

## Team (Essence)
We are currently completeing some of the items in the checklist for the Performing state. The team was Seeded during the course outline and when the assignment was presented. The team was Formed during our initial meeting when we spread out the work among our team members. We have fulfilled the criteria for the Collaborating state, but we need more time working as a team before we complete the checklist for the Performing state. This is because we need more time to see if we can consistently meet our commitments and adapt to the changing context of a project.

