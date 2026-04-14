# Space Controll Documentation

## Overview
Space Controll is a comprehensive office space management system that enables efficient allocation and utilization of office resources.

## Features
- **Space Allocation**: Manage and allocate office spaces effectively.
- **Resource Management**: Monitor and control office resources.
- **Booking System**: Allow users to book spaces and resources online.
- **Analytics and Reporting**: Generate reports on space usage and occupancy.

## Tech Stack
- **Java 11**: Programming Language
- **Spring Boot**: Framework for building the application
- **MySQL**: Database for storing application data
- **Thymeleaf**: Template engine for rendering the web interface
- **Maven**: Build tool for dependency management

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/DKgmpl/SpaceControll.git
   ```
2. Navigate to the project directory:
   ```bash
   cd SpaceControll
   ```
3. Install dependencies:
   ```bash
   npm install
   ```

## Usage
1. To start the application, run the following command:
   ```bash
   npm start
   ```
   Or run the application using the command: 
   ```bash
   mvn spring-boot:run
   ```
2. Access the application at `http://localhost:8080`

## Project Structure
```bash
SpaceControll/
├── .idea/                          # IntelliJ IDEA configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pl/edu/wszib/dnogiec/spacecontroll/
│   │   │       ├── controller/     # REST controllers
│   │   │       ├── service/        # Business logic services
│   │   │       ├── repository/     # Data access layer (JPA)
│   │   │       ├── model/          # Entity classes
│   │   │       ├── config/         # Configuration classes
│   │   │       └── SpaceControllApplication.java
│   │   └── resources/
│   │       ├── templates/          # Thymeleaf HTML templates
│   │       ├── static/
│   │       │   ├── css/           # Stylesheets
│   │       │   ├── js/            # JavaScript files
│   │       │   └── images/        # Images and assets
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
│       ├── java/
│       │   └── pl/edu/wszib/dnogiec/spacecontroll/
│       │       ├── controller/     # Controller tests
│       │       ├── service/        # Service tests
│       │       └── repository/     # Repository tests
│       └── resources/
│           └── application-test.properties
├── .gitignore
├── pom.xml                         # Maven configuration
└── README.md                       # Project documentation
```

## Contributing
Contributions are welcome! Please submit a pull request to contribute to this project.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
