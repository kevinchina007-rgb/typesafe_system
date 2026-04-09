## Development Database Seed

This directory contains a PostgreSQL data-only dump for the local `travel_platform` database.

### Files

- `travel_platform_dev_data.sql`
  - Plain SQL data dump
  - Exported with `pg_dump --data-only --inserts --column-inserts`
  - Intended for local development and demo data recovery

### Restore order

1. Create an empty PostgreSQL database named `travel_platform`.
2. Start the backend once so schema migrations create all tables.
3. Import the data dump into the same database.

### Example restore commands

```powershell
$env:PGPASSWORD='<your-postgres-password>'
& 'E:\typesafe\postgresql\bin\psql.exe' -U postgres -h 127.0.0.1 -p 5432 -d travel_platform -f 'E:\typesafe\template\backend\data\dev-seed\travel_platform_dev_data.sql'
Remove-Item Env:PGPASSWORD
```

### Notes

- This dump contains development data only.
- The repository already carries schema and migrations in source control, so this file intentionally stores data only rather than full schema DDL.
- If schema and dump drift, rerun migrations first and then import the dump.
