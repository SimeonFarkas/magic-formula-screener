# Magic formula screener

Application that automates stock screening on the Stockholm Stock Exchange (Nasdaq Stockholm) based on Joel Greenblatt's
investment strategy, "The Magic Formula".

## Quickstart

The local database is PostgreSQL inside a Docker-container. Before running the program start the container with the
following in the terminal:

```bash
docker compose up -d

# 1. Start the client
./start-db-client.sh

# Alt. Podman:
podman exec -it mfs-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
```

Link to the script to start the client: [start-db-client.sh](./start-db-client.sh).

```bash
# 2. List tables and some useful commands
- List all tables, views, and sequences in current database
\dt

- List tables including system tables / full details
\dt+

- Describe columns, types, and constraints for a specific table
\d table_name;
\d companies;

-- List all databases
\l

-- Quit psql
\q
```

When you are done with the program, shut down the Docker-container with:

```bash
docker compose stop
```

To run the program (mvn clean exec:exec):

```bash
./bigmoneybaby.sh
```

Link to the script to start the program [bigmoneybaby.sh](./bigmoneybaby.sh).
## Components of this program

### Infrastructure

* **Start Button (`Main.java`)**

  This is the programs entrypoint. It checks the database information (host, port, password), puzzles the connection
  together with the connection link (`jdbc:postgresql//...`) and tells `DatabaseMigrator` to update the database.

* **Database Starter (`DatabaseMigrator.java`)**

  A helper class that starts Flyway. It takes the database link and password from `Main.java`, searches for the SQL-file
  and runs the database.

* **SQL-script (`src/main/resources/db/migration/`)**

  The map where the SQL-files are (`V1__create-companies-table.sql`, etc.). These are the files that decides how the
  tables in the database should look like.

* **Database Container (`docker-compose.yml`)**

  An instruction file for Docker. It starts a ready PostgreSQL-database locally on your computer with the right
  settings, so you don't need to install Postgres to your operative system.

### Core Domain & Logic (Upcoming)

* **Data Ingestion / API**

  The code that extracts the financial data for the Swedish companies.

* **Data Models**

  Classes in Java that acts like templates for the data. A `record` keeps order on ex. what a company or quarterly
  report contains when the codes work it.

* **Magic Formula Engine**

  The brain of the program. It takes the numbers from the database, calculates the key figures `Return on Capital (ROC)`
  and `Earnings Yield (EY)`, does the equation and gives a list of companies that meet the requirements.