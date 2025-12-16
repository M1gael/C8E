# T1 
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

# T2
## Notable Directories
The strucure follows the same default paradigm as T1 , except for : 
```txt
	└── outputs
       ├── output.csv

	└── database
       ├── data.db
```

# Ports Used
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