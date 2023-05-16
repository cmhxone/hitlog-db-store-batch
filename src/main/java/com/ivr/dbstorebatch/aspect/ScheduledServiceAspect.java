package com.ivr.dbstorebatch.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ScheduledServiceAspect {

    @Pointcut("execution(* com.ivr.dbstorebatch.schedule..*.*(..))")
    private void scheduleExecution() {
    }

    /**
     * Logging when scheduled job start
     * 
     * @param joinpoint
     */
    @Before(value = "scheduleExecution()")
    public void beforeSchduleExcution(JoinPoint joinpoint) {

        Method method = getMethod(joinpoint);
        log.info("{}: schedule started", method.getName());
    }

    /**
     * Logging when scheduled job successfully executed
     * 
     * @param joinPoint
     */
    @AfterReturning(value = "scheduleExecution()")
    public void afterScheduleExecution(JoinPoint joinPoint) {

        Method method = getMethod(joinPoint);
        log.info("{}: schedule finishied", method.getName());
    }

    /**
     * Logging when scheduled job failed execution
     * 
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(value = "scheduleExecution()", throwing = "exception")
    public void afterScheduleExecutionWithException(JoinPoint joinPoint, Exception exception) {

        Method method = getMethod(joinPoint);
        log.error("{}: schedule failed. exception={}", method.getName(), exception.toString());
    }

    /**
     * Returns JoinPoint's method
     * 
     * @param joinPoint
     * @return
     */
    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
