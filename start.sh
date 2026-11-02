#!/usr/bin/env bash
# ==================================================================
#  ChatSpace one-command launcher for macOS / Linux
#  Usage:  bash start.sh
#  Starts the backend (port 8080) in the background, logging to backend.log,
#  waits until it is ready, then runs the frontend (port 5173) in this terminal.
#  Press Ctrl+C to stop BOTH.
# ==================================================================
set -e
cd "$(dirname "$0")"

echo
echo "===== ChatSpace launcher ====="

if ! command -v java >/dev/null 2>&1; then
  echo "[ERROR] Java not found. Install JDK 17 or newer (https://adoptium.net) and try again."
  exit 1
fi
if ! command -v npm >/dev/null 2>&1; then
  echo "[ERROR] Node.js/npm not found. Install Node.js 18 or newer (https://nodejs.org) and try again."
  exit 1
fi
java -version 2>&1 | head -1
echo "node $(node -v)"

if [ ! -d frontend/node_modules ]; then
  echo "[1/3] Installing frontend packages (first run only)..."
  (cd frontend && npm install)
else
  echo "[1/3] Frontend packages already installed."
fi

echo "[2/3] Starting backend (log: backend.log). First run downloads Maven + libraries: 3-5 minutes..."
chmod +x backend/mvnw
set -m   # run the backend in its own process group so Ctrl+C can stop all of it
(cd backend && ./mvnw spring-boot:run) > backend.log 2>&1 &
BACKEND_PID=$!

cleanup() {
  echo
  echo "Stopping backend..."
  kill -- -"$BACKEND_PID" 2>/dev/null || kill "$BACKEND_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

printf "Waiting for backend on http://localhost:8080 "
until curl -s -o /dev/null http://localhost:8080/api/presence; do
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo
    echo "[ERROR] Backend stopped. Last lines of backend.log:"
    tail -n 30 backend.log
    exit 1
  fi
  printf "."
  sleep 3
done
echo " ready!"

echo
echo "=============================================================="
echo "  ChatSpace is running!"
echo "  App:          http://localhost:5173"
echo "  Backend API:  http://localhost:8080"
echo "  DB console:   http://localhost:8080/h2-console"
echo "  Press Ctrl+C to stop everything."
echo "=============================================================="

( sleep 3; (command -v open >/dev/null && open http://localhost:5173) || (command -v xdg-open >/dev/null && xdg-open http://localhost:5173) ) >/dev/null 2>&1 &

echo "[3/3] Starting frontend..."
cd frontend
npm run dev
