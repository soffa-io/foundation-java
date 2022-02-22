package io.soffa.foundation.service.core.aop;

import com.google.common.base.Supplier;
import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.application.context.RequestContextUtil;
import io.soffa.foundation.application.model.Validatable;
import io.soffa.foundation.errors.ErrorUtil;
import io.soffa.foundation.errors.ManagedException;
import io.soffa.foundation.errors.TechnicalException;
import io.soffa.foundation.errors.UnauthorizedException;
import io.soffa.foundation.infrastructure.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.soffa.foundation.application.CoreMetrics.OPERATION_PREFIX;

@Aspect
@Component
@AllArgsConstructor
public class OperationHandlerAspect {

    private MetricsRegistry metricsRegistry;

    @SneakyThrows
    @Around("execution(* io.soffa.foundation.application.Operation*.*(..))")
    public Object handleOperation(ProceedingJoinPoint jp) {
        Object[] args = jp.getArgs();

        Object input;
        RequestContext context;
        boolean hasInput = args.length == 2;

        if (hasInput) {
            input = args[0];
            context = (RequestContext) args[1];
            if (input instanceof Validatable) {
                ((Validatable) input).validate();
            }
        } else {
            context = (RequestContext) args[0];
        }

        String operationId = OPERATION_PREFIX + jp.getTarget().getClass().getSimpleName();
        Map<String, Object> tags = RequestContextUtil.tagify(context);

        //noinspection Convert2Lambda
        return metricsRegistry.track(operationId, tags, new Supplier<Object>() {
            @SneakyThrows
            @Override
            public Object get() {
                try {
                    return jp.proceed(args);
                } catch (AuthenticationCredentialsNotFoundException e) {
                    throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                } catch (Exception e) {
                    if (e instanceof ManagedException) {
                        throw e;
                    }
                    throw new TechnicalException(e);
                }
            }
        });
    }


   /*
    @Override
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass) {
        RequestContext context = RequestContextHolder.require();
        return handle(operationClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass, RequestContext context) {

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.process(context);
        }

        for (NoInputOperation<?> act : mapping.getRegistry0()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                NoInputOperation<O> impl = (NoInputOperation<O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getName(), ImmutableMap.of(
                    Constants.OPERATION, operationClass.getName()
                ), () -> {
                    try {
                        return impl.handle(context);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    }
                });
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }
    */
}
