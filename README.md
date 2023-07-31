# Expense Tracker

## Table of Contents
- [Introduction](#introduction)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)

## Introduction

This project is a little project for the getting used to kind of backend stuff.No proffessional concers existed here.Just tried to put everything down i learned. 

## Installation

To get started with this project, follow the steps below:

1. Clone this repository to your local machine.

https://github.com/ardac67/Expense_Tracker.git

** This project benefited from mongoDb container from docker.If you wanna use it ,handle it properly or just download docker.**
** Also this project utilize from some other api but i did not add that part for some security purposes.**

2. Run the project with commandline or any idea you prefer.

** gradlew build **
** java -jar build/libs/{"your.jar"}.jar

## API Endpoints
Below are the API endpoints available in this project:

1.GET /track/getPosts
- Description: Getting all expense posts according to given timeline,need to authenticate with basic authentication
- Request Parameters: 
  -Key:startDate
  -Value:2023-01-01 
  -Key:endDate
  -Value:2023-12-12
- Response: Consists of the expenseTypes and it's values and the userId of the expenses

2.GET /track/getPosts
- Description: Getting all expenses and putting them together in one report  according to given timeline,need to authenticate with basic authentication
- Request Parameters: 
  -Key:startDate (Required)
  -Value:2023-01-01
  -Key:endDate (Required)
  -Value:2023-01-01
  -Key:desiredCurrencies (Required)// this can be populated for example->if you fetch data for one or more currencies this parameter can be populated for them.
  -Value:"TRL"
  -Key:currencyDate (Required) //gets the value of the currencies for that date.
  -Value:2023-01-01
- Response: Calculates the all expenses for the given timeline and gives report about the total and unit based values for all currencies that endpoint requests.Possible codes 200 and 400(bad request some parameter can be missing or not properly formatted)


3.POST /createUser
- Description: Creating user for given data and no need for authentication.
- Request Body:{
                "name":"ardaTest1", //name of the user
                "password":"123",  //pass of the user
                "currency":"USD"  // currency which is used for as base currency for the calculations
               }
-Response: code:201 -> user created or code:401->bad request

4.POST /track/createExpense
- Description: Post the body of the request as a expense , requires authentication 
- Request Body: Sample body -> you can add more field if you wish
  [
   {
      "expenseType": {
        "health": 1200.50,
        "Transportation": 1000.25,
        "Education": 300.75,
        "food": 500.00,
        "Entertainment": 200.00,
        "Utilities": 150.00
      },
      "submittedDate": "2023-09-25"
    }
  ]
- Response: 201(succesful) or 400 (bad request)