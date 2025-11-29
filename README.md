# RmvTicker

<div align="center">
  <a href="#">
    <img src="https://img.shields.io/badge/Maintained%3F-yes-green.svg" alt="Maintained">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat" alt="Contributions welcome">
  </a>
</div>

RmvTicker is a Java-based application that fetches public transport data from the RMV (Rhein-Main-Verkehrsverbund) API and sends it as a notification to a ntfy.sh topic.

## About The Project

This application is designed to automate the process of checking for public transport connections. It determines the user's likely commute based on the day of the week and time, queries the RMV API for trip details, formats the information into a human-readable message, and sends it as a notification.

### Built With

*   [Java](https://www.java.com/)
*   [Maven](https://maven.apache.org/)
*   [ntfy.sh](https://ntfy.sh/)

## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

*   Java 21 or higher
*   Maven
*   An RMV API key
*   A `ntfy.sh` topic

### Installation & Configuration

1.  Clone the repo
    ```sh
    git clone <YOUR_REPOSITORY_URL>
    ```
2.  Create a `Secrets.java` file in `src/main/java/com/rmv/`. This file is not tracked by Git and must contain your personal API keys.

    ```java
    package com.rmv;

    public class Secrets {
        private final String api_key = "YOUR_RMV_API_KEY";
        private final String ntfy_key = "YOUR_NTFY_TOPIC";

        public String getApi_key() {
            return api_key;
        }

        public String getNtfy_key() {
            return ntfy_key;
        }
    }
    ```

3.  **Customize the Commute**:
    *   **Define Stations**: Open `src/main/java/com/rmv/Station.java` and replace the existing enum values with the names of the stations you use.
    *   **Set Logic**: Open `src/main/java/com/rmv/Controller.java`.
        *   Update the `decisionStation()` method to return the correct start station based on the day of the week.
        *   Update `arrival()` and `departure()` methods to set your desired `startStation` and `endStation` logic.

4.  Build the project using Maven:
    ```sh
    mvn package
    ```
    This will create a `rmv-1.0-SNAPSHOT.jar` file in the `target/` directory.

## Usage

To run the application, use the following command:

```bash
java -jar target/rmv-1.0-SNAPSHOT.jar -start
```

The `-start` argument initiates the main application logic, which will then determine the appropriate trip to query and send a notification.

There is also a `-test` argument for a one-time test run with hardcoded stations:

```bash
java -jar target/rmv-1.0-SNAPSHOT.jar -test
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.
