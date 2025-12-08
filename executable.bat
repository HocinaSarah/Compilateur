@echo off
title Mini-Compilateur Python
color 0A
cls
echo ╔═══════════════════════════════════════════════════════════╗
echo ║         MINI-COMPILATEUR PYTHON - Sarah Hocina           ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Demarrage de l'analyseur...
echo.

java -jar Analyseur_Lexical-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
echo ─────────────────────────────────────────────────────────────
echo Analyse terminee !
echo.
pause