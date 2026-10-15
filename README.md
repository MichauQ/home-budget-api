# Home Budget API

## Project Description
This application is built to help users manage their household budgets.

Users can check their transaction balance, view income and expense summaries, and browse filtered transaction history.

## Tech
Gradle, Java, Spring Boot, JUnit 5, Mockito, PostgreSQL, Spring Data JPA
## API
### GET /api/transactions
#### Available parameters
Date format: YYYY-MM-DD (ISO 8601)

| Parameters   | Description                                                      | Example                                              |
|--------------|------------------------------------------------------------------|------------------------------------------------------|
| None         | Returns all transactions                                         | `GET /api/transactions`                              |
| `date`       | Returns transactions from a specific day                         | `GET /api/transactions?date=2026-10-05`              |
| `from`       | Returns transactions on or after the specified date              | `GET /api/transactions?from=2026-10-01`              |
| `to`         | Returns transactions on or before the specified date             | `GET /api/transactions?to=2026-10-31`                |
| `from` + `to`| Returns transactions within the specified date range (inclusive) | `GET /api/transactions?from=2026-10-01&to=2026-10-31` |

### Error responses

The API returns `400 Bad Request` when:
- `date` is combined with `from` or `to`.
- `from` is later than `to`.
- A date has an invalid format.
