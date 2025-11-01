# Latest Changes Summary

## Date: 2025-11-01

### 1. Added Time Validation to Service Request System

#### Modified: `ServiceRequestService.java`
- **Method:** `isAvailable(Municipality, LocalDate, LocalTime)`
- **Change:** Added validation to ensure time slots are within service hours (09:00 - 18:00)
- **Logic:**
  - Time slots before 09:00 → Returns `false`
  - Time slots at or after 18:00 → Returns `false`
  - Time slots between 09:00 and 17:59 → Proceeds with other validations
- **Logging:** Added info log when time is outside service hours

### 2. Added New API Endpoint for User Requests

#### Modified: `ServiceRequestRestController.java`
- **New Endpoint:** `GET /requests/user/{username}`
- **Purpose:** Retrieve all service requests for a specific user
- **Response:**
  - Success (200): List of ServiceRequest objects
  - Not Found (404): User doesn't exist
  - Empty list (200): User exists but has no requests
- **Usage:** Used by citizen portal to display "My Requests" section

### 3. Added Integration Tests for New Endpoint

#### Modified: `ServiceRequestIT.java`
- **testGetRequestsByUser_Success()**
  - Creates a service request
  - Retrieves it by username
  - Validates user and municipality data

- **testGetRequestsByUser_NotFound()**
  - Tests behavior with non-existent user
  - Expects 404 status

- **testGetRequestsByUser_EmptyList()**
  - Tests user with no requests
  - Expects 200 with empty list

### 4. Added Unit Tests for Time Validation

#### Modified: `ServiceRequestServiceTest.java`
Added 7 new test cases:

- **testIsAvailable_TimeBeforeServiceHours_ReturnsFalse()**
  - Time: 08:30
  - Expected: false

- **testIsAvailable_TimeAfterServiceHours_ReturnsFalse()**
  - Time: 18:30
  - Expected: false

- **testIsAvailable_TimeExactly18_ReturnsFalse()**
  - Time: 18:00
  - Expected: false (18:00 is not included in service hours)

- **testIsAvailable_TimeExactly9_ReturnsTrue()**
  - Time: 09:00
  - Expected: true (09:00 is included in service hours)

- **testIsAvailable_TimeWithinServiceHours_ReturnsTrue()**
  - Time: 14:30
  - Expected: true

- **testIsAvailable_Time1759_ReturnsTrue()**
  - Time: 17:59
  - Expected: true (last valid minute)

- **testIsAvailable_Time0859_ReturnsFalse()**
  - Time: 08:59
  - Expected: false (one minute before service starts)

## Test Results

### Unit Tests
✅ All 8 `isAvailable` tests passed
- Validates Sunday restriction
- Validates time range (09:00-18:00)
- Validates time slot conflicts

### Integration Tests
✅ All 3 new endpoint tests passed
- Successfully creates and retrieves user requests
- Handles non-existent users correctly
- Returns empty list for users without requests

## Business Rules Enforced

1. **No service on Sundays** (existing)
2. **Service hours: 09:00 - 18:00** (NEW)
   - 09:00 is included (valid)
   - 18:00 is excluded (invalid)
3. **Time slots must be 1 hour apart** (existing)

## Impact on Web Interface

The web interface already has client-side validation for the 09:00-18:00 range using HTML5 time input. Now this validation is also enforced server-side, providing:
- Defense in depth (client + server validation)
- Protection against direct API calls
- Consistent business rules across all entry points

## Files Modified

1. `src/main/java/org/hw1/service/ServiceRequestService.java`
2. `src/main/java/org/hw1/boundary/ServiceRequestRestController.java`
3. `src/test/java/org/hw1/ServiceRequestIT.java`
4. `src/test/java/org/hw1/ServiceRequestServiceTest.java`

## API Documentation Update

### New Endpoint

```
GET /requests/user/{username}
```

**Parameters:**
- `username` (path parameter) - The name of the user

**Responses:**
- `200 OK` - Returns list of ServiceRequest objects
- `404 Not Found` - User doesn't exist

**Example:**
```bash
GET /requests/user/John%20Doe
```

**Response:**
```json
[
  {
    "id": 1,
    "token": "abc-123-def-456",
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "municipality": {
      "id": 5,
      "name": "Lisboa"
    },
    "requestedDate": "2025-11-05",
    "timeSlot": "14:00",
    "description": "Old furniture",
    "lastUpdate": "2025-11-01T13:30:00"
  }
]
```

## Running the Tests

```bash
# Run all isAvailable tests
mvn test -Dtest=ServiceRequestServiceTest#testIsAvailable*

# Run new endpoint tests
mvn test -Dtest=ServiceRequestIT#testGetRequestsByUser*

# Run all tests
mvn test
```

## Notes

- All tests pass successfully
- No breaking changes to existing functionality
- Backward compatible with existing client code
- Time validation now consistent between frontend and backend