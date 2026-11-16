# **Java 14 – Switch Expressions**

**Switch Expressions** were introduced as a standard feature in **Java 14** (JEP 361).

They improve traditional `switch` statements by allowing you to:

- Return a value directly from a `switch`.
- Eliminate repetitive `break` statements.
- Group multiple case labels.
- Use concise arrow (`->`) syntax.
- Reduce accidental fall-through bugs.

Let's understand this with examples, starting from Java 11.

## **1. Traditional switch statement – Java 11**

Consider a scenario where we want to determine an employee's designation based on their role.

```
public class EmployeeService {

    public String getDesignation(String role) {
        String designation;

        switch (role) {
            case "ADMIN":
                designation = "Administrator";
                break;

            case "MANAGER":
                designation = "Manager";
                break;

            case "DEVELOPER":
                designation = "Software Engineer";
                break;

            default:
                designation = "Unknown";
        }

        return designation;
    }
}
```

**Problems with the traditional approach:**

1. We need a separate variable to store the result.
2. We need explicit `break` statements.
3. Forgetting `break` can cause unintended fall-through.
4. The code is more verbose than necessary.

## **2. Switch expression – Java 14**

The same functionality can be implemented using the new switch expression syntax.

```
public class EmployeeService {

    public String getDesignation(String role) {

        return switch (role) {
            case "ADMIN" -> "Administrator";
            case "MANAGER" -> "Manager";
            case "DEVELOPER" -> "Software Engineer";
            default -> "Unknown";
        };
    }
}
```

Notice the differences:

| Traditional switch          | Switch expression          |
| --------------------------- | -------------------------- |
| Uses `:`                    | Uses `->`                  |
| Requires `break`            | No `break` required        |
| Assigns values to variables | Returns values directly    |
| Can fall through            | No accidental fall-through |
| More verbose                | More concise               |

## **3. Multiple case labels**

In traditional switch statements, we commonly write multiple case labels to execute the same logic.

**Java 11:**

```
switch (day) {
    case "SATURDAY":
    case "SUNDAY":
        type = "WEEKEND";
        break;

    case "MONDAY":
    case "TUESDAY":
    case "WEDNESDAY":
    case "THURSDAY":
    case "FRIDAY":
        type = "WEEKDAY";
        break;

    default:
        type = "INVALID";
}
```

**Java 14:**

```
String type = switch (day) {
    case "SATURDAY", "SUNDAY" -> "WEEKEND";

    case "MONDAY", "TUESDAY", "WEDNESDAY",
         "THURSDAY", "FRIDAY" -> "WEEKDAY";

    default -> "INVALID";
};
```

Multiple labels can now be grouped using commas.

## **4. Using yield for complex logic**

What if you need multiple statements inside a case?

For example, you want to calculate an employee's annual bonus based on their performance rating.

You can use a block with `{}` and the `yield` keyword.

```
public double calculateBonus(String rating, double salary) {

    return switch (rating) {

        case "EXCELLENT" -> {
            double bonus = salary * 0.20;
            yield bonus;
        }

        case "GOOD" -> {
            double bonus = salary * 0.10;
            yield bonus;
        }

        case "AVERAGE" -> {
            double bonus = salary * 0.05;
            yield bonus;
        }

        default -> {
            yield 0.0;
        }
    };
}
```

**What is&#x20;**`yield`**?**

- `yield` returns a value from a switch expression.
- It is used when a case contains a block of statements.
- Unlike `return`, it returns a value from the switch expression, not from the enclosing method.

Think of it this way:

```
case "EXCELLENT" -> salary * 0.20;
```

For a simple expression, no `yield` is necessary.

```
case "EXCELLENT" -> {
    double bonus = salary * 0.20;
    yield bonus;
}
```

For a block containing multiple statements, use `yield` to produce the result.

## **5. Important: Switch statement vs Switch expression**

Java 14 supports both forms.

**Switch statement:**

```
switch (role) {
    case "ADMIN" -> System.out.println("Administrator");
    case "USER" -> System.out.println("User");
    default -> System.out.println("Unknown");
}
```

**Switch expression:**

```
String description = switch (role) {
    case "ADMIN" -> "Administrator";
    case "USER" -> "User";
    default -> "Unknown";
};
```

The distinction:

- Switch statement performs actions.
- Switch expression produces a value.

## **6. Important points for interviews**

| Concept         | Explanation                                                              |
| --------------- | ------------------------------------------------------------------------ |
| Introduced      | Java 12 (preview)                                                        |
| Standardized    | Java 14                                                                  |
| JEP             | JEP 361                                                                  |
| Arrow syntax    | `->`                                                                     |
| Multiple labels | `case A, B ->`                                                           |
| `yield`         | Returns a value from a switch expression                                 |
| Fall-through    | Not possible with arrow-style cases                                      |
| Exhaustiveness  | Switch expressions must cover all possible cases, usually with `default` |

**One important distinction:** Traditional colon-style switch labels (`case X:`) can still fall through. The no-fall-through guarantee applies to arrow-style rules.

## **7. Practical exercise**

Try implementing the following using Java 14 switch expressions.

**Problem: Order Status Processor**

Create a method that accepts an order status and returns an appropriate message.

| Order status  | Expected message              |
| ------------- | ----------------------------- |
| CREATED       | Order placed successfully     |
| PROCESSING    | Your order is being processed |
| SHIPPED       | Your order has been shipped   |
| DELIVERED     | Order delivered successfully  |
| CANCELLED     | Your order was cancelled      |
| Anything else | Unknown order status          |

Additional requirements:

1. Use switch expressions.
2. Group multiple statuses if appropriate.
3. Use `yield` for at least one case with multiple statements.

## **What to remember**

Java 14 – Standard Feature

The biggest improvement is that `switch` is no longer just a control-flow statement. It can now be an expression that produces a value.

For someone modernizing Java 11 codebases, this is a useful refactoring opportunity, particularly in service-layer code, status mapping, enum handling, and DTO transformations.