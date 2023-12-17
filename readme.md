# ClientSSL
Ceci est un dépôt qui contient un client pour se connecter au serveur EN SSL

## Informations générales du projet

- Application Java avec le JDK OpenJDK 21.0.1
- Projet Maven
- Utilisation de socket SSL

## Explication de ce dépôt

Ce dépôt contient un client qui permet de se connecter au serveur en SSL.

## Comment utiliser ce client

Dans un premier temps, il faut que le serveur soit lancé.

Le serveur se trouve dans le dépôt suivant :

![ Texte alternatif](/imgREADME/dépôt.png "Titre de l'image") 

**WARNING** : Il faut que tu sois sur la branche "**SSL-Server-V1**" car sur la branche main,
il se peut que j'ai fait des modifications qui ne sont pas encore fonctionnelles.

Ensuite, il faut que tu lances le serveur.

C'est le fichier "**Main_Server**".

## Notes importantes

Ces fichiers sont cruciaux pour le bon fonctionnement du clientSSL :

Quand tu vas essayer de faire fonctionner ton clientSSL, tu devras mettre ces fichiers obligatoirement dans ton dossier de travail.


![captureFichiersSSLClient.png](imgREADME%2FcaptureFichiersSSLClient.png)



Il faut également que la classe User ait absolument le même nom de package que la classe User du serveur.
_Ca sera la package "model"_.
