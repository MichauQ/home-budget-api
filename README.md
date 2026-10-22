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

| Parameters    | Description                                                      | Example                                               |
|---------------|------------------------------------------------------------------|-------------------------------------------------------|
| None          | Returns all transactions                                         | `GET /api/transactions`                               |
| `type`        | Returns transactions of specific type [EXPENSE/INCOME]           | `GET /api/transactions?type=EXPENSE`                  |
| `date`        | Returns transactions from a specific day                         | `GET /api/transactions?date=2026-10-05`               |
| `from`        | Returns transactions on or after the specified date              | `GET /api/transactions?from=2026-10-01`               |
| `to`          | Returns transactions on or before the specified date             | `GET /api/transactions?to=2026-10-31`                 |
| `from` + `to` | Returns transactions within the specified date range (inclusive) | `GET /api/transactions?from=2026-10-01&to=2026-10-31` |

#### All data returned by this endpoint is automatically sorted by transactionDate in descending order, then by id in descending order when transactionDate values are identical.
#### Type parameter can be used with any other parameters.
### Error responses

The API returns `400 Bad Request` when:
- `date` is combined with `from` or `to`.
- `from` is later than `to`.
- A date has an invalid format.
- `type` is invalid.

### GET /api/transactions/summary
#### Available parameters
Date format: YYYY-MM-DD (ISO 8601)

| Parameters   | Description                                                 | Example                                                       |
|--------------|-------------------------------------------------------------|---------------------------------------------------------------|
| None         | Returns the summary for all transactions.                   | `GET /api/transactions/summary`                               |
| `from`       | Returns summary on or after the specified date              | `GET /api/transactions/summary?from=2026-10-01`               |
| `to`         | Returns summary on or before the specified date             | `GET /api/transactions/summary?to=2026-10-31`                 |
| `from` + `to`| Returns summary within the specified date range (inclusive) | `GET /api/transactions/summary?from=2026-10-01&to=2026-10-31` |

### Error responses
The API returns `400 Bad Request` when:
- `from` is later than `to`.
- A date has an invalid format.

### GET /api/transactions/summary/expenses-by-category

Returns the total amount of expenses grouped by transaction category.
- Includes only transactions of type EXPENSE.
- Results are sorted by total expense amount in descending order.
- If no expenses exist, the endpoint returns 200 OK with an empty JSON array ([]).
### Example
```json
[
    {
        "category": "FOOD",
        "total": 277.50
    },
    {
        "category": "TRANSPORT",
        "total": 185.00
    },
    {
        "category": "OTHER",
        "total": 125.00
    }
]
```
