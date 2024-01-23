# SaveSync - Client

## Description
This module is the client part of the offsite backup application. Developed with JavaFX, this user interface allows users to select folders to backup, configure connection settings to the backup server, and initiate the backup process.

![SaveSyncSchema.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FSaveSyncSchema.png)

## Prerequisites
- JDK 21 or higher
- JavaFX SDK 17 or higher

## Installation
1. Clone the Git repository
```
git clone https://github.com/SaveSync-App/Client.git
```

2. Configure JavaFX

Ensure that the JavaFX SDK is installed and properly configured in your IDE or development environment.

### Server Connection

Upon launching the application, the user is prompted to connect to the backup server. They must enter the server's IP address as well as their username and password. The User object is then created and sent to the server for authentication via an SSL socket. The server verifies the login information with the LDAP directory and either accepts or rejects the connection.

![ConnectionForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FConnectionForm.png)

### Selecting Folders to Backup

Once connected, the user can select the folder to backup by clicking on the dedicated button. 
A `DirectoryChooser` then opens, allowing the selection of a folder. The user can then start the backup by clicking on the "Start Backup" button. Files are sent to the server via an SSL socket and zipped on the server with backup information (date and time) in the file name.

![SaveForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FSaveForm.png)

### Backup Recovery
From the interface, the user can retrieve the backups made by clicking on the "Start Recovery" button. However, they must first select the destination folder and the backup to restore. Once this is done, the files are retrieved from the server via an SSL socket and restored in the indicated folder.

![RestoreForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FRestoreForm.png)
