# Manager Product Approval API Endpoints

## Base Path: `/manager`

### Dashboard Endpoints

#### 1. Product Approval Dashboard
```
GET /manager/dashboard
```
- **Description**: Displays all products organized by approval status
- **Access**: Manager only
- **Response**: 
  - Renders `manager/products-dashboard.html`
  - Model contains:
    - `pendingProducts`: List of products awaiting approval
    - `approvedProducts`: List of approved products
    - `rejectedProducts`: List of rejected products
- **Status Codes**:
  - `200 OK` - Successfully retrieved dashboard
  - `302 Found` - Redirects to login if not authenticated as manager

---

### Product Approval Actions

#### 2. Approve Product
```
POST /manager/products/approve/{id}
```
- **Description**: Approves a pending product by ID
- **Parameters**:
  - `id` (path) - Product ID (e.g., "P01")
- **Access**: Manager only
- **Body**: None
- **Response**:
  - Flash message: "Product '{name}' from {supplier} has been approved!"
  - Redirects to `/manager/dashboard`
- **Status Changes**: `pending` → `approved`
- **Logs**: Logs action with manager user ID

**Example:**
```
POST /manager/products/approve/P01
```

---

#### 3. Reject Product
```
POST /manager/products/reject/{id}
```
- **Description**: Rejects a pending product by ID
- **Parameters**:
  - `id` (path) - Product ID (e.g., "P01")
- **Access**: Manager only
- **Body**: None
- **Response**:
  - Flash warning: "Product '{name}' from {supplier} has been rejected!"
  - Redirects to `/manager/dashboard`
- **Status Changes**: `pending` → `rejected`
- **Logs**: Logs action with manager user ID

**Example:**
```
POST /manager/products/reject/P01
```

---

### Product Details Endpoints

#### 4. View Product Details
```
GET /manager/products/view/{id}
```
- **Description**: Displays detailed information about a specific product
- **Parameters**:
  - `id` (path) - Product ID (e.g., "P01")
- **Access**: Manager only
- **Response**:
  - Renders `manager/product-details.html`
  - Model contains:
    - `product`: Product object
    - `supplier`: Supplier user information
- **Features**:
  - Shows full product details
  - Shows supplier information
  - Conditional action buttons (approve/reject for pending only)
  - Shows submission and update timestamps

---

## Response Format

### Success Response
```json
{
  "redirect": "/manager/dashboard",
  "flashAttribute": "success",
  "message": "Product '{productName}' from {supplierName} has been approved!"
}
```

### Error Response
```json
{
  "redirect": "/manager/dashboard",
  "flashAttribute": "error",
  "message": "Product not found" | "Failed to approve product"
}
```

---

## Product Status Values

| Status | Display | Color | Meaning |
|--------|---------|-------|---------|
| `pending` | PENDING | Yellow | Awaiting manager approval |
| `approved` | APPROVED | Green | Approved and visible to customers |
| `rejected` | REJECTED | Red | Rejected by manager |

---

## Authentication & Authorization

### Required Session Attributes
```java
User user = (User) session.getAttribute("loggedInUser");
```

### Role Check
```java
"system_manager".equalsIgnoreCase(user.getRole())
```

### Access Control
- ❌ Unauthorized access → Redirects to `/login`
- ✓ Authorized manager → Processes request

---

## UI Components

### Status Badges
```html
<span class="status-badge status-pending">PENDING</span>
<span class="status-badge status-approved">APPROVED</span>
<span class="status-badge status-rejected">REJECTED</span>
```

### Action Buttons
```html
<!-- Approve Button (Green) -->
<button class="btn btn-approve">✓ Approve</button>

<!-- Reject Button (Red) -->
<button class="btn btn-reject">✗ Reject</button>

<!-- View Button (Blue) -->
<button class="btn btn-view">View Details</button>
```

### Statistics Cards
```
📊 Dashboard Statistics
├─ ⏳ Pending Approval (Count)
├─ ✓ Approved Products (Count)
└─ ✗ Rejected Products (Count)
```

---

## Error Handling

| Error | Cause | Resolution |
|-------|-------|-----------|
| 401 Unauthorized | Not logged in or not a manager | Redirect to login page |
| 404 Not Found | Product ID doesn't exist | Show error message and redirect |
| 500 Server Error | Database or system error | Log error and show user-friendly message |

---

## Usage Examples

### Workflow Example
```
1. Manager logs in with credentials
   POST /login (username=manager1, password=xxx)
   
2. Redirected to dashboard
   GET /manager/dashboard
   
3. Reviews pending products
   Sees list of products awaiting approval
   
4. Approves a product
   POST /manager/products/approve/P01
   
5. Views details of a rejected product
   GET /manager/products/view/P02
   
6. Rejects the product
   POST /manager/products/reject/P02
   
7. Returns to dashboard
   Sees updated statistics and products organized by status
```

---

## Database Queries

### ProductRepository Methods Used

```java
// Get all pending products
List<Product> pending = productRepository.findByApprovalStatus("pending");

// Get all approved products
List<Product> approved = productRepository.findByApprovalStatus("approved");

// Get all rejected products
List<Product> rejected = productRepository.findByApprovalStatus("rejected");

// Get product by ID
Optional<Product> product = productRepository.findById("P01");
```

### Supplier Information Query

```java
// Get supplier details for a product
Optional<User> supplier = userRepository.findById(product.getUserId());
```

---
