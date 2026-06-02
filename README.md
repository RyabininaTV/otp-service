# OTP Service - Сервис одноразовых кодов подтверждения

## 📋 О проекте

**OTP Service** — это backend-приложение для генерации и отправки одноразовых паролей (OTP) с целью защиты операций, требующих подтверждения. Проект разработан для компании **Promo IT** в рамках учебной программы и представляет собой реальную задачу от заказчика.

### Основной функционал

- 🔐 **Аутентификация и авторизация** с использованием JWT-токенов
- 👥 **Ролевая модель** (ADMIN / USER)
- 🔢 **Генерация OTP-кодов** с настраиваемой длиной и временем жизни
- 📧 **Отправка кодов** через Email (симулятор), SMS (эмулятор SMPP), Telegram, файл
- ✅ **Валидация OTP-кодов** с отслеживанием статусов (ACTIVE, EXPIRED, USED)
- ⏰ **Автоматическое истечение срока действия** кодов (планировщик)
- 🛠️ **Административные API** для управления пользователями и конфигурацией

---

## 🏗️ Технологический стек

- Java 17
- Gradle 8+
- H2 Database (in-memory)
- HikariCP
- JJWT (JWT токены)
- com.sun.net.httpserver
- Jackson
- SLF4J + Logback
- Jakarta Mail (Email)
- JSMPP (SMS/SMPP)
- Java HTTP Client (Telegram)

---

## 📁 Структура проекта

```
otp-service/
├── build.gradle # Конфигурация сборки
├── settings.gradle # Настройки Gradle
├── README.md # Документация
├── otp_codes.log # Файл с сгенерированными кодами
└── src/main/
├── java/com/promoit/otp/
│ ├── App.java # Точка входа
│ ├── config/
│ │ └── DatabaseConfig.java # Конфигурация БД (H2)
│ ├── controller/
│ │ ├── AuthController.java # API аутентификации
│ │ ├── UserController.java # API пользователя
│ │ └── AdminController.java # API администратора
│ ├── service/
│ │ ├── AuthService.java # Сервис аутентификации
│ │ ├── OtpService.java # Сервис OTP
│ │ ├── EmailService.java # Отправка email
│ │ ├── SmsService.java # Отправка SMS (эмулятор)
│ │ ├── TelegramService.java # Отправка в Telegram
│ │ └── FileService.java # Сохранение в файл
│ ├── dao/
│ │ ├── UserDao.java # Работа с пользователями
│ │ ├── OtpDao.java # Работа с OTP-кодами
│ │ └── OtpConfigDao.java # Работа с конфигурацией
│ ├── model/
│ │ ├── User.java # Модель пользователя
│ │ ├── OtpCode.java # Модель OTP-кода
│ │ ├── OtpConfig.java # Модель конфигурации
│ │ ├── Role.java # Enum ролей
│ │ └── OtpStatus.java # Enum статусов
│ ├── util/
│ │ ├── JwtUtil.java # Работа с JWT
│ │ ├── PasswordUtil.java # Хеширование паролей
│ │ └── OtpGenerator.java # Генерация OTP
│ └── scheduler/
│ └── OtpExpiryScheduler.java # Планировщик истечения
└── resources/
└── logback.xml # Конфигурация логирования
```

---

## 🚀 Установка и запуск

### Требования

- **Java 17** или новее
- **Gradle** 8+ (можно использовать встроенный wrapper)
- **Git** (для клонирования репозитория)

### Шаг 1. Клонирование репозитория

```bash
git clone https://github.com/YOUR_USERNAME/otp-service.git
cd otp-service
```

### Шаг 2. Сборка проекта

```bash
gradle clean build
```

### Шаг 3. Запуск приложения

```bash
gradle run
```

После запуска увидите:

```bash
OTP Service started successfully on port 8080
```

## 🔐 Безопасность

- Пароли хешируются с использованием SHA-256 + соль
- JWT-токены имеют срок действия 24 часа
- Разграничение доступа на основе ролей (ADMIN / USER)
- OTP-коды имеют ограниченное время жизни (настраиваемое)
- Логирование всех запросов для аудита

## 📝 Логирование

Логи пишутся в два места:
- Консоль — цветной вывод в реальном времени
- Файл logs/otp-service.log — ротация ежедневно