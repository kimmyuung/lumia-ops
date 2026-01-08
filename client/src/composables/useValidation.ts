import { ref, computed, type Ref } from 'vue'
import { getFieldErrors, getErrorMessage, isApiError, type FieldError } from '@/utils/error'

/**
 * 폼 Validation 에러 관리 컴포저블
 * 
 * 사용 예:
 * const { setError, getError, hasError, clearErrors, errors } = useValidation()
 * 
 * try {
 *   await api.submit(form)
 * } catch (err) {
 *   setError(err) // API 에러에서 자동으로 필드 에러 추출
 * }
 * 
 * <input :class="{ error: hasError('email') }" />
 * <span v-if="hasError('email')">{{ getError('email') }}</span>
 */
export function useValidation() {
    const errors: Ref<Record<string, string>> = ref({})
    const generalError: Ref<string | null> = ref(null)

    /**
     * 특정 필드의 에러 메시지 반환
     */
    const getError = (field: string): string | undefined => {
        return errors.value[field]
    }

    /**
     * 특정 필드에 에러가 있는지 확인
     */
    const hasError = (field: string): boolean => {
        return !!errors.value[field]
    }

    /**
     * 에러 존재 여부
     */
    const hasErrors = computed(() => {
        return Object.keys(errors.value).length > 0 || generalError.value !== null
    })

    /**
     * 모든 에러 초기화
     */
    const clearErrors = () => {
        errors.value = {}
        generalError.value = null
    }

    /**
     * 특정 필드의 에러 초기화
     */
    const clearFieldError = (field: string) => {
        delete errors.value[field]
    }

    /**
     * 수동으로 필드 에러 설정
     */
    const setFieldError = (field: string, message: string) => {
        errors.value[field] = message
    }

    /**
     * API 에러에서 필드 에러 자동 추출 및 설정
     * @param error - catch한 에러 객체
     * @returns 일반 에러 메시지 (필드 에러가 아닌 경우)
     */
    const setError = (error: unknown): string => {
        clearErrors()

        if (isApiError(error)) {
            // 필드별 에러 추출 (Validation 에러)
            const fieldErrors = getFieldErrors(error)

            if (fieldErrors.length > 0) {
                fieldErrors.forEach((fe: FieldError) => {
                    errors.value[fe.field] = fe.message
                })
                return '입력값을 확인해 주세요.'
            }
        }

        // 일반 에러
        const message = getErrorMessage(error)
        generalError.value = message
        return message
    }

    /**
     * 필드 입력 시 해당 필드 에러 자동 클리어
     * v-model과 함께 사용
     */
    const onFieldInput = (field: string) => {
        clearFieldError(field)
    }

    return {
        errors,
        generalError,
        getError,
        hasError,
        hasErrors,
        clearErrors,
        clearFieldError,
        setFieldError,
        setError,
        onFieldInput
    }
}
