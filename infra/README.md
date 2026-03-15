# StorePulse Infra

This folder contains the local MVP stack described in `docs/architecture.md`:
- PostgreSQL
- Spring Boot API
- React web client
- sample CSV files for import testing

## Quick start

1. Copy the environment template:

```powershell
Copy-Item .\infra\.env.example .\infra\.env
```

2. Start the stack:

```powershell
docker compose --env-file .\infra\.env -f .\infra\docker-compose.yml up --build
```

3. Open the web client:

```text
http://localhost:4173
```

4. Sign in with the bootstrap admin from `.env`:

```text
username: admin
password: storepulse-admin
```

5. Upload a sample CSV from `infra/sample-data/`.

## Services

- Web: `http://localhost:4173`
- API: `http://localhost:8080`
- Postgres: `localhost:5432`

## Sample files

- `infra/sample-data/sales-sample-valid.csv`
- `infra/sample-data/sales-sample-with-errors.csv`

The second file is useful for testing row-level error handling and partial-success imports.

## Data volumes

- Postgres data is stored in the `storepulse_pg` Docker volume.
- Imported CSV files are stored under the repo `data/imports/` folder, mounted into the API container.

## Notes

- `STOREPULSE_ALLOWED_ORIGIN` should match the web URL exposed to the browser.
- `VITE_API_BASE_URL` is baked into the web image at build time.
- The default stack is single-node and matches the MVP architecture constraints.
