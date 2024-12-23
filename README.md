# Merge request assistant

JetBrains test task

1. Connects to a private GitHub account
2. Lists available repositories
3. Creates a pull request that add Hello.txt

## Project structure

.idea/
| runConfigurations/
|---- Run.xml <-- main configuration
src/
| main/
|---- java/
|-------- apiClients/
|------------ ApiClient.java
|------------ GitHubApiClient.java
|-------- cli/
|------------ CommandContext.java
|------------ CommandHandler.java
|------------ CreateMergeRequestHandler.java
|------------ ListRepositoriesHandler.java
|-------- dataModels/
|------------ ApiResponse.java
|------------ FileDetails.java
|------------ MergeRequestParameters.java
|-------- helpers/
|------------ ConfigHelper.java
|------------ FileHelper.java
|------------ InputHelper.java
|-------- interfaces/
|------------ RepositorySelector.java
|------------ GitHubMergeRequestAssistant.java <-- main entry point
|---- resources/
| test/
|---- java/
|-------- cli/
|------------ CreateMergeRequestHandlerTest.java
|------------ ListRepositoriesHandlerTest.java
|-------- helpers/
|------------ ConfigHelperTest.java
.gitignore
config.properties
pom.xml


## Functionality

After running the application, all repositories will be listed in the CLI

![listRepositories]()

Output of the program looks like this

![output]()

## Usage

1. Clone the repository
2. Setup your personal access token in the config.properties file
3. Set configuration as "Run"
4. Run the application

### Testing

1. Write "mvn test" in the terminal

## Design features

### Chain of reponsibility

CLI Comand Handlers are implement using [Chain of Responsibility pattern](https://refactoring.guru/design-patterns/chain-of-responsibility)

### Design extensibility

Since ApiClient is an abstract class, it allows the application to be exendable in the future, for example by introducing another versioning system, like GitLab.

Chain of responsibility pattern allows to extend functionality of the CLI interface. 

## Dependencies

1. OkHttp
2. Gson
3. JUnit
4. Mockito

