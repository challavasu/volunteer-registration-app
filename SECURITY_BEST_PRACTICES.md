# Security Best Practices for Developers

## Quick Reference Guide

This guide provides practical security practices to follow when developing or maintaining the Volunteer Registration Application.

---

## 1. Form Handling & CSRF Protection

### ✓ DO: Add CSRF Token to Forms

When creating HTML forms that modify data (POST, PUT, DELETE):

```html
<form method="post" action="/admin/users">
    <!-- Add CSRF token -->
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
    
    <input type="text" name="username" required minlength="3" maxlength="100">
    <input type="email" name="email" required>
    <button type="submit">Save</button>
</form>
```

### ✗ DON'T: Forget CSRF Tokens

```html
<!-- Bad: Missing CSRF token -->
<form method="post" action="/admin/users">
    <input type="text" name="username">
    <button type="submit">Save</button>
</form>
```

---

## 2. JavaScript & API Calls

### ✓ DO: Include CSRF Token in Fetch Requests

```javascript
// Get CSRF token from meta tag or form
const token = document.querySelector('meta[name="_csrf"]')?.content || 
              document.querySelector('input[name="_csrf"]')?.value;

async function saveData(data) {
    try {
        const response = await fetch('/api/data', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': token  // Include CSRF token
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) throw new Error(response.statusText);
        return await response.json();
    } catch (error) {
        console.error('Request failed:', error);
        throw error;
    }
}
```

### ✗ DON'T: Forget CSRF Token in AJAX

```javascript
// Bad: No CSRF token
fetch('/api/data', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
});
```

---

## 3. Input Validation

### ✓ DO: Validate Input on Client and Server

**Server-side (Java):**
```java
import jakarta.validation.constraints.*;

public class UserDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100)
    private String username;
    
    @Email
    @NotBlank
    private String email;
    
    @Size(min = 8, max = 255)
    private String password;
}

// In Controller:
@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO dto) {
    // dto is guaranteed to be valid here
    userService.createUser(dto);
    return ResponseEntity.ok("User created");
}
```

**Client-side (HTML):**
```html
<input type="email" name="email" required>
<input type="text" name="username" required minlength="3" maxlength="100">
<input type="password" name="password" required minlength="8" maxlength="255">
```

### ✗ DON'T: Skip Validation

```java
// Bad: No validation
@PostMapping
public ResponseEntity<?> createUser(@RequestBody String username) {
    userService.createUser(username);  // Could be anything!
    return ResponseEntity.ok("User created");
}
```

---

## 4. Output Encoding & XSS Prevention

### ✓ DO: Escape HTML Output

**In Thymeleaf Templates (automatic escaping):**
```html
<!-- Automatically escaped -->
<h1 th:text="${user.name}">Name</h1>

<!-- If you need to disable (use only for trusted content) -->
<div th:utext="${trustedHtml}"></div>
```

**In JavaScript:**
```javascript
// Always use InputValidator to sanitize user data
import { InputValidator } from './util/InputValidator';

function displayUserData(userData) {
    // Escape HTML special characters
    const safeName = InputValidator.sanitizeHtml(userData.name);
    document.getElementById('userName').textContent = safeName;  // Safe
}
```

**Good way to set HTML:**
```javascript
// Use sanitization for user-provided HTML
const userComment = getUserComment();  // e.g., "<img src=x onerror=alert('xss')>"
const sanitized = InputValidator.sanitizeHtml(userComment);
element.innerHTML = sanitized;  // Safe: becomes "&lt;img src=x..."
```

### ✗ DON'T: Trust User Input

```html
<!-- Bad: Direct output -->
<h1>Welcome, ${user.name}</h1>  <!-- Could execute scripts! -->
<div th:utext="${userInput}"></div>  <!-- DANGEROUS -->
```

```javascript
// Bad: Using innerHTML with user data
element.innerHTML = userInput;  // Dangerous!
document.write(userInput);      // Dangerous!
```

---

## 5. Logging & Error Handling

### ✓ DO: Log Errors Without Sensitive Data

```java
// Good: Generic error message
try {
    // authentication logic
} catch (Exception e) {
    logger.error("Authentication attempt failed");  // Generic
    return ResponseEntity.status(401).body("Invalid credentials");  // Generic
}
```

### ✗ DON'T: Log Sensitive Information

```java
// Bad: Logs user credentials
System.out.println("User: " + username + " logged in with password: " + password);
logger.error("User not found: " + username + ", Email: " + email);

// Bad: Exposes system details
logger.error("SQL Error: " + e.getMessage());  // Could expose table names, structure
```

---

## 6. Authentication & Session Management

### ✓ DO: Use Secure Session Handling

```java
@PostMapping("/login")
public String login(@RequestParam String username, 
                   @RequestParam String password,
                   HttpSession session) {
    Optional<User> user = authService.authenticate(username, password);
    
    if (user.isPresent()) {
        // Session is automatically created and secure cookies are set
        session.setAttribute("userId", user.get().getId());
        session.setAttribute("userRole", user.get().getRole());
        return "redirect:/admin";
    }
    
    return "redirect:/login?error=true";
}
```

### ✗ DON'T: Disable Session Security

