#!/bin/bash
set -e
cd libreria_is && mvn clean package -DskipTests && cd ..
cp libreria_is/dist/libreria_is-*.jar Proyecto_Poo/librerias/libreria_is.jar
echo "Listo: libreria_is.jar actualizado con éxito en Proyecto_Poo."
