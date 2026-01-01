package com.lumiaops.lumiacore.aspect

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class LoggingAspect {

    // @LogExecution 어노테이션이 붙은 메서드를 가로챔
    @Around("@annotation(com.lumiaops.lumiacore.common.annotation.LogExecution)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.signature.declaringType.simpleName

        // 실행 전 (Before)
        logger.info { "👉 [START] $className.$methodName() Args: ${joinPoint.args.joinToString()}" }

        val stopWatch = StopWatch()
        stopWatch.start()

        try {
            // 실제 메서드 실행
            val result = joinPoint.proceed()

            // 실행 후 (After Returning)
            stopWatch.stop()
            logger.info { "[END] $className.$methodName() - 소요시간: ${stopWatch.totalTimeMillis}ms - 결과: $result" }

            return result
        } catch (e: Exception) {
            // 예외 발생 시 (After Throwing)
            stopWatch.stop()
            logger.error(e) { "[ERROR] $className.$methodName() - 소요시간: ${stopWatch.totalTimeMillis}ms" }
            throw e
        }
    }
}