# Record of Tasks and Individual Contributions

The following table records who completed or worked on each major task in the
Buongiorno Social Network project. A **lead contributor** was primarily
responsible for the task, while a **supporting contributor** assisted with
integration, refinement, testing, or debugging.

| Task | Lead contributor | Supporting contributor | Work completed |
| --- | --- | --- | --- |
| Requirements analysis and system planning | ZhehanLin | JingyaoQiu | Analysed the required social-network features and divided the system into Application, UI, Service, Model, Persistence, and Utility layers. |
| Overall system architecture | ZhehanLin | JingyaoQiu | Designed the layered architecture, application dependencies, shared application context, and the overall flow between the JavaFX interface, services, and data model. |
| Project structure and build configuration | ZhehanLin | — | Organised the Java packages and configured Maven, Java 21, JavaFX, testing dependencies, packaging, and application entry points. |
| Application layer | ZhehanLin | — | Implemented `Main` and `SocialNetworkApp`, including application startup and construction of the shared network, session, services, and persistence manager. |
| Core data model | ZhehanLin | — | Implemented `User`, `SocialNetwork`, and `Session`, including user profiles, login state, friendship storage, undirected graph operations, and workplace/hometown indexes. |
| Data-structure design | ZhehanLin | JingyaoQiu | Designed the adjacency-list graph using `HashMap` and `HashSet`, supporting efficient user lookup, friendship operations, common-friend queries, filtering, and recommendations. |
| Password security mechanism | ZhehanLin | — | Designed and implemented salted PBKDF2-HMAC-SHA256 password hashing, password verification, stored-hash validation, and secure random salt generation in `PasswordUtil`. |
| Authentication service | JingyaoQiu | ZhehanLin | Implemented the registration, login, logout, and user-ID availability logic in `AuthService`; integrated it with `PasswordUtil`, `SocialNetwork`, `Session`, and the JavaFX login flow. |
| User and profile services | JingyaoQiu | ZhehanLin | Implemented user lookup, profile updates, and searches by ID, name, workplace, and hometown in `UserService`; later refined and integrated the service with the indexed model and UI. |
| Friendship service | JingyaoQiu | ZhehanLin | Implemented adding and removing friends, displaying friend lists, viewing another user's friends, checking friendships, finding common friends, and filtering friends in `FriendService`; both members contributed to later refinement and debugging. |
| Friend recommendation service | JingyaoQiu | ZhehanLin | Implemented friends-of-friends candidate discovery and recommendation scoring using shared workplace, shared hometown, and mutual friends; both members contributed to later refinement and integration. |
| File persistence | JingyaoQiu | ZhehanLin | Implemented network import and export in `NetworkFileManager`, including user records, friendship records, two-pass loading, validation, and prevention of duplicate undirected friendship records; integrated file operations into the UI. |
| JavaFX application and navigation | ZhehanLin | JingyaoQiu | Implemented `SocialNetworkFxApp`, `AppContext`, the controller contract, screen loading, shared dependency access, and navigation between login, registration, and dashboard screens. |
| JavaFX views and controllers | ZhehanLin | JingyaoQiu | Implemented the FXML views, CSS styling, and controllers for login, registration, dashboard, profile management, friendship operations, and recommendations; service functionality was connected and checked during integration. |
| System testing | JingyaoQiu | ZhehanLin | Conducted functional and system testing of authentication, profile management, friendship operations, recommendations, and file loading/saving. ZhehanLin also tested password behaviour and UI integration. |
| System integration | ZhehanLin and JingyaoQiu | — | Combined the independently developed layers, resolved interface and dependency mismatches, connected services to the JavaFX controllers, and verified complete user workflows. |
| Debugging and refinement | ZhehanLin and JingyaoQiu | — | Diagnosed and fixed issues found during integration and testing, including service behaviour, friendship and recommendation logic, data loading, user searches, UI state, and error handling. |
| Documentation and final packaging | ZhehanLin | JingyaoQiu | Prepared the architecture documentation, class diagram, README, sample data, and final executable JAR. JingyaoQiu contributed technical information and testing results for the completed service and persistence features. |

## Contribution Summary

### ZhehanLin

Responsible for the overall system architecture, password mechanism design, and
implementation of the Application, UI, and Model layers. Also configured the
project structure and build process, prepared the main documentation and final
package, and contributed to service refinement, system integration, testing,
and debugging.

### JingyaoQiu

Responsible for implementing the Service and Persistence layers and conducting
system testing. This included authentication, user and profile operations,
friendship management, friend recommendations, and network file import/export.
Also contributed to UI-service integration, system integration, testing, and
debugging.

