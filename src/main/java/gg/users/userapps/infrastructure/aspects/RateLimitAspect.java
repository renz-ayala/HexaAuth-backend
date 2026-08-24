package gg.users.userapps.infrastructure.aspects;

import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.exception.RateLimitExceededException;
import gg.users.userapps.infrastructure.adapters.in.web.contracts.ForgotPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisWebPort redisWebPort;

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        var args = joinPoint.getArgs();

        if (args.length == 0 || args[0] == null) {
            throw new IllegalArgumentException("No válid request");
        }

        String username = (args[0] instanceof ForgotPasswordRequest(String username1))
                ? username1
                : args[0].toString();

        if (!redisWebPort.isAllowedToContinue(username, Duration.ofSeconds(rateLimited.seconds()))) {
            throw new RateLimitExceededException(
                    "Espera los %s segundos antes de volver a solicitarlo.".formatted(rateLimited.seconds()));
        }

        return joinPoint.proceed();
    }
}
