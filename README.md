# Home Budget API

## Project Description
This application is built to help users manage their household budgets.

Users can check their transaction balance, view income and expense summaries, and browse filtered transaction history.

## Tech
Gradle, Java, Spring Boot, JUnit 5, Mockito, PostgreSQL, Spring Data JPA
## API
### GET /api/transactions
#### Pagination parameters
| Parameters   | Description                     | Default |
|--------------|---------------------------------|---------|
| `page`       | Zero-based page index           | 0       |
| `size`       | Number of transactions per page | 10      |

#### Example

```http
GET /api/transactions?type=EXPENSE&from=2026-10-01&to=2026-10-31&page=1&size=5
```

#### Available filtering parameters
Date format: YYYY-MM-DD (ISO 8601)

| Parameters    | Description                                                      | Example                                               |
|---------------|------------------------------------------------------------------|-------------------------------------------------------|
| None          | Returns all transactions                                         | `GET /api/transactions`                               |
| `type`        | Returns transactions of specific type [EXPENSE/INCOME]           | `GET /api/transactions?type=EXPENSE`                  |
| `date`        | Returns transactions from a specific day                         | `GET /api/transactions?date=2026-10-05`               |
| `from`        | Returns transactions on or after the specified date              | `GET /api/transactions?from=2026-10-01`               |
| `to`          | Returns transactions on or before the specified date             | `GET /api/transactions?to=2026-10-31`                 |
| `from` + `to` | Returns transactions within the specified date range (inclusive) | `GET /api/transactions?from=2026-10-01&to=2026-10-31` |

#### Sorting
All data returned by this endpoint is automatically sorted by transactionDate in descending order, then by id in descending order when transactionDate values are identical.

#### The type filter can be combined with any valid date filter configuration.

### Response Structure
The endpoint returns a paginated response containing:
- content — transactions on the requested page
- totalElements — total number of matching transactions
- totalPages — total number of pages
- number — current page index
- size — requested page size

### Error responses

The API returns `400 Bad Request` when:
- `date` is combined with `from` or `to`.
- `from` is later than `to`.
- A date has an invalid format.
- `type` is invalid.
- Negative page parameter value.
- size parameter value less than 1.

Requesting a page outside the available range returns 200 OK with an empty content array.

### GET /api/transactions/summary
#### Available parameters
Date format: YYYY-MM-DD (ISO 8601)

| Parameters    | Description                                                 | Example                                                       |
|---------------|-------------------------------------------------------------|---------------------------------------------------------------|
| None          | Returns the summary for all transactions.                   | `GET /api/transactions/summary`                               |
| `from`        | Returns summary on or after the specified date              | `GET /api/transactions/summary?from=2026-10-01`               |
| `to`          | Returns summary on or before the specified date             | `GET /api/transactions/summary?to=2026-10-31`                 |
| `from` + `to` | Returns summary within the specified date range (inclusive) | `GET /api/transactions/summary?from=2026-10-01&to=2026-10-31` |

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

## Error Handling

The API uses centralized exception handling to provide consistent error responses for invalid requests and missing resources.

### Error Response Format

For most request errors, the API returns a JSON response with the following structure:

```json
{
  "timestamp": "2026-10-27T17:13:41Z",
  "status": 400,
  "message": "Invalid value for parameter: date",
  "path": "/api/transactions"
}
```

| Field         | Description                          |
|---------------|--------------------------------------|
| `timestamp`   | Time when the error occurred (UTC).  |
| `status`      | HTTP status code.                    |
| `message`     | Description of the error.            |
| `path`        | Request path that caused the error.  |

### Supported Error Responses

| HTTP Status        | Description                                                                    |
|--------------------|--------------------------------------------------------------------------------|
| `400 Bad Request`  | Invalid query parameters, incorrect date ranges, or invalid request arguments. |
| `404 Not Found`    | Requested transaction does not exist.                                          |

**Note:** Bean Validation errors use a separate response format containing field-specific validation messages.