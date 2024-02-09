# Assignment 2: Continuous Integration (DD2480)
Repository for DD2480 Continuous Integration assignment - group 28.

## Description

## Running the project locally
In order to Run the project locally, you need to have the following installed:
1) Java 17
2) Maven
3) Ngrok (Check the section "Starting the Server" for more information)

Once you have the above installed, you can run the project by doing the following:

1) Clone the repository
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
ngrok http 8028"
```
7) The server should now be visible online and you can access it using the url provided by ngrok
8) You can use the url to use the server as a webhook in Github 


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
ngrok http 8028"
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
1) Run ngrok.exe and use the following command "ngrok http --domain thrush-engaging-monkfish.ngrok-free.app 8080"

Note: The domain is registered to my ngrok account. If anyone needs to access it, you can go to https://thrush-engaging-monkfish.ngrok-free.app/ (This link seems broken)

Note 2: If the domain is changed for any reason, make sure to update the url in the webhook in Github

2) Add the jars found in the folder with the same name to the dependencies of the project.
3) Compile the project

## Statement of contributions
#### Ilias Lachiri (lachiri)

#### Johann Biörck (jbiorck)

#### Kristian Fatohi (kfatohi)

#### Max Israelsson (maxisr)

#### Vlad Dobre (dobre)

## Team (Essence)
