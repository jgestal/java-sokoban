#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Script de construcción sin dependencias (solo necesita un JDK 21+).
#
#   ./build.sh compile   Compila el código y copia los recursos
#   ./build.sh test      Compila y ejecuta los tests (JUnit 5)
#   ./build.sh run [N]   Compila y lanza el juego (opcionalmente en el nivel N)
#   ./build.sh jar       Genera dist/Sokoban.jar ejecutable
#   ./build.sh clean     Borra build/ y dist/
#
# El proyecto sigue pudiéndose abrir y ejecutar desde NetBeans (build.xml).
# -----------------------------------------------------------------------------
set -euo pipefail
cd "$(dirname "$0")"

SRC=src
TEST_SRC=test
OUT=build/classes
TEST_OUT=build/test/classes
JUNIT=lib/junit-platform-console-standalone-1.10.3.jar
MAIN=sokoban.App

compile() {
  rm -rf "$OUT" && mkdir -p "$OUT"
  javac --release 21 -Xlint:all -d "$OUT" $(find "$SRC" -name '*.java')
  # Recursos no-Java (niveles y sprites) al classpath, igual que hace NetBeans.
  (cd "$SRC" && find . -type f ! -name '*.java' ! -name '.DS_Store' | while read -r f; do
      mkdir -p "../$OUT/$(dirname "$f")" && cp "$f" "../$OUT/$f"
  done)
}

test() {
  compile
  rm -rf "$TEST_OUT" && mkdir -p "$TEST_OUT"
  javac --release 21 -Xlint:all -d "$TEST_OUT" -cp "$OUT:$JUNIT" $(find "$TEST_SRC" -name '*.java')
  java -jar "$JUNIT" execute --class-path "$OUT:$TEST_OUT" --scan-class-path "$TEST_OUT" --details=tree
}

case "${1:-compile}" in
  compile) compile ;;
  test)    test ;;
  run)     compile; java -cp "$OUT" "$MAIN" "${@:2}" ;;
  jar)     compile; mkdir -p dist; jar --create --file dist/Sokoban.jar --main-class "$MAIN" -C "$OUT" .
           echo "Generado dist/Sokoban.jar  ->  java -jar dist/Sokoban.jar" ;;
  clean)   rm -rf build dist ;;
  *)       echo "Uso: $0 {compile|test|run [nivel]|jar|clean}"; exit 1 ;;
esac
