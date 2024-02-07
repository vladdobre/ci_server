# Assignment 2: Continuous Integration (DD2480)
Repository for DD2480 Continuous Integration assignment - group 28.

## Description

## Running the project
In order to Run the project, you need to have the following installed:
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
5) The server should now be running on localhost:8080

## Running the tests

## Starting the Server
In order to make the server visible online we need to do the following:
1) Run ngrok.exe and use the following command "ngrok http --domain thrush-engaging-monkfish.ngrok-free.app 8080"

Note: The domain is registered to my ngrok account. If anyone needs to access it, you can go to https://thrush-engaging-monkfish.ngrok-free.app/

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
