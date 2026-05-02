#!/bin/bash
# Dungeon Arise - Installation initiale du serveur Forge
# A executer une seule fois apres avoir depose les fichiers
# Requis : Java 17, connexion internet pour telecharger Forge

FORGE_VERSION="1.20.1-47.3.22"
FORGE_INSTALLER="forge-${FORGE_VERSION}-installer.jar"

echo "=== Dungeon Arise Server Setup ==="

# Telecharger l'installeur Forge si absent
if [ ! -f "$FORGE_INSTALLER" ]; then
    echo "Telechargement de Forge ${FORGE_VERSION}..."
    wget -O "$FORGE_INSTALLER" "https://maven.minecraftforge.net/net/minecraftforge/forge/${FORGE_VERSION}/forge-${FORGE_VERSION}-installer.jar"
fi

# Installer Forge en mode serveur
echo "Installation de Forge..."
java -jar "$FORGE_INSTALLER" --installServer

# Accepter l'EULA
echo "eula=true" > eula.txt

echo ""
echo "=== Installation terminee ==="
echo "Lancez le serveur avec : bash start.sh"
