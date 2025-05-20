# Crypto Wallet (Prototype - Portfolio Edition)
### Project Introduction
Initially, the project took off as a university prototype and was not meant to be a production-ready application. The main objective of the module, which the project was developed in, was to implement software engineering principles like UML diagrams, requirements gathering, and architectural planning in a real project context.\
\
Our primary focus during the implementation was to create a prototype that satisfied the requirements of UML diagrams and documents we made rather than creating a complete environment-independent application on a first prototype. This intentional design influenced how it now operates.
________________________________________
### Structure & Limitations
As explained above, this prototype was not meant to be deployed in production or be accessed by the public but only operate to mimic actual functionality in a prototype context.\
Consequently, the backend was designed to work purely in a development environment that is isolated and tightly controlled.\
Therefore:
- Containerization or environment-independent setup were not included in the project
- The original MySQL database was set up manually during the application development
- The way it was configured was not saved in a reproducible manner

\
Creating the environment from today would require doing it all over - starting from reinstalling MySQL, reconfiguring the database, which had been manually configured during development and wasn’t preserved in a reproducible form.
________________________________________
### What I Did Instead
Instead of spending time trying to set up a manual and narrowly defined development environment, my emphasis was on rendering the project more portfolio ready.\
This involved:
* Cleaning the code structure from redundant files
* Removing or isolating broken components
*	Configuring and setting CI/CD pipelines
*	Setting up real integration tests wired into the CI/CD pipeline
________________________________________
### CI/CD & Testing
This section of the project reflects the kind of automated workflows used in real-world development. It demonstrates that I can set up and configure CI/CD systems using GitHub Actions.\
Due to the original codebase not being testable in many areas, I concentrated on implementing one real integration test:
* The test runs automatically through the CI pipeline and pulls live price data from the Coindesk API
*	It doesn’t use mocks or stubs, but it proves that testing can still be reliable in this setup

