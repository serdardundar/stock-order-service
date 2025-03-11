package com.broker.stock.aspect;

import com.broker.stock.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final CustomerRepository customerRepository;

    @Before("@annotation(AuthorizeCustomer)")
    public void authorizeAccess(JoinPoint joinPoint) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var username = authentication.getName();

        // Admins bypass authorization
        if (isAdmin(authentication)) {
            return;
        }

        // Extract customerId
        var customerId = extractCustomerId(joinPoint)
            .orElseThrow(() -> new IllegalArgumentException("CustomerId not found in method arguments or fields"));

        // Validate customer access
        validateCustomerAccess(customerId, username);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")); // Using standard Spring Security role naming
    }

    private Optional<Long> extractCustomerId(JoinPoint joinPoint) {
        var method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        var args = joinPoint.getArgs();

        // Try extracting from annotated parameters
        return extractFromParameters(args, method)
            // Fallback to extracting from annotated fields
            .or(() -> extractFromFields(args));
    }

    private Optional<Long> extractFromParameters(Object[] args, Method method) {
        var parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (var annotation : parameterAnnotations[i]) {
                if (annotation instanceof CustomerId) {
                    // Safely extract customerId from parameter
                    var arg = args[i];
                    if (arg instanceof Long) {
                        return Optional.of((Long) arg);
                    } else {
                        throw new IllegalArgumentException(
                            "Invalid parameter type for @CustomerId; expected Long but got " + arg.getClass()
                        );
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Long> extractFromFields(Object[] args) {
        return Arrays.stream(args)
            .filter(Objects::nonNull) // Ignore null arguments
            .flatMap(arg -> Arrays.stream(arg.getClass().getDeclaredFields())) // Access fields of arguments
            .filter(field -> field.isAnnotationPresent(CustomerId.class)) // Find @CustomerId-annotated fields
            .map(field -> extractFieldValue(field, args)) // Extract the value of the annotated field
            .findFirst();
    }

    private Long extractFieldValue(Field field, Object[] args) {
        field.setAccessible(true);

        for (Object arg : args) {
            if (arg != null && field.getDeclaringClass().isAssignableFrom(arg.getClass())) {
                try {
                    Object value = field.get(arg);
                    if (value instanceof Long) {
                        return (Long) value;
                    } else {
                        throw new IllegalArgumentException(
                            "Invalid field type for @CustomerId; expected Long but got " + (value != null ? value.getClass() : "null")
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to access @CustomerId field", e);
                }
            }
        }
        return null; // No matching field found
    }

    private void validateCustomerAccess(Long customerId, String username) {
        customerRepository.findByUsername(username)
            .filter(customer -> customer.getId().equals(customerId))
            .orElseThrow(() -> new IllegalArgumentException("Unauthorized: You do not have access to this customer's data"));
    }
}

