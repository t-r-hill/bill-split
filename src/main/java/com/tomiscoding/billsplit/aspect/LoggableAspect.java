package com.tomiscoding.billsplit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggableAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggableAspect.class);

    @Pointcut("(@annotation(LogException))")
    public void logException(){}

    @Before("logException()")
    public void executeLogException(JoinPoint joinPoint){
        StringBuilder msgLog = new StringBuilder("Exception caught: ");
        msgLog.append(joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0){
            Exception e = (Exception) args[0];
            msgLog.append(e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info(msgLog.toString());
    }
}
