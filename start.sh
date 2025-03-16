#!/bin/bash

# Переменные путей
BACKEND_DIR="./backend"
FRONTEND_DIR="./frontend"

# Функция для запуска бэкенда
start_backend() {
  echo "Запускаем Spring Boot backend..."
  cd "$BACKEND_DIR" || exit
  ./mvnw spring-boot:run &
  BACKEND_PID=$!
  echo "Backend запущен (PID: $BACKEND_PID)"
  cd - > /dev/null
}

# Функция для запуска фронтенда
start_frontend() {
  echo "Запускаем React frontend..."
  cd "$FRONTEND_DIR" || exit
  npm install
  npm start &
  FRONTEND_PID=$!
  echo "Frontend запущен (PID: $FRONTEND_PID)"
  cd - > /dev/null
}

# Запуск обоих сервисов
start_backend
start_frontend

# Ожидание нажатия Ctrl+C для завершения
trap "echo 'Останавливаем сервисы...'; kill $BACKEND_PID $FRONTEND_PID; exit" INT

wait
