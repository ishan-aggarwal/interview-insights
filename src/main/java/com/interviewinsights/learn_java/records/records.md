# **Java 16 – Records**

**Java 11:**

```
public class Employee {
private final String name;
private final int age;

    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

**Java 17:**

```
public record Employee(String name, int age) {
}
```

What you should understand:
- Automatically generated constructor, accessors, equals(), hashCode() and toString().
- Records are implicitly final.
- Records are shallowly immutable, not deeply immutable.
- When records are unsuitable, such as entities with mutable lifecycle or identity.


1. Additional advantages and practical uses of records 

1.1 Better data modelling
Records communicate intent clearly: this class represents a piece of data rather than an object with a mutable lifecycle.

Compare:
```
public class Employee {
private final String name;
private final int age;
// Constructor, getters, equals, hashCode, toString...
}
```

With:
```
public record Employee(String name, int age) {}
```

When someone reads the second declaration, they immediately understand that Employee is intended to be a data carrier.

1.2 Constructor validation
You can validate data directly inside a record using a compact constructor.

```
public record Employee(String name, int age) {

    public Employee {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (age < 18) {
            throw new IllegalArgumentException("Employee must be an adult");
        }
    }
}
```

Usage:

```Employee employee = new Employee("Ishan", 35);```


Invalid data is rejected at object creation.
Practical usage: API request validation, configuration objects, domain value objects, and event payloads.

1.3 Custom methods are allowed
Records aren't restricted to storing data. You can add business-related methods.

```
public record Employee(String name, int age) {

    public boolean isSeniorEmployee() {
        return age >= 40;
    }

    public String getDisplayName() {
        return name + " (" + age + ")";
    }
}
```

Usage:

```
Employee employee = new Employee("Ishan", 35);

System.out.println(employee.isSeniorEmployee());
System.out.println(employee.getDisplayName());
```

1.4 Custom accessors and constructors
You can override accessors and define additional constructors.

For example:

```
public record Employee(String name, int age) {

    public String name() {
        return name.toUpperCase();
    }

    public Employee(String name) {
        this(name, 18);
    }
}
```

Note that record accessors use name() rather than the JavaBean convention getName().

1.5 Excellent for DTOs and REST APIs
Consider a Spring Boot REST API.

Previously:
```
public class EmployeeResponse {
private String name;
private int age;

    // Constructor, getters, setters...
}
```

With records:

```
public record EmployeeResponse(
Long id,
String name,
int age,
String department
) {}
```

Controller:

```
@GetMapping("/{id}")
public EmployeeResponse getEmployee(@PathVariable Long id) {

    return new EmployeeResponse(
        id,
        "Ishan",
        35,
        "Engineering"
    );
}
```

Jackson supports Java records in modern versions, making them convenient for JSON serialization and deserialization.

1.6 Ideal for returning multiple values
Suppose a method needs to return two related values.

Instead of creating a separate POJO:

```
public record EmployeeStatistics(
int totalEmployees,
double averageAge
) {}
```

You can return:
```
public EmployeeStatistics calculateStatistics() {
return new EmployeeStatistics(100, 32.5);
}
```

This is cleaner than returning Object[], Map, or introducing a dedicated mutable class.
1.7 Useful in Streams and functional programming
Records work particularly well with Java Streams.
public record Employee(String name, String department, int age) {}


Example:
```
List<Employee> employees = List.of(
new Employee("A", "Engineering", 30),
new Employee("B", "HR", 35),
new Employee("C", "Engineering", 40)
);

Map<String, List<Employee>> groupedEmployees =
employees.stream()
.collect(Collectors.groupingBy(Employee::department));
```

They are also useful as composite map keys because they automatically implement equals() and hashCode().