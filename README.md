# Argonomix — Personal Finance Manager

A full-featured personal finance management desktop application built with **Java**, **JavaFX**, and **MySQL**. Argonomix provides a clean, modern UI for tracking expenses, managing budgets, monitoring investments, and scoring your overall financial health.

---

## Features

- **Dashboard** — real-time overview of income, expenses, savings rate, and net worth
- **Transaction Management** — add, edit, delete, filter transactions with CSV import/export
- **Budget Tracking** — set category budgets and track spending against limits with progress indicators
- **Savings Goals** — create goals with target amounts and deadlines, track progress visually
- **Investment Portfolio** — log investments, track current values, and view returns
- **Financial Health Score** — algorithmic score based on savings rate, budget adherence, and spending patterns
- **"Should I Buy This?" Calculator** — calculates real cost in working hours and opportunity cost
- **Bill Split Calculator** — split shared expenses between multiple people
- **Subscription Audit** — track recurring subscriptions and identify unnecessary spend
- **Smart Savings Challenges** — gamified savings goals to build habits
- **Reports & Analytics** — line charts, bar charts, pie charts with PDF and CSV export
- **User Authentication** — secure login and registration with BCrypt password hashing
- **Settings** — personalise income, hourly wage, preferences

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 17.0.2 |
| Database | MySQL 8.0 |
| Build Tool | Maven |
| Password Hashing | jBCrypt |
| CSV Handling | OpenCSV |
| PDF Export | iText 7 |
| UI Controls | ControlsFX |

---

## Project Structure

```
argonomix-finance-manager/
├── src/main/java/com/argonomix/
│   ├── Main.java                    # Application entry point
│   ├── controllers/                 # JavaFX controllers (one per screen)
│   │   ├── DashboardController.java
│   │   ├── TransactionController.java
│   │   ├── BudgetController.java
│   │   ├── GoalController.java
│   │   ├── InvestmentController.java
│   │   ├── ReportsController.java
│   │   └── ...
│   ├── models/                      # Entity classes
│   │   ├── User.java
│   │   ├── Transaction.java
│   │   ├── Budget.java
│   │   └── ...
│   ├── database/                    # DAO layer — all DB operations
│   │   ├── DatabaseConnection.java
│   │   ├── UserDAO.java
│   │   ├── TransactionDAO.java
│   │   └── ...
│   └── utils/                       # Helpers and services
│       ├── FinancialCalculations.java
│       ├── SessionManager.java
│       ├── NavigationUtil.java
│       └── NotificationService.java
├── src/main/resources/
│   ├── fxml/                        # UI layout files (15 screens)
│   ├── css/modular.css              # Application stylesheet
│   └── database.properties          # DB config (not committed — see .env.example)
├── database/
│   └── schema.sql                   # Full MySQL schema (11 tables)
├── .env.example                     # Template for database credentials
└── pom.xml                          # Maven dependencies
```

---

## Database Schema

The MySQL schema consists of 11 normalised tables:

- `users` — user accounts with hashed passwords
- `transactions` — income and expense records with category and date
- `budgets` — per-category monthly budget limits
- `goals` — savings goals with targets and deadlines
- `investments` — investment records with current valuations
- `subscriptions` — recurring subscription tracking
- `challenges` — savings challenge records
- `bill_split_groups`, `group_members`, `shared_expenses`, `expense_splits` — bill splitting

All tables use foreign key constraints with `ON DELETE CASCADE`, indexed on commonly queried columns.

---

## Setup and Installation

### Prerequisites

- Java 17 or higher
- MySQL 8.0
- Maven 3.6+

### 1. Clone the repository

```bash
git clone https://github.com/rajasuleman7/argonomix-finance-manager.git
cd argonomix-finance-manager
```

### 2. Set up the database

```bash
mysql -u root -p < database/schema.sql
```

### 3. Configure database credentials

Copy `.env.example` as `src/main/resources/database.properties` and fill in your credentials:

```properties
db.url=jdbc:mysql://localhost:3306/argonomix_db?useSSL=false&serverTimezone=UTC
db.user=your_mysql_username
db.password=your_mysql_password
```

### 4. Build and run

```bash
mvn clean compile
mvn javafx:run
```

Or import as a Maven project in IntelliJ IDEA and run `com.argonomix.Main`.

---

## Architecture

The application follows the **MVC (Model-View-Controller)** pattern:

- **Models** — Java POJOs representing database entities
- **Views** — FXML files defining UI layouts, styled with CSS
- **Controllers** — JavaFX controllers handling user interaction and binding to DAOs
- **DAO Layer** — data access objects encapsulating all SQL queries
- **SessionManager** — singleton managing the logged-in user across screens

---

## Key Technical Implementations

- **BCrypt hashing** for secure password storage — no plaintext passwords
- **DAO pattern** with parameterised queries to prevent SQL injection
- **Observable collections** for reactive JavaFX table updates
- **Multi-type chart rendering** — LineChart for trends, BarChart for comparisons, PieChart for category breakdown
- **CSV parsing with OpenCSV** supporting bulk transaction import
- **iText PDF generation** for downloadable financial reports
- **Financial health scoring algorithm** considering savings rate, budget adherence, and spending diversity

---

## Future Improvements

- Modal dialogs for inline form entry (currently via separate screens)
- Dark mode theme toggle
- Multi-currency support
- Notification dropdown panel in the layout header
- Recurring transaction scheduling
