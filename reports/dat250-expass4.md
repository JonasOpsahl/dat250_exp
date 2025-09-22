# Report: Experiment 4 - DAT250

This report summarizes the work and challenges I encountered during the implementation of the database part for my voting application. 

---

## Technical Problems and Solutions

I faced several technical challenges during the transition from an in-memory storage solution to a persistent one using JPA. The following sections detail these problems and the solutions I implemented.

### 1. Entity Relationships and Associations

Initially, the `Poll` class contained an integer field `creatorId` to represent the user who created it. When integrating JPA, this approach was insufficient as JPA is designed to work with object relationships.

* **Problem**: A simple integer ID doesn't allow JPA to manage the relationship between a `Poll` and a `User`.
* **Solution**: The `creatorId` field was replaced with a direct reference to the `User` entity: `private User creator;`. This was annotated with `@ManyToOne` to establish a proper database relationship. This change required minor updates in the `PollManager` and `PollController` to pass the `User` object directly instead of just its ID.

### 2. Implementing a Composite Key

The `VoteOptions` entity required a composite primary key composed of the poll ID and the user ID to ensure a user could only vote once per poll option.

* **Problem**: A standard `@Id` annotation on a single field was not sufficient.
* **Solution**: I created a dedicated ID class, `VoteOptionsId`, to represent the composite key. After researching the requirements for a JPA ID class, I ensured it met the following criteria:
    * It must be a `public` class.
    * It must implement `java.io.Serializable`.
    * It must have a no-argument constructor.
    * It must implement the `equals()` and `hashCode()` methods.

The `VoteOptions` entity was then annotated with `@IdClass(VoteOptionsId.class)` to link it to the composite key definition.

### 3. Decoupling Storage Logic

I had invested significant time in the original in-memory storage logic and wanted to keep it as an alternative to the new database implementation.

* **Problem**: The application logic was tightly coupled to the in-memory storage class, making it difficult to switch to a database-backed solution.
* **Solution**: I applied the **Strategy design pattern**. I created a `PollService` interface that defined the contract for all storage operations. Then, I created two classes that implement this interface: `InMemoryPollService` and `HibernatePollServuce`. The specific implementation to be used by the application can now be easily configured in the `application.properties` file, providing flexibility without changing the controllers. This required refactoring the `PollController` and `UserController` to depend on the interface rather than the concrete class, which initially caused some build errors until all references were updated.

### 4. General Mapping and Naming Issues

Throughout the implementation, I encountered several errors related to incorrect naming and mapping between the Java code, JPA queries, and test cases.

* **NullPointerException**: A `NullPointerException` occurred because the `List` of vote options within the `Poll` entity was not initialized. The fix was to initialize the collection upon declaration: `private List<VoteOptions> voteOptions = new ArrayList<>();`.
* **JPQL Errors**: I received a `Could not resolve root entity "Polls"` error. This was caused by a simple typo in a test query where I had written `"Polls"` instead of the correct entity name, `"Poll"`.
* **Test Case Mismatches**: Many tests failed initially because the variable names in the test code did not match the ones in my entity classes. For example, a test expected a field named `createdBy`, whereas my `Poll` entity used `creator`. This required some reviewing of the test suite to align all property names.

### 5. Complex Query vs. Brute-Force Filtering

I attempted to write an optimized, complex JPQL query in the `getPolls` method to filter polls based on their visibility (public or private) directly in the database.

* **Problem**: The complex query involving joins resulted in a strange bug where private polls would intermittently disappear from the view after a page refresh.
* **Solution**: After spending considerable time debugging, I opted for a more straightforward, "brute-force" solution. I now fetch all polls from the database with a simple query and then perform the filtering logic within the Java code. While this might be less performant on a very large dataset, it is reliable and much easier to debug.

---

## Database Inspection

### Inspection Method

To inspect the database schema and data, I used the **H2 Console**, which is a simple web-based client for the H2 in-memory database. By enabling it in the `application.properties` file, it becomes accessible at the `/h2-console` endpoint while the application is running. This tool allowed me to run SQL queries directly and view the tables, columns, and relationships that JPA generated. To login I simply went to "http://localhost:8080/h2-console/", and I didnt specify any username or password in the persistence.xml so I didnt need to provide any to login. The JDBC URL can also be found in the persistence.xml. I basically just copied this file from the old example that was provided to us in the project description.

### Generated Tables

Based on the entity classes, JPA created the following tables in the database:

1.  **`USER` Table**
2.  **`POLL_INVITED_USERS` Table**
3.  **`POLLS` Table**
4.  **`VOTES` Table**
5.  **`VOTE_OPTIONS` Table**

Screenshots can be found in the "Pictures-expass4" folder in this repo.

---

## Pending Issues

The only area for potential future improvement is the `getPolls` method mentioned earlier. The current brute-force filtering approach works, but revisiting the complex JPQL query to solve the bug could lead to better performance if the application were to scale to a very large number of polls. However, for the scope of this assignment, the current solution is robust and functional. Also passwords are just stored as strings which is a security concern.