```java
// Bad: Disabling HTTPS-only cookies
// Don't set: server.servlet.session.cookie.secure=false  (unless development)

// Bad: Setting very long timeouts
session.setMaxInactiveInterval(Integer.MAX_VALUE);  // Never expires!

// Bad: Disabling HttpOnly flag
response.addCookie(new Cookie("sessionId", sessionValue));  // No HttpOnly!
```

---

## 7. Database & SQL

### ✓ DO: Use Parameterized Queries (JPA/Hibernate)

```java
// Good: Using JPA repository methods
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}

// Good: Using JPA Query with named parameters
@Query("SELECT u FROM User u WHERE u.username = :username AND u.active = true")
Optional<User> findActiveUserByUsername(@Param("username") String username);
```

### ✗ DON'T: Use String Concatenation for SQL

```java
// Bad: SQL injection vulnerability!
String query = "SELECT * FROM users WHERE username = '" + username + "'";
entityManager.createNativeQuery(query).getResultList();

// Bad: Vulnerable to injection
List<User> users = userRepository.findByQuery(
    "SELECT * FROM users WHERE email = '" + email + "'"
);
```

---

## 8. Password Handling

### ✓ DO: Use BCrypt for Password Hashing

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// In service:
public User createUser(String username, String password) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));  // Hashed!
    return userRepository.save(user);
}
```

### ✗ DON'T: Store Plain Text Passwords

```java
// Bad: Storing plain text
user.setPassword(password);  // NEVER DO THIS!

// Bad: Using weak hashing (MD5, SHA1)
user.setPassword(DigestUtils.md5Hex(password));
```

---

## 9. API Security

### ✓ DO: Validate and Authorize API Requests

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO dto,
            HttpSession session) {
        
        // Check authorization
        User.UserRole role = (User.UserRole) session.getAttribute("userRole");
        if (role != User.UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN users can create users");
        }
        
        // Validate input
        if (!InputValidator.isValidUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid username format");
        }
        
        return ResponseEntity.ok(userService.createUser(dto));
    }
}
```

### ✗ DON'T: Skip Authorization Checks

```java
// Bad: No authorization check
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody UserDTO dto) {
    return ResponseEntity.ok(userService.createUser(dto));  // Anyone can create users!
}
```

---

## 10. Error Messages

### ✓ DO: Use Generic Error Messages

```javascript
// Good: Generic message shown to user
showToast('Invalid username or password', 'error');

// Server logs detailed information
logger.warn("Login attempt failed: username not found in active users");
```

### ✗ DON'T: Expose System Details

```javascript
// Bad: Exposes user existence
alert('User with email ' + email + ' does not exist');

// Bad: Exposes internal structure
alert('Database connection failed: ' + error.message);

// Bad: Exposes validation details
alert('Username already taken');  // Confirms username exists
```

---

## 11. Dependency Updates

### Regular Maintenance

```bash
# Check for vulnerable dependencies
mvn org.owasp:dependency-check-maven:check

# Update dependencies
mvn versions:use-latest-releases

# Run security tests
mvn clean test
```

---

## 12. Security Testing Checklist

Before committing code:

- [ ] Added CSRF token to all forms
- [ ] Validated all user inputs (server-side)
- [ ] Escaped all user-provided HTML output
- [ ] No sensitive data in logs or error messages
- [ ] No SQL injection vulnerabilities (using JPA)
- [ ] No hardcoded credentials or secrets
- [ ] Proper error handling (generic messages to users)
- [ ] Security headers present in responses
- [ ] Session cookies have security flags
- [ ] No console.log() statements in production code
- [ ] Authorization checks in place for protected endpoints
- [ ] Password fields use proper input types
- [ ] All dependencies are up to date

---

## 13. Common Security Mistakes to Avoid

| Mistake | Risk | Prevention |
|---------|------|-----------|
| No CSRF token | CSRF attacks | Add token to all forms |
| No input validation | Injection attacks | Use @Valid and InputValidator |
| Trusting user input | XSS attacks | Escape HTML output |
| SQL string concatenation | SQL injection | Use parameterized queries (JPA) |
| Storing plain passwords | Password theft | Use BCrypt |
| Long session timeouts | Session hijacking | Set 30-minute timeout |
| Logging sensitive data | Information disclosure | Log generic messages |
| Disabled HTTPS | MITM attacks | Enable SSL in production |
| No authorization checks | Unauthorized access | Check user role/permissions |
| Verbose error messages | Information disclosure | Use generic error messages |

---

## 14. Security Review Questions

When reviewing code:

1. Are all form submissions protected with CSRF tokens?
2. Is user input validated on the server-side?
3. Is all user-provided output properly escaped?
4. Are sensitive operations authorized correctly?
5. Are error messages generic (not exposing system details)?
6. Are passwords hashed with BCrypt?
7. Are database queries using parameterized queries?
8. Is debug logging removed from production code?
9. Are security headers properly configured?
10. Are all dependencies up to date?

---

## 15. Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [MDN Web Security](https://developer.mozilla.org/en-US/docs/Web/Security)
- [CWE Most Dangerous](https://cwe.mitre.org/top25/)

---

**Always prioritize security in your code. Security is everyone's responsibility!**
