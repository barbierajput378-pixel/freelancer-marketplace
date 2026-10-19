# Freelancer Marketplace — Java JDBC Desktop Client

A Java **Swing** desktop GUI that manages the existing **FreelancerMarketplace**
MySQL database (MySQL 8.4 on Aiven) over **JDBC**. It is a standalone alternative
to the web app in the parent folder and talks to the **same database** — it does
not create or modify the schema, which already exists.

## Architecture

```
┌─────────────────────┐
│   Swing GUI (ui/)   │   JTabbedPane: one tab per table + Dashboard
└──────────┬──────────┘
           │ plain Java calls
┌──────────▼──────────┐
│   DAO layer (dao/)  │   ClientDAO, FreelancerDAO, JobDAO, OfferDAO,
│                     │   ContractDAO, ReportDAO
└──────────┬──────────┘
           │ PreparedStatement / CallableStatement
┌──────────▼──────────┐
│  JDBC (DBConnection)│   DriverManager + try-with-resources
└──────────┬──────────┘
           │ jdbc:mysql://…?sslMode=REQUIRED
┌──────────▼──────────┐
│  MySQL on Aiven     │   FreelancerMarketplace (schema already created)
└─────────────────────┘
```

- **ui/** builds the window. Each table tab has a form for every column, the
  buttons Add / Update / Delete / Clear / Refresh, and a `JTable` of all rows.
  Clicking a row fills the form; Delete asks for confirmation; input is validated
  and database/validation errors are shown in friendly dialogs (including the SQL
  state and message, e.g. a UNIQUE violation on `Contracts.job_id` or a foreign-key
  error).
- **dao/** holds one DAO per table plus `ReportDAO` for the Dashboard. Every query
  uses a `PreparedStatement` (or `CallableStatement`) with `?` placeholders — no
  string concatenation of values — wrapped in try-with-resources.
- **db/DBConnection** is the single place that opens JDBC connections. All
  settings come from **environment variables**; nothing is hardcoded.

## Prerequisites

- **JDK 17 or newer** — check with `java -version`
- **Apache Maven 3.8+** — check with `mvn -version`
- Network access to the Aiven MySQL service and its connection details.

The MySQL JDBC driver (`com.mysql:mysql-connector-j`) is pulled in automatically
by Maven — you do not install it manually.

## 1. Set the five environment variables (Windows PowerShell)

The app reads `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`.
**No password is ever stored in the project** — you set it in your shell.

Set them for the **current PowerShell session** (they disappear when you close the window):

```powershell
$env:DB_HOST     = "your-service-name.aivencloud.com"
$env:DB_PORT     = "your-port-number"
$env:DB_USER     = "avnadmin"
$env:DB_PASSWORD = "your-aiven-password"
$env:DB_NAME     = "FreelancerMarketplace"
```

(Get the actual values from the Aiven service's **Connection Information** page —
the same ones already used by the web app's Vercel environment variables.)

To persist them across sessions for your Windows user (optional), use:

```powershell
setx DB_HOST     "your-service-name.aivencloud.com"
setx DB_PORT     "your-port-number"
setx DB_USER     "avnadmin"
setx DB_PASSWORD "your-aiven-password"
setx DB_NAME     "FreelancerMarketplace"
```

> `setx` only affects **new** PowerShell windows — open a fresh one afterwards.
> Verify with `echo $env:DB_HOST`.

## 2. Build and run

All commands are run from inside the `java-jdbc-app/` folder:

```powershell
cd "java-jdbc-app"
```

### Option A — compile and run from source (quickest)

```powershell
mvn compile exec:java
```

### Option B — build one runnable jar and run it

```powershell
mvn clean package
java -jar target/freelancer-jdbc-app.jar
```

The `maven-shade-plugin` bundles the JDBC driver into
`target/freelancer-jdbc-app.jar`, so the single jar runs on its own.

On startup the app tests the connection; if the variables are missing or wrong,
you get a clear dialog explaining what to fix.

## Using the app

- **Clients / Freelancers / Jobs / Offers / Contracts** tabs — full CRUD. Fill the
  form and click **Add** / **Update** / **Delete**; **Clear** empties the form,
  **Refresh** reloads the table. Click any row to load it into the form.
- **Dashboard / Reports** tab:
  - *Open jobs, budget ≥ 70000*
  - *Freelancers with ≥ 1 offer*
  - *Completed contracts per freelancer*
  - *GetJobsByStatus* — stored procedure via `CallableStatement`, with a status dropdown
  - *CountOffers(job_id)* and *AverageContractAmount()* — stored functions via `SELECT`

### A note on triggers

The database has triggers that fire automatically, so some results change on
their own after you save. For example: inserting a Job uppercases its title;
inserting/deleting a Contract flips the related Job's status to `Awarded`/`Open`;
updating a Freelancer's rating above 5 is capped to 5. Click **Refresh** to see
the post-trigger state.

## File guide

| File | Purpose |
|------|---------|
| `pom.xml` | Maven build: Java 17, `mysql-connector-j`, exec + shade plugins |
| `src/main/java/com/freelancer/Main.java` | Entry point; sets look-and-feel, tests connection, opens the window |
| `db/DBConnection.java` | Builds the JDBC URL from env vars and opens connections |
| `model/Client, Freelancer, Job, Offer, Contract` | Plain POJOs, one per table |
| `dao/ClientDAO … ContractDAO` | CRUD per table using `PreparedStatement` |
| `dao/ReportDAO.java` | Dashboard queries, `CallableStatement`, function calls |
| `dao/QueryResult.java` | Generic column+rows holder for arbitrary result sets |
| `ui/MainFrame.java` | The `JTabbedPane` window |
| `ui/ClientPanel … ContractPanel` | One CRUD tab per table |
| `ui/DashboardPanel.java` | The reports tab |
| `ui/UiUtils.java` | Shared helpers: validation, error dialogs, table styling |
```
