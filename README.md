# Computation Graph Server

A Java-based HTTP server implementation that supports a visual computation graph system. The server allows you to create and visualize computation graphs where agents perform mathematical operations on data flowing through topics.

## Features

- Simple HTTP server with servlet support
- Real-time computation graph visualization
- Support for multiple mathematical operations:
  - Multiplication
  - Division
  - Power
  - Addition
  - Subtraction
  - Increment
- Interactive web interface
- Topic-based message passing
- Dynamic graph updates

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Web browser with JavaScript enabled

## Project Structure

```
.
├── src/
│   ├── agents/         # Mathematical operation agents
│   ├── configs/        # Configuration and graph management
│   ├── graph/          # Core graph components
│   ├── server/         # HTTP server implementation
│   ├── servlets/       # Request handlers
│   ├── utils/          # Utility classes
│   ├── views/          # Graph visualization
│   └── Main.java       # Application entry point
├── html_files/         # Web interface files
└── temp_configs/       # Temporary configuration storage
```

## Quick Start

1. Clone the repository
2. Compile the project
3. Run the Main class
4. Access the web interface at http://localhost:8080/app/

## Running the Server

### Method 1: Using an IDE
1. Open the project in your preferred IDE
2. Run `src/Main.java`

### Method 2: Command Line
```bash
# Compile the project
javac -d bin src/**/*.java

# Run the server
java -cp bin Main
```

## Using the Computation Graph
![image](https://github.com/user-attachments/assets/08a32608-dedc-421a-b779-3a2875e2e726)


1. Create a configuration file (e.g., `complex.conf`):
```
MultiplyAgent
A,B
E
DivideAgent
C,D
F
PowerAgent
E,F
G 
```

2. Upload the configuration through the web interface
![image](https://github.com/user-attachments/assets/64cd6bd0-e066-4b6e-887d-d400536fd3c7)


3. Use the interface to:
   - View the graph visualization
   - Monitor topic values
   - Send messages to topics
   - Watch computations happen in real-time

## Configuration File Format

The configuration file uses a simple format where each agent and its input/output topics are specified in groups of three lines:
```
AgentType
inputTopic1,inputTopic2
outputTopic
```

Each agent configuration block specifies:
1. The type of agent (e.g., MultiplyAgent, PlusAgent)
2. Input topics (comma-separated if multiple)
3. Output topic

Available agent types:
- `MultiplyAgent`: Multiplies two inputs
- `DivideAgent`: Divides first input by second
- `PowerAgent`: Raises first input to power of second
- `PlusAgent`: Adds two inputs
- `MinusAgent`: Subtracts second input from first
- `IncAgent`: Increments input by 1

## Example Configurations

### Simple Addition with Increment
```
# simple.conf
PlusAgent
A,B
C
IncAgent
C
D
```
This configuration:
1. Adds values from topics A and B, outputs to topic C
2. Increments the value from topic C, outputs to topic D

### Complex Expression ((A*B)^(C/D))
```
# complex.conf
MultiplyAgent
A,B
E
DivideAgent
C,D
F
PowerAgent
E,F
G
```
This configuration:
1. Multiplies values from topics A and B, outputs to topic E
2. Divides values from topics C and D, outputs to F
3. Raises E to the power of F, outputs final result to G

## Debugging

- Enable debug mode in `Main.java` by setting:
  ```java
  Logger.setDebugMode(true);
  ```
- Check the console for detailed logging information
- Monitor the browser's developer tools console for client-side logs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details 
