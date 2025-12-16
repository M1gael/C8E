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

## Ports
- MongoDB : `27017`
- App : `8000`

# T2
No need for any user interaction to get running as with T1.


## Notable Directories
```txt
	└── outputs
       ├── output.csv

	└── database
       ├── data.db
```
Note that these files do not exist and are automatically created at runtime each time.

## Ports
- test1 : `8000`
       - MongoDB : '27017'
- test2 : '8001'


# Run It 
## Docker/Podman
I have made use of podman to build these containers.
```shell
docker-compose up --build
OR
podman-compose up --build
```
As it stands:
- test1 works all by itself, mongoDB's instance and data is not persistent.
- test2 works , created files are persistent.
## Native IDE
The native IDE run for both will work OOTB. Ensure you run the grandle build beforehand.