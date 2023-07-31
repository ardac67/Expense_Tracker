# Expense Tracker

## Table of Contents
- [Introduction](#introduction)
- [Installation](#installation)
- [API Endpoints](#api-endpoints)
- [License](#license)

## Introduction

Expense Tracker is a simple backend project designed for learning purposes. It allows users to track their expenses within a specified timeline. Please note that this project is not intended for professional use, and certain APIs have been omitted for security reasons.

## Installation

To get started with Expense Tracker, follow these steps:

1. Clone this repository to your local machine:

2. Navigate to the project directory:

3. Build the project using gradle:

4. Run the application:


**Note**: This project utilizes a MongoDB container from Docker. Ensure that you handle it properly or install Docker if you want to use it.

## Usage

Expense Tracker provides API endpoints for various functionalities. Below are the available endpoints:

1. **GET /track/getPosts**
- Description: Fetches all expense posts according to a given timeline. Requires basic authentication.
- Request Parameters:
  - Key: startDate (Required) - Value: 2023-01-01
  - Key: endDate (Required) - Value: 2023-12-12
- Response: Provides a list of expenseTypes with their values and the userId of the expenses.

2. **GET /track/getPosts**
- Description: Fetches all expenses and generates a report for the given timeline. Requires basic authentication.
- Request Parameters:
  - Key: startDate (Required) - Value: 2023-01-01
  - Key: endDate (Required) - Value: 2023-01-01
  - Key: desiredCurrencies (Required) - Populated with the currencies to fetch data for. Value: "TRL"
  - Key: currencyDate (Required) - Gets the value of the currencies for that date. Value: 2023-01-01
- Response: Calculates the total expenses for the given timeline and provides a report with total and unit-based values for all requested currencies. Possible response codes are 200 and 400 (bad request, some parameters may be missing or not properly formatted).

3. **POST /createUser**
- Description: Creates a user with the provided data. No authentication is required.
- Request Body: 
  ```json
  {
    "name": "ardaTest1", // Name of the user
    "password": "123",   // Password of the user
    "currency": "USD"    // Currency used as the base currency for calculations
  }
  ```
- Response: Returns code 201 if the user is created successfully or code 401 if there is a bad request.

4. **POST /track/createExpense**
- Description: Posts the body of the request as an expense. Requires authentication.
- Request Body: A sample body is provided, and additional fields can be added if desired.
  ```json
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
  ```
- Response: Returns code 201 if the request is successful (expense is created) or code 400 if the request is bad (invalid schema).

