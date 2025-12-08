# T1 
## Setup
1. Ensure Mongo is started in the dir containing the database
```shell
mongod --dbpath dirName
```

2. Now run the application via the source code from and IDE , or the jar artifact , assuming you have JDK 21 installed.

3. The HTTP sevrer will be hosted on port `8000` and MongoDB will be hosted on `27017`.

## Directory Structure
```txt
	└── src
       ├── main
           ├── kotlin
           │    └── Main.kt
           └── resources
                └── index.html
```
This is not the complete project structure , only the directories I have worked with , the other directories follow the standard java/kotlin project structure.
