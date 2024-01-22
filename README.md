# SaveSync - Client

## Description
Ce module est la partie cliente de l'application de sauvegarde externalisée. Développée avec JavaFX, cette interface utilisateur permet aux utilisateurs de sélectionner les dossiers à sauvegarder, de configurer les paramètres de connexion au serveur de sauvegarde, et d'initier le processus de sauvegarde.

## Prérequis
- JDK 21 ou supérieur
- JavaFX SDK 17 ou supérieur

## Installation
1. Cloner le dépôt Git
```
git clone https://github.com/SaveSync-App/Client.git
```

2. Configurer JavaFX

Assurez-vous que le SDK JavaFX est installé et correctement configuré dans votre IDE ou environnement de développement.

## Fonctionnement

Cette application permet à l'utilisateur de sauvegarder ses fichiers sur un serveur distant. 
Elle permet également de récupérer les sauvegardes effectuées.

![SaveSyncSchema.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FSaveSyncSchema.png)

### Connexion au serveur

Lors du lancement de l'application, l'utilisateur est invité à se connecter au serveur de sauvegarde. Il doit renseigner l'adresse IP du serveur ainsi que son nom d'utilisateur et son mot de passe.
L'objet `User` est ensuite créé et envoyé au serveur pour authentification avec un socket SSL.
Le serveur vérifie les informations de connexion auprès de l'annuaire LDAP et accepte ou non la connexion.

![ConnectionForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FConnectionForm.png)

### Sélection des dossiers à sauvegarder

Une fois connecté, l'utilisateur peut sélectionner le dossier à sauvegarder en cliquant sur le bouton dédié. 
Un `DirectoryChooser` s'ouvre alors et permet de sélectionner un dossier.
L'utilisateur peut ensuite démarrer la sauvegarde en cliquant sur le bouton "Démarrer la sauvegarde".
Les fichiers sont envoyés au serveur via un socket SSL et zipés sur le serveur avec les informations concernant la sauvegarde (date et heure) dans le nom du fichier.

![SaveForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FSaveForm.png)

### Récupération des sauvegardes
Depuis l'interface, l'utilisateur peut récupérer les sauvegardes effectuées en cliquant sur le bouton "Démarrer la récupération".
Il doit toutefois auparavant sélectionner le dossier de destination et la sauvegarde à restaurer.
Une fois ceci fait, les fichiers sont récupérés depuis le serveur via un socket SSL et restaurés dans le dossier indiqué.

![RestoreForm.png](src%2Fmain%2Fresources%2Fimage%2Fdoc%2FRestoreForm.png)