#!/bin/bash
# ─────────────────────────────────────────────────────────────────
# build.sh  —  Compile and run Hotel Management System (GUI)
# Usage:
#   chmod +x build.sh
#   ./build.sh          (compile + run)
#   ./build.sh compile  (compile only)
#   ./build.sh run      (run only — requires prior compile)
# ─────────────────────────────────────────────────────────────────

MAIN_CLASS="com.hotel.HotelManagementApp"
SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"

JDBC_JAR=$(ls "$LIB_DIR"/mysql-connector-j-*.jar 2>/dev/null | head -1)

if [ -z "$JDBC_JAR" ]; then
    echo "ERROR: No mysql-connector-j-*.jar found in lib/"
    echo "Download from: https://dev.mysql.com/downloads/connector/j/"
    exit 1
fi

compile() {
    echo "── Compiling ──────────────────────────────────────────"
    mkdir -p "$OUT_DIR"
    find "$SRC_DIR" -name "*.java" | xargs javac \
        -cp "$JDBC_JAR" \
        -d "$OUT_DIR" \
        -encoding UTF-8
    if [ $? -eq 0 ]; then
        echo "✔  Compilation successful → $OUT_DIR/"
    else
        echo "✘  Compilation failed."
        exit 1
    fi
}

run() {
    echo "── Launching GUI ──────────────────────────────────────"
    java -cp "$OUT_DIR:$JDBC_JAR" "$MAIN_CLASS"
}

case "$1" in
    compile) compile ;;
    run)     run     ;;
    *)       compile && run ;;
esac
