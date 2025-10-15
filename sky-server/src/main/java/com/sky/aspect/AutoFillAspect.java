package com.sky.aspect;

/*
 * @Author lhj
 * @Create 2025/10/15 16:58
 * Description:
 * @Version 1.0
 */

import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面, 实现公共字段填充自动处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 配置切入点
     * 需要符合前面的逻辑 和 带有后面的注解
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.Autofill)")
    public void autoFillPointCut() {

    }

    /**
     * 前置通知, 配置处理逻辑
     * <p>
     * 具体来说，我们可以通过 JoinPoint 对象获得被拦截的方法.<br/>
     * 然后通过这个方法可以得到方法的 注解 和方法的 参数<br/>
     * 通过注解可以判断具体是哪个类型(OperationType.INSERT or OPERATION.UPDATE)<br/>
     * 通过参数可以对更新或插入的对象中的属性进行修改<br/>
     * 由此便完成了 AOP + 注解 + 反射 + 枚举 实现动态注入属性<br/>
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始注入属性");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method mt = signature.getMethod();
        Autofill autofill = mt.getAnnotation(Autofill.class);
        OperationType type = autofill.value();
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object obj = args[0];
        // 下面代码仍可优化
        // 若是插入操作
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        if (type == OperationType.INSERT) {
            try {
                Method setCreateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setCreateTime.invoke(obj, now);
                setUpdateTime.invoke(obj, now);
                setCreateUser.invoke(obj, id);
                setUpdateUser.invoke(obj, id);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        // 若是更新操作
        if (type == OperationType.UPDATE) {
            try {
                Method setUpdateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(obj, now);
                setUpdateUser.invoke(obj, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}