# Code review of TICKET-101


## Well-implemented features:

1) **Implemented a decision engine**: The decision engine that takes the personal code, loan amount and loan period in months to determine a valid loan amount.

2) **Loan amount validation**: The decision engine determines the maximum sum to be approved in case the requested sum is bigger that could be approved.

3) **Debt segmentation**: The decision engine considers existing debt to assign appropriate segmentation to the user.

4) **Extended loan period**: If a suitable loan isn't found within the requested period, the engine can suggest an extended period for the desired loan amount.

5) **Frontend design**: The input validation and form design are well-implemented.



## Areas of improvement:


1) **MOST IMPORTANT Maximum loan amount limit**: The decision engine should determine the maximum approvable loan amount for a given period, regardless of the requested amount. Currently, it only validates the requested amount.

2) **Person credit score calculation**: Credit score calculation not implemented as needed. Current implementation takes into account only the loan period and creditModifier ( multiplies these values).

**Additional notes**: Loan period select slider starting value set to 6 months in frontend although the minimum loan period is 12 months.


#### All shortcomings have been addressed according to the requirements.





# InBank Backend Service

This service provides a REST API for calculating an approved loan amount and period for a customer.
The loan amount is calculated based on the customer's credit modifier, which is determined by the last four
digits of their ID code.

## Technologies Used

- Java 17
- Spring Boot
- [estonian-personal-code-validator:1.6](https://github.com/vladislavgoltjajev/java-personal-code)

## Requirements

- Java 17
- Gradle

## Installation

To install and run the service, please follow these steps:

1. Clone the repository.
2. Navigate to the root directory of the project.
3. Run `gradle build` to build the application.
4. Run `java -jar build/libs/inbank-backend-1.0.jar` to start the application

The default port is 8080.

## Endpoints

The application exposes a single endpoint:

### POST /loan/decision

The request body must contain the following fields:

- personalCode: The customer's personal ID code.
- loanAmount: The requested loan amount.
- loanPeriod: The requested loan period.

**Request example:**

```json
{
"personalCode": "50307172740",
"loanAmount": "5000",
"loanPeriod": "24"
}
```

The response body contains the following fields:

- loanAmount: The approved loan amount.
- loanPeriod: The approved loan period.
- errorMessage: An error message, if any.

**Response example:**

```json
{
"loanAmount": 2400,
"loanPeriod": 24,
"errorMessage": null
}
```

## Error Handling

The following error responses can be returned by the service:

- `400 Bad Request` - in case of an invalid input
    - `Invalid personal ID code!` - if the provided personal ID code is invalid
    - `Invalid loan amount!` - if the requested loan amount is invalid
    - `Invalid loan period!` - if the requested loan period is invalid
- `404 Not Found` - in case no valid loans can be found
    - `No valid loan found!` - if there is no valid loan found for the given ID code, loan amount, and loan period
- `500 Internal Server Error` - in case the server encounters an unexpected error while processing the request
    - `An unexpected error occurred` - if there is an unexpected error while processing the request

## Architecture

The service consists of two main classes:

- DecisionEngine: A service class that provides a method for calculating an approved loan amount and period for a customer.
- DecisionEngineController: A REST endpoint that handles requests for loan decisions.
