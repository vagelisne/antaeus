## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!

### My approach
For completing this challenge with the current implementation I spend almost 12 hours.
I started by reading about Kotlin for 3 hours and in the process I spend 2 more hours. The rest was the implementation.

After reading the documentation of the PaymentProvider.charge function I decided that an easy start would to crate a simple function
that converts Money objects from one currency to another. The next thing implemented was the fetchPending function
of InvoiceService which I tested by creating a new temporary endpoint. I then proceeded to create the payBill function of
BillingService which is the one handling the payment of the invoice. This lead to the creation of the changeStatusToPaid
function for successfully paid invoices. Moving on, the conversion function came to use when a CurrencyMismatchException was thrown.
For cases when the aforementioned exception was thrown, InvoiceService.changeAmount was implemented. The next step was to check
that the function could only be called on the first day of the month. I introduced the BusinessRuleException for this case.
To check it worked as expected, a default value with the current date was added to chargeCustomers method of BillingService.

I tried to enhance the functionality of the BillingService with creating a scheduler with the initialization of the class.
The scheduler is supposed to call the chargeCustomers method every 24 hours. To make sure the BusinessRuleException was not
thrown by this call, a second default attribute with the name userInitiated was added to the method. In that way we could
tell if the user called the function and an exception should be thrown to inform him or if the scheduler called it making
the throw of the exception redundant.

Some further improvements that crossed my mind was the use of a cron scheduler to call the BillingService.chargeCustomers
function. Additionally, the exchange rates could be fetched by an external source instead of being randomly generated.
The creation of an extra table that would audit the calls to the chargeCustomers function and a new table to
hold information about the payment of an invoice would also be of help. eg T_PAYMENT(id, invoiceID, paymentDate)

I could not find a way to access the localhost when deploying through Docker. The image was built, the container
was started but when I wrote the address in the browser it seemed like it was not exposed. The message returned
was "localhost refused to connect